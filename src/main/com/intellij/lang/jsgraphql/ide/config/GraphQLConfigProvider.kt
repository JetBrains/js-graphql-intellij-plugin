package com.intellij.lang.jsgraphql.ide.config

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.ide.scratch.ScratchUtil
import com.intellij.lang.jsgraphql.GraphQLConfigOverridePath
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLConfigLoader
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawConfig
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig
import com.intellij.lang.jsgraphql.ide.injection.GraphQLFileTypeContributor
import com.intellij.lang.jsgraphql.ide.injection.GraphQLInjectedLanguage
import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourcesManager
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLResolveUtil
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeDependency
import com.intellij.lang.jsgraphql.parseOverrideConfigComment
import com.intellij.lang.jsgraphql.psi.GraphQLFile
import com.intellij.lang.jsgraphql.psi.getPhysicalVirtualFile
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.*
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.util.BackgroundTaskUtil
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
import com.intellij.util.Alarm
import com.intellij.util.CommonProcessors
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import com.intellij.util.concurrency.annotations.RequiresReadLock
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference


@Service(Service.Level.PROJECT)
class GraphQLConfigProvider(private val project: Project) : Disposable, ModificationTracker {
    companion object {
        private val LOG = logger<GraphQLConfigProvider>()

        private val CONFIG_CLOSEST =
            Key.create<CachedValue<GraphQLConfig?>>("graphql.config.closest")
        private val CONFIG_OVERRIDE_FILE_KEY =
            Key.create<CachedValue<GraphQLConfigOverride?>>("graphql.config.override.file")
        private val CONFIG_OVERRIDE_PATH_KEY =
            Key.create<CachedValue<GraphQLConfigOverridePath?>>("graphql.config.override.path")

        private const val CONFIG_RELOAD_DELAY = 500

        @JvmStatic
        fun getInstance(project: Project) = project.service<GraphQLConfigProvider>()
    }

    init {
        GraphQLFileTypeContributor.EP_NAME.addChangeListener({ invalidate() }, this)
        GraphQLInjectedLanguage.EP_NAME.addChangeListener({ invalidate() }, this)
        GraphQLConfigContributor.EP_NAME.addChangeListener({ invalidate() }, this)
    }

    private val generatedSourcesManager = GraphQLGeneratedSourcesManager.getInstance(project)
    private val scopeDependency = GraphQLScopeDependency.getInstance(project)

