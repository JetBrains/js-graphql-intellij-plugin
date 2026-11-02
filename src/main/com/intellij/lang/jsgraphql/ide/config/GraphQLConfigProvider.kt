@file:OptIn(FlowPreview::class)

package com.intellij.lang.jsgraphql.ide.config

import com.intellij.ide.scratch.ScratchUtil
import com.intellij.lang.jsgraphql.GraphQLConfigOverridePath
import com.intellij.lang.jsgraphql.GraphQLExecutionMode
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLConfigEnvironmentListener
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLConfigLoader
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawConfig
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig
import com.intellij.lang.jsgraphql.ide.highlighting.GraphQLHighlightingRestarter
import com.intellij.lang.jsgraphql.ide.injection.GraphQLFileTypeContributor
import com.intellij.lang.jsgraphql.ide.injection.GraphQLInjectedLanguage
import com.intellij.lang.jsgraphql.ide.introspection.remote.GraphQLRemoteSchemasRegistry
import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourcesManager
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLResolveUtil
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeDependency
import com.intellij.lang.jsgraphql.parseOverrideConfigComment
import com.intellij.lang.jsgraphql.psi.GraphQLFile
import com.intellij.lang.jsgraphql.psi.getPhysicalVirtualFile
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.traceThrowable
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ModuleRootEvent
import com.intellij.openapi.roots.ModuleRootListener
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.CommonProcessors
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import com.intellij.util.concurrency.annotations.RequiresReadLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.time.Duration.Companion.milliseconds

@Service(Service.Level.PROJECT)
class GraphQLConfigProvider(private val project: Project, coroutineScope: CoroutineScope) : ModificationTracker, GraphQLConfigEnvironmentListener {
  companion object {
    private val LOG = logger<GraphQLConfigProvider>()

    private val CONFIG_CLOSEST =
      Key.create<CachedValue<GraphQLConfigSearchResult?>>("graphql.config.closest")
    private val CONFIG_OVERRIDE_FILE_KEY =
      Key.create<CachedValue<GraphQLConfigOverride?>>("graphql.config.override.file")
    private val CONFIG_OVERRIDE_PATH_KEY =
      Key.create<CachedValue<GraphQLConfigOverridePath?>>("graphql.config.override.path")

    private const val CONFIG_RELOAD_DELAY = 500

    private const val LOAD_LOCK_POLL_MS = 10L

    @JvmStatic
    fun getInstance(project: Project): GraphQLConfigProvider = project.service<GraphQLConfigProvider>()
  }

  private val generatedSourcesManager = GraphQLGeneratedSourcesManager.getInstance(project)
  private val remoteSchemasRegistry = GraphQLRemoteSchemasRegistry.getInstance(project)
  private val scopeDependency = GraphQLScopeDependency.getInstance(project)

  private val reloadRequests = MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  // need to trigger invalidation of dependent caches regardless of whether any changes are detected or not
  private val pendingInvalidation = AtomicBoolean(false)

  /**
   * The load state machine. [LoadState.requested] counts the reload requests, [LoadState.loaded] is the request that the last
   * completed load served. The first load is requested on creation.
   * - NotLoaded: `loaded == 0`.
   * - Loaded: `loaded == requested`.
   * - ReloadPending: `0 < loaded < requested`.
   *
   * Loading is not a state: it is [loadLock] held by the loading thread.
   */
  private val loadState = AtomicReference(LoadState(requested = 1, loaded = 0))

  /**
   * Held by the thread that loads the configuration in the synchronous mode, see [ensureInitialized].
   * It is held only by a thread that makes progress, it never waits for another thread while holding it.
   */
  private val loadLock = ReentrantLock()

