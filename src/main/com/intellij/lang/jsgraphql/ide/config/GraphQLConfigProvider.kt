@file:OptIn(FlowPreview::class)

package com.intellij.lang.jsgraphql.ide.config

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.ide.scratch.ScratchUtil
import com.intellij.lang.jsgraphql.GraphQLConfigOverridePath
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLConfigEnvironmentListener
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLConfigLoader
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawConfig
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig
import com.intellij.lang.jsgraphql.ide.injection.GraphQLFileTypeContributor
import com.intellij.lang.jsgraphql.ide.injection.GraphQLInjectedLanguage
import com.intellij.lang.jsgraphql.ide.introspection.remote.GraphQLRemoteSchemasRegistry
import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourcesManager
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLResolveUtil
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeDependency
import com.intellij.lang.jsgraphql.parseOverrideConfigComment
import com.intellij.lang.jsgraphql.psi.GraphQLFile
import com.intellij.lang.jsgraphql.psi.getPhysicalVirtualFile
import com.intellij.openapi.application.*
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.ex.temp.TempFileSystem
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.CommonProcessors
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import com.intellij.util.concurrency.annotations.RequiresReadLock
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.milliseconds


@Service(Service.Level.PROJECT)
class GraphQLConfigProvider(private val project: Project, cs: CoroutineScope) : ModificationTracker, GraphQLConfigEnvironmentListener {

  companion object {
    private val LOG = logger<GraphQLConfigProvider>()

    private val CONFIG_CLOSEST =
      Key.create<CachedValue<GraphQLConfigSearchResult?>>("graphql.config.closest")
    private val CONFIG_OVERRIDE_FILE_KEY =
      Key.create<CachedValue<GraphQLConfigOverride?>>("graphql.config.override.file")
    private val CONFIG_OVERRIDE_PATH_KEY =
      Key.create<CachedValue<GraphQLConfigOverridePath?>>("graphql.config.override.path")

    private const val CONFIG_RELOAD_DELAY = 500

    @JvmStatic
    fun getInstance(project: Project) = project.service<GraphQLConfigProvider>()
  }

  private val generatedSourcesManager = GraphQLGeneratedSourcesManager.getInstance(project)
  private val remoteSchemasRegistry = GraphQLRemoteSchemasRegistry.getInstance(project)
  private val scopeDependency = GraphQLScopeDependency.getInstance(project)

  private val reloadConfigAlarm = MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  // need to trigger invalidation of dependent caches regardless of whether any changes are detected or not
  private val pendingInvalidation = AtomicBoolean(false)

  @Volatile
  private var initialized = false

  /**
   * Use this service as a dependency for tracking content changes in configuration files.
   * Note, that calculations which depends on the scope structure should probably depend on
   * [GraphQLScopeDependency] instead, which tracks this service state and some more stuff,
   * which could affect resolve and type evaluation.
   */
  private val modificationTracker = SimpleModificationTracker()

  private val configData: MutableMap<VirtualFile, ConfigEntry> = ConcurrentHashMap()

  // NB: this is directory -> config mapping, because a physical config file may not exist
  private val contributedConfigs: AtomicReference<Map<VirtualFile, GraphQLConfig>> = AtomicReference(emptyMap())

  private val configFiles: CachedValue<Set<VirtualFile>> = CachedValuesManager.getManager(project).createCachedValue {
    CachedValueProvider.Result.create(
      queryAllConfigFiles(),
      VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
      ProjectRootManager.getInstance(project)
    )
  }

  private val configFileInDirectory: CachedValue<ConcurrentMap<VirtualFile, Optional<VirtualFile>>> =
    CachedValuesManager.getManager(project).createCachedValue {
      CachedValueProvider.Result.create(ConcurrentHashMap(), scopeDependency)
    }

  private val configsInParentDirectories: CachedValue<ConcurrentMap<VirtualFile, Optional<GraphQLConfig>>> =
    CachedValuesManager.getManager(project).createCachedValue {
      CachedValueProvider.Result.create(ConcurrentHashMap(), scopeDependency)
    }

  init {
    GraphQLFileTypeContributor.EP_NAME.addChangeListener(cs, ::invalidate)
    GraphQLInjectedLanguage.EP_NAME.addChangeListener(cs, ::invalidate)
    GraphQLConfigContributor.EP_NAME.addChangeListener(cs, ::invalidate)

    project.messageBus.connect(cs).subscribe(GraphQLConfigEnvironmentListener.TOPIC, this)

    cs.launch {
      reloadConfigAlarm.debounce(CONFIG_RELOAD_DELAY.milliseconds).collectLatest {
        reload()
      }
    }
  }