    private val reloadConfigAlarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)

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

    @RequiresReadLock
    fun resolveConfig(context: PsiFile): GraphQLProjectConfig? {
        val overriddenConfig = findOverriddenConfig(context)
        if (overriddenConfig != null) {
            val config = getForConfigFile(overriddenConfig.file)
            if (config != null) {
                val projectConfig = config.findProject(overriddenConfig.projectName)
                if (projectConfig != null) {
                    return projectConfig
                }
            }
        }

        return findClosestConfig(context)?.match(context)
    }

    @RequiresReadLock
    fun resolveConfig(virtualFile: VirtualFile): GraphQLProjectConfig? =
        PsiManager.getInstance(project).findFile(virtualFile)?.let { resolveConfig(it) }

    fun getForConfigFile(file: VirtualFile?): GraphQLConfig? {
        return when {
            file == null -> null
            file.isDirectory -> contributedConfigs.get()[file]
            else -> configData[file]?.config
        }
    }

    fun getAllConfigs(): List<GraphQLConfig> {
        return configData.mapNotNull { it.value.config }
    }

    val isInitialized
        get() = initialized

    val hasConfigurationFiles
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
                    } else {
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
    fun findClosestConfig(context: PsiFile): GraphQLConfig? {
        return CachedValuesManager.getCachedValue(context, CONFIG_CLOSEST) {
            var from: VirtualFile? = getPhysicalVirtualFile(context)

            val sourceFile = generatedSourcesManager.getSourceFile(from)
            if (sourceFile != null) {
                from = sourceFile
            }

            val config = findConfigInParents(from)
            CachedValueProvider.Result.create(config, scopeDependency)
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
                } else {
                    config = getForConfigFile(configFile)
                    false
                }
            } else {
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
        if (candidate.name !in OPTIONAL_CONFIG_NAMES) {
            return false
        }

        val configEntry = configData[candidate] ?: return true
        return configEntry.status != GraphQLConfigLoader.Status.SUCCESS
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
        if (configFile != null) {
            configData.remove(configFile)
        } else {
            initialized = false
            configData.clear()
            contributedConfigs.set(emptyMap())
        }

        pendingInvalidation.set(true)
        scheduleConfigurationReload()
    }

    fun scheduleConfigurationReload() {
        if (ApplicationManager.getApplication().isUnitTestMode) {
            invokeLater { reload() }
        } else {
            reloadConfigAlarm.cancelAllRequests()
            reloadConfigAlarm.addRequest({
                BackgroundTaskUtil.runUnderDisposeAwareIndicator(this, ::reload)
            }, CONFIG_RELOAD_DELAY)
        }
    }

    private fun reload() {
        val discoveredConfigFiles = configFiles.value
        saveModifiedDocuments(discoveredConfigFiles)

        val loader = GraphQLConfigLoader.getInstance(project)
        val explicitInvalidate = pendingInvalidation.getAndSet(false)
        var hasChanged = configData.keys.removeIf { !it.isValid || it !in discoveredConfigFiles }

        for (file in discoveredConfigFiles) {
            ProgressManager.checkCanceled()
            val dir = file.parent.takeIf { it.isValid && it.isDirectory }
            if (!file.isValid || dir == null) {
                continue
            }

            val timeStamp = file.timeStamp
            val cached = configData[file]
            if (cached?.timeStamp == timeStamp) {
                continue
            }

            val result = loader.load(file)
            val entry = ConfigEntry(
                GraphQLConfig(project, dir, file, result.data ?: GraphQLRawConfig.EMPTY),
                timeStamp,
                result.status
            )

            hasChanged = true
            if (cached == null) {
                configData.putIfAbsent(file, entry)
            } else {
                configData.replace(file, cached, entry)
            }
        }

        if (pollConfigContributors()) {
            hasChanged = true
        }

        if (hasChanged || explicitInvalidate || !initialized) {
            notifyConfigurationChanged()
        }
    }

    private fun pollConfigContributors(): Boolean {
        val prevSnapshot = contributedConfigs.get()
        val prevContributed = prevSnapshot.values.toSet()
        val updatedContributed =
            GraphQLConfigContributor.EP_NAME.extensionList.flatMap { it.contributeConfigs(project) }.toSet()

        return if (prevContributed != updatedContributed) {
            if (contributedConfigs.compareAndSet(prevSnapshot, updatedContributed.associateBy { it.dir })) {
                LOG.info("contributed configs changed")
                LOG.debug { "contributed configs: new=$updatedContributed, previous=$prevContributed" }
                true
            } else {
                // concurrent modification, let's try again later
                scheduleConfigurationReload()
                false
            }
        } else {
            false
        }
    }

    private fun notifyConfigurationChanged() {
        invokeLater(ModalityState.defaultModalityState()) {
            initialized = true

            modificationTracker.incModificationCount()
            scopeDependency.update()
            PsiManager.getInstance(project).dropPsiCaches()

            DaemonCodeAnalyzer.getInstance(project).restart()
            project.messageBus.syncPublisher(GraphQLConfigListener.TOPIC).onConfigurationChanged()
        }
    }

    private fun saveModifiedDocuments(files: Collection<VirtualFile>) {
        if (files.isEmpty()) return
        val fileDocumentManager = FileDocumentManager.getInstance()
        val anyFileModified = runReadAction { files.any { fileDocumentManager.isFileModified(it) } }
        if (anyFileModified) {
            WriteAction.runAndWait<Throwable> {
                files
                    .asSequence()
                    .filter { it.isValid }
                    .mapNotNull { fileDocumentManager.getDocument(it) }
                    .forEach { fileDocumentManager.saveDocument(it) }
            }
        }
    }

    /**
     * Cached inside of [configFiles], do not use directly.
     */
    @RequiresBackgroundThread
    private fun queryAllConfigFiles(): Set<VirtualFile> =
        ReadAction.nonBlocking<Set<VirtualFile>> {
            val processor = CommonProcessors.CollectUniquesProcessor<VirtualFile>()
            FilenameIndex.processFilesByNames(
                CONFIG_NAMES, true, GlobalSearchScope.projectScope(project), null, processor
            )
            processor.results.toSet()
        }
            .inSmartMode(project)
            .expireWith(this)
            .executeSynchronously()

    private data class ConfigEntry(
        val config: GraphQLConfig? = null,
        val timeStamp: Long = -1,
        val status: GraphQLConfigLoader.Status,
    )

    override fun getModificationCount(): Long = modificationTracker.modificationCount

    override fun dispose() {
    }
}

private data class GraphQLConfigOverride(val file: VirtualFile, val projectName: String?)