  /**
   * Use this service as a dependency for tracking content changes in configuration files.
   * Note that calculations that depend on the scope structure should probably depend on
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
    GraphQLFileTypeContributor.EP_NAME.addChangeListener(coroutineScope, ::invalidate)
    GraphQLInjectedLanguage.EP_NAME.addChangeListener(coroutineScope, ::invalidate)
    GraphQLConfigContributor.EP_NAME.addChangeListener(coroutineScope, ::invalidate)

    val connection = project.messageBus.connect(coroutineScope)
    connection.subscribe(GraphQLConfigEnvironmentListener.TOPIC, this)
    connection.subscribe(ModuleRootListener.TOPIC, object : ModuleRootListener {
      override fun rootsChanged(event: ModuleRootEvent) {
        requestReload()
      }
    })

    VirtualFileManager.getInstance().addAsyncFileListener(coroutineScope, GraphQLConfigFileListener(project))

    if (!GraphQLExecutionMode.isSynchronous) {
      coroutineScope.launch {
        reloadRequests.debounce(CONFIG_RELOAD_DELAY.milliseconds).collect {
          reload()
        }
      }
    }
  }

  @RequiresReadLock(generateAssertion = false /* IJPL-115548 */)
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

  @RequiresReadLock(generateAssertion = false /* IJPL-115548 */)
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

  /**
   * `true` when a configuration load completed and was not dropped by [invalidate] since.
   * It does not trigger a load, see [ensureInitialized].
   */
  val isInitialized: Boolean
    get() = loadState.get().isInitialized

  /**
   * `true` when the last completed load served the last reload request.
   */
  val isUpToDate: Boolean
    get() = loadState.get().isUpToDate

  /**
   * Returns [isInitialized] after loading the configuration if needed.
   *
   * In the synchronous mode (see [GraphQLExecutionMode]) it loads the configuration under the read action of the caller when a reload is
   * requested. A call from the loading thread itself (e.g. a [GraphQLConfigContributor] that resolves a scope) does not load again
   * and returns the state of the previous load. [com.intellij.openapi.project.IndexNotReadyException] and
   * [com.intellij.openapi.progress.ProcessCanceledException] propagate; the state is not advanced then.
   *
   * In the asynchronous mode it only returns [isInitialized]: the configuration is loaded by [reload] in the background.
   */
  @RequiresReadLock(generateAssertion = false /* IJPL-115548 */)
  fun ensureInitialized(): Boolean {
    if (!GraphQLExecutionMode.isSynchronous) return isInitialized
    if (isUpToDate) return true
    if (loadLock.isHeldByCurrentThread || project.isDisposed) return isInitialized

    lockCancellably()
    try {
      val state = loadState.get()
      if (!state.isUpToDate) {
        performLoad(state.requested)
      }
    }
    finally {
      loadLock.unlock()
    }
    return isInitialized
  }

  /**
   * The lock holder makes progress under its own read action, so the wait is short; it stays cancellable to let a write action in.
   */
  private fun lockCancellably() {
    while (!loadLock.tryLock(LOAD_LOCK_POLL_MS, TimeUnit.MILLISECONDS)) {
      ProgressManager.checkCanceled()
    }
  }

  /**
   * Loads the configuration for the reload request [generation] in the synchronous mode. Called under [loadLock] and a read action.
   */
  private fun performLoad(generation: Long) {
    val discoveredConfigFiles = discoverConfigFiles()
    val explicitInvalidation = pendingInvalidation.getAndSet(false)
    val hasChanged = evaluateConfigs(discoveredConfigFiles, explicitInvalidation)
    applyLoaded(generation, hasChanged, explicitInvalidation)
  }

  val hasExplicitConfiguration: Boolean
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
        val virtualFile = StandardFileSystems.local().findFileByPath(override.path)
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

  @RequiresReadLock(generateAssertion = false /* IJPL-115548 */)
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

  @RequiresReadLock(generateAssertion = false /* IJPL-115548 */)
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

    LOG.traceThrowable { Throwable("Invalidating GraphQL config${if (configFile == null) "" else " for $configFile"}") }

    if (configFile != null) {
      configData[configFile]?.invalidated?.set(true)
    }
    else {
      configData.clear()
      contributedConfigs.set(emptyMap())
      loadState.updateAndGet { it.copy(loaded = 0) }
    }

    pendingInvalidation.set(true)
    requestReload()
  }

  /**
   * Requests a configuration reload. The synchronous mode loads on the next [ensureInitialized]; the asynchronous mode schedules [reload].
   */
  fun requestReload() {
    if (project.isDisposed) return

    LOG.traceThrowable { Throwable("Requesting GraphQL config reload") }

    loadState.updateAndGet { it.copy(requested = it.requested + 1) }
    if (!GraphQLExecutionMode.isSynchronous) {
      check(reloadRequests.tryEmit(Unit))
    }
  }

  @Deprecated("Use requestReload()", ReplaceWith("requestReload()"))
  fun scheduleConfigurationReload() {
    requestReload()
  }

  /**
   * Reloads the configuration and returns when the load completes.
   * The synchronous mode requests a reload and loads it under a smart read action, see [ensureInitialized].
   */
  @RequiresBackgroundThread(generateAssertion = false /* IJPL-115548 */)
  suspend fun reload() {
    if (project.isDisposed) return
    checkCanceled()

    if (GraphQLExecutionMode.isSynchronous) {
      requestReload()
      // the loader reads the VFS text, so the edited config documents are saved first, as in the asynchronous mode
      saveModifiedDocuments(smartReadAction(project) { discoverConfigFiles() })
      smartReadAction(project) { ensureInitialized() }
      return
    }

    val generation = loadState.get().requested
    val discoveredConfigFiles = smartReadAction(project) { discoverConfigFiles() }
    saveModifiedDocuments(discoveredConfigFiles)

    val explicitInvalidation = pendingInvalidation.getAndSet(false)
    val hasChanged = evaluateConfigs(discoveredConfigFiles, explicitInvalidation)

    if (applyLoaded(generation, hasChanged, explicitInvalidation)) {
      withContext(Dispatchers.EDT) {
        PsiManager.getInstance(project).dropPsiCaches()
      }
      GraphQLHighlightingRestarter.getInstance(project).restart("GraphQLConfigProvider.reload")
    }
  }

  @RequiresReadLock(generateAssertion = false /* IJPL-115548 */)
  private fun discoverConfigFiles(): Set<VirtualFile> = configFiles.value

  /**
   * Loads the changed [discoveredConfigFiles] and polls the [GraphQLConfigContributor]s. Returns `true` when anything changed.
   *
   * [configData] is updated entry by entry. When the evaluation throws (e.g. cancellation) after a change was applied, the retry would
   * see the matching timestamps and lose the change, so the change is kept as a pending invalidation for the next load.
   */
  private fun evaluateConfigs(discoveredConfigFiles: Set<VirtualFile>, explicitInvalidation: Boolean): Boolean {
    var hasChanged = false
    try {
      evaluateConfigFiles(discoveredConfigFiles) { hasChanged = true }
      if (pollConfigContributors(explicitInvalidation)) {
        hasChanged = true
      }
      return hasChanged
    }
    catch (e: Throwable) {
      if (explicitInvalidation || hasChanged) {
        pendingInvalidation.set(true)
      }
      throw e
    }
  }

  /**
   * Loads the changed [discoveredConfigFiles] into [configData]. Calls [onChange] right after each change of [configData].
   */
  private fun evaluateConfigFiles(discoveredConfigFiles: Set<VirtualFile>, onChange: () -> Unit) {
    val loader = GraphQLConfigLoader.getInstance(project)
    if (configData.keys.removeIf { !it.isValid || it !in discoveredConfigFiles }) {
      onChange()
    }

    for (file in discoveredConfigFiles) {
      ProgressManager.checkCanceled()
      val dir = file.parent.takeIf { it.isValid && it.isDirectory }
      if (!file.isValid || dir == null) {
        continue
      }

      val timeStamp = file.timeStamp
      val cached = configData[file]
      // the flag is not consumed here: a failed load keeps it for the retry; a replaced entry starts with a fresh flag
      if (cached != null && cached.timeStamp == timeStamp && !cached.invalidated.get()) {
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

      onChange()
    }
  }

  /**
   * Publishes the loaded configuration and completes the load of the reload request [generation].
   * Returns `true` when the configuration change was published. Mode-independent: no EDT, no daemon.
   */
  private fun applyLoaded(generation: Long, hasChanged: Boolean, explicitInvalidation: Boolean): Boolean {
    val initialized = isInitialized
    val shouldNotifyConfigurationChange = hasChanged || explicitInvalidation || !initialized
    if (shouldNotifyConfigurationChange) {
      LOG.debug {
        "GraphQL configuration changed: hasChanged=$hasChanged, explicitInvalidation=$explicitInvalidation, initialized=$initialized"
      }

      try {
        updateModificationTrackers()
        ResolveCache.getInstance(project).clearCache(true)
        project.messageBus.syncPublisher(GraphQLConfigListener.TOPIC).onConfigurationChanged()
      }
      catch (e: Throwable) {
        // the load does not complete; the next load publishes again
        pendingInvalidation.set(true)
        throw e
      }
    }

    loadState.updateAndGet { it.copy(loaded = maxOf(it.loaded, generation)) }
    return shouldNotifyConfigurationChange
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
        requestReload()
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

  private fun updateModificationTrackers() {
    modificationTracker.incModificationCount()
    scopeDependency.update()
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
  @RequiresBackgroundThread(generateAssertion = false /* IJPL-115548 */)
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

  private data class LoadState(val requested: Long, val loaded: Long) {
    val isInitialized: Boolean get() = loaded > 0
    val isUpToDate: Boolean get() = loaded == requested
  }

  data class ConfigEvaluationState(val status: GraphQLConfigEvaluationStatus, val error: Throwable?)
}

class GraphQLConfigSearchResult(val config: GraphQLConfig, val projectName: String? = null)

private data class GraphQLConfigOverride(val file: VirtualFile, val projectName: String?)