  @RequiresReadLock
  fun resolveProjectConfig(context: PsiFile): GraphQLProjectConfig? {
    // shouldn't try resolving for the config file itself
    if (isConfigFile(context)) {
      return null
    }

    val searchResult = findConfig(context)
    return when {
      searchResult == null -> null
      searchResult.projectName != null -> searchResult.config.findProject(searchResult.projectName)
      else -> searchResult.config.match(context)
    }
  }

  @RequiresReadLock
  fun resolveProjectConfig(virtualFile: VirtualFile): GraphQLProjectConfig? =
    PsiManager.getInstance(project).findFile(virtualFile)?.let { resolveProjectConfig(it) }

  fun getForConfigFile(file: VirtualFile?): GraphQLConfig? {
    return when {
      file == null -> null
      file.isDirectory -> contributedConfigs.get()[file]
      else -> configData[file]?.config
    }
  }

  fun isCachedConfigOutdated(file: VirtualFile?): Boolean {
    if (file == null) return false
    val entry = configData[file] ?: return false
    return runReadAction { file.timeStamp != entry.timeStamp || FileDocumentManager.getInstance().isFileModified(file) }
  }

  @JvmOverloads
  fun getAllConfigs(includeContributed: Boolean = true): List<GraphQLConfig> {
    val configs = configData.mapNotNull { it.value.config }
    return if (includeContributed) configs + contributedConfigs.get().values else configs
  }

  fun getConfigEvaluationState(file: VirtualFile): ConfigEvaluationState? {
    val entry = configData[file] ?: return null
    return ConfigEvaluationState(entry.status, entry.error)
  }

  val isInitialized
    get() = initialized

  val hasExplicitConfiguration
    get() = configData.isNotEmpty()

  private fun findOverriddenConfig(file: PsiFile): GraphQLConfigOverride? {
    return CachedValuesManager.getCachedValue(file, CONFIG_OVERRIDE_FILE_KEY) {
      CachedValueProvider.Result.create(findOverriddenConfigImpl(file), file, scopeDependency)
    }
  }

  private fun findOverriddenConfigImpl(file: PsiFile): GraphQLConfigOverride? {
    if (file is GraphQLFile && ScratchUtil.isScratch(file.virtualFile)) {
      val override = getContentDependentOverridePath(file)
      if (override != null) {
        val virtualFile = findFileByPath(override.path)
        if (virtualFile != null) {
          return if (virtualFile.isDirectory) {
            findConfigFileInDirectory(virtualFile)?.let {
              GraphQLConfigOverride(it, override.project)
            }
          }
          else {
            GraphQLConfigOverride(virtualFile, override.project)
          }
        }
      }

      return project.guessProjectDir()
        ?.let { findConfigFileInDirectory(it) }
        ?.let { GraphQLConfigOverride(it, null) }
    }

    return null
  }

  private fun findFileByPath(path: String): VirtualFile? {
    if (ApplicationManager.getApplication().isUnitTestMode) {
      return TempFileSystem.getInstance().findFileByPath(path)
    }

    return LocalFileSystem.getInstance().findFileByPath(path)
  }

  private fun getContentDependentOverridePath(file: PsiFile): GraphQLConfigOverridePath? {
    if (file !is GraphQLFile) {
      return null
    }

    return CachedValuesManager.getCachedValue(file, CONFIG_OVERRIDE_PATH_KEY) {
      val start = file.firstChild?.let { if (it is PsiWhiteSpace) PsiTreeUtil.skipWhitespacesForward(it) else it }
      val override =
        generateSequence(start) { prev ->
          PsiTreeUtil.skipWhitespacesForward(prev)?.takeIf { it is PsiComment }
        }
          .mapNotNull { parseOverrideConfigComment(it.text) }
          .firstOrNull()

      CachedValueProvider.Result.create(override, file)
    }
  }

  @RequiresReadLock
  fun findConfig(context: PsiFile): GraphQLConfigSearchResult? {
    return CachedValuesManager.getCachedValue(context, CONFIG_CLOSEST) {
      val overriddenConfig = findOverriddenConfig(context)
      if (overriddenConfig != null) {
        val config = getForConfigFile(overriddenConfig.file)
        if (config != null) {
          return@getCachedValue CachedValueProvider.Result.create(
            GraphQLConfigSearchResult(config, overriddenConfig.projectName ?: GraphQLConfig.DEFAULT_PROJECT),
            scopeDependency,
          )
        }
      }

      var from: VirtualFile? = getPhysicalVirtualFile(context)

      val sourceFile = generatedSourcesManager.getSourceFile(from) ?: remoteSchemasRegistry.getSourceFile(from)
      if (sourceFile != null) {
        from = sourceFile
      }

      CachedValueProvider.Result.create(
        findConfigInParents(from)?.let { GraphQLConfigSearchResult(it) },
        scopeDependency,
      )
    }
  }

  private fun findConfigInParents(file: VirtualFile?): GraphQLConfig? {
    if (file == null || ScratchUtil.isScratch(file)) return null

    val cache = configsInParentDirectories.value
    val prev = cache[file]
    if (prev != null) {
      return prev.orElse(null)
    }

    val contributedConfigsSnapshot = contributedConfigs.get()

    var config: GraphQLConfig? = null
    var fallback: GraphQLConfig? = null

    GraphQLResolveUtil.processDirectoriesUpToContentRoot(project, file) { dir ->
      val candidate = contributedConfigsSnapshot[dir]
      if (fallback == null && candidate != null) {
        fallback = candidate
      }

      val configFile = findConfigFileInDirectory(dir)
      if (configFile != null) {
        if (shouldSkipConfig(configFile)) {
          true
        }
        else {
          config = getForConfigFile(configFile)
          false
        }
      }
      else {
        true
      }
    }

    if (config == null) {
      config = fallback
    }

    val result = Optional.ofNullable(config)
    return (cache.putIfAbsent(file, result) ?: result).orElse(null)
  }

  private fun shouldSkipConfig(candidate: VirtualFile): Boolean {
    if (GraphQLConfigSearchCustomizer.EP_NAME.extensionList.any { it.isIgnoredConfig(project, candidate) }) {
      return true
    }

    if (candidate.name !in OPTIONAL_CONFIG_NAMES) {
      return false
    }

    val configEntry = configData[candidate] ?: return true
    return configEntry.status != GraphQLConfigEvaluationStatus.SUCCESS
  }

  @RequiresReadLock
  fun findConfigFileInDirectory(dir: VirtualFile): VirtualFile? {
    if (!dir.isDirectory) return null

    val cache = configFileInDirectory.value
    val prev = cache[dir]
    if (prev != null) {
      return prev.orElse(null)
    }
    val candidates = dir.children.filter { it.name in CONFIG_NAMES }.associateBy { it.name }
    val result = CONFIG_NAMES.find { it in candidates }?.let { candidates[it] }.let { Optional.ofNullable(it) }
    return (cache.putIfAbsent(dir, result) ?: result).orElse(null)
  }

  @JvmOverloads
  fun invalidate(configFile: VirtualFile? = null) {
    if (project.isDisposed) return

    if (configFile != null) {
      configData[configFile]?.invalidated?.set(true)
    }
    else {
      initialized = false
      configData.clear()
      contributedConfigs.set(emptyMap())
    }

    pendingInvalidation.set(true)
    scheduleConfigurationReload()
  }

  fun scheduleConfigurationReload() {
    if (project.isDisposed) return

    if (ApplicationManager.getApplication().isUnitTestMode) {
      DumbService.getInstance(project).smartInvokeLater(
        {
          runWithModalProgressBlocking(project, "") {
            reload()
          }
        },
        ModalityState.nonModal()
      )
    }
    else {
      check(reloadConfigAlarm.tryEmit(Unit))
    }
  }

  private suspend fun reload() {
    if (project.isDisposed) return
    checkCanceled()

    val discoveredConfigFiles = smartReadAction(project) {
      configFiles.value
    }
    saveModifiedDocuments(discoveredConfigFiles)

    val loader = GraphQLConfigLoader.getInstance(project)
    val explicitInvalidation = pendingInvalidation.getAndSet(false)
    var hasChanged = configData.keys.removeIf { !it.isValid || it !in discoveredConfigFiles }

    for (file in discoveredConfigFiles) {
      checkCanceled()
      val dir = file.parent.takeIf { it.isValid && it.isDirectory }
      if (!file.isValid || dir == null) {
        continue
      }

      val timeStamp = file.timeStamp
      val cached = configData[file]
      if (cached != null && cached.timeStamp == timeStamp && !cached.invalidated.getAndSet(false)) {
        continue
      }

      val result = loader.load(file)
      val entry = ConfigEntry(
        GraphQLConfig(project, dir, file, result.data ?: GraphQLRawConfig.EMPTY),
        timeStamp,
        result.status,
        result.error,
      )

      if (cached == null) {
        configData.putIfAbsent(file, entry)
      }
      else {
        configData.replace(file, cached, entry)
      }

      hasChanged = true
    }

    if (pollConfigContributors(explicitInvalidation)) {
      hasChanged = true
    }

    if (hasChanged || explicitInvalidation || !initialized) {
      notifyConfigurationChanged()
    }
  }

  private fun pollConfigContributors(explicitInvalidation: Boolean): Boolean {
    val prevSnapshot = contributedConfigs.get()
    val prevContributed = prevSnapshot.values.toSet()
    val updatedContributed =
      GraphQLConfigContributor.EP_NAME.extensionList.flatMap { it.contributeConfigs(project) }.toSet()

    if (prevContributed.isEmpty() && updatedContributed.isEmpty()) {
      return false
    }

    return if (prevContributed != updatedContributed || explicitInvalidation) {
      if (contributedConfigs.compareAndSet(prevSnapshot, updatedContributed.associateBy { it.dir })) {
        LOG.info("contributed configs changed")
        LOG.debug { "contributed configs: new=$updatedContributed, previous=$prevContributed" }
        true
      }
      else {
        // concurrent modification, let's try again later
        scheduleConfigurationReload()
        false
      }
    }
    else {
      false
    }
  }

  override fun onEnvironmentChanged() {
    configData.values
      .asSequence()
      .mapNotNull { it.config }
      .flatMap { it.getProjects().values }
      .filter { it.environment.variables.isNotEmpty() }
      .mapTo(mutableSetOf()) { it.file ?: it.dir }
      .forEach {
        invalidate(it)
      }
  }

  private suspend fun notifyConfigurationChanged() {
    withContext(Dispatchers.EDT) {
      initialized = true

      modificationTracker.incModificationCount()
      scopeDependency.update()
      WriteAction.run<Throwable> {
        PsiManager.getInstance(project).dropPsiCaches()
      }

      DaemonCodeAnalyzer.getInstance(project).restart()
      project.messageBus.syncPublisher(GraphQLConfigListener.TOPIC).onConfigurationChanged()
    }
  }

  private suspend fun saveModifiedDocuments(files: Collection<VirtualFile>) {
    if (files.isEmpty()) return
    val fileDocumentManager = FileDocumentManager.getInstance()
    val anyFileModified = readAction { files.any { fileDocumentManager.isFileModified(it) } }
    if (anyFileModified) {
      withContext(Dispatchers.EDT) {
        val documents = readAction {
          files.filter { it.isValid }.mapNotNull { fileDocumentManager.getDocument(it) }
        }

        documents.forEach {
          checkCanceled()
          fileDocumentManager.saveDocument(it)
        }
      }
    }
  }

  /**
   * Cached inside of [configFiles], do not use directly.
   */
  @RequiresBackgroundThread
  private fun queryAllConfigFiles(): Set<VirtualFile> {
    val processor = CommonProcessors.CollectUniquesProcessor<VirtualFile>()
    FilenameIndex.processFilesByNames(
      CONFIG_NAMES, true, GlobalSearchScope.projectScope(project), null, processor
    )
    return processor.results
      .filter { file -> GraphQLConfigSearchCustomizer.EP_NAME.extensionList.none { it.isIgnoredConfig(project, file) } }
      .toSet()
  }

  private class ConfigEntry(
    val config: GraphQLConfig? = null,
    val timeStamp: Long = -1,
    val status: GraphQLConfigEvaluationStatus,
    val error: Throwable? = null,
  ) {
    val invalidated: AtomicBoolean = AtomicBoolean(false)
  }

  override fun getModificationCount(): Long = modificationTracker.modificationCount

  data class ConfigEvaluationState(val status: GraphQLConfigEvaluationStatus, val error: Throwable?)
}

class GraphQLConfigSearchResult(val config: GraphQLConfig, val projectName: String? = null)

private data class GraphQLConfigOverride(val file: VirtualFile, val projectName: String?)
