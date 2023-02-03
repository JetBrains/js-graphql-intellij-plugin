package com.intellij.lang.jsgraphql.ide.config

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLConfigLoader
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLResolveUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.*
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.util.BackgroundTaskUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiUtil
import com.intellij.util.Alarm
import com.intellij.util.CommonProcessors
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import com.intellij.util.concurrency.annotations.RequiresReadLock
import java.util.concurrent.ConcurrentHashMap

private const val CONFIG_RELOAD_TIMEOUT = 3000

@Service
class GraphQLConfigProvider(private val project: Project) : Disposable, ModificationTracker {
    companion object {
        private val LOG = logger<GraphQLConfigProvider>()

        private val CONFIG_FILE_KEY = Key.create<CachedValue<VirtualFile?>>("graphql.config.file")
        private val CONFIG_FILE_IN_DIRECTORY_KEY = Key.create<CachedValue<VirtualFile?>>("graphql.config.file.inside.dir")

        @JvmStatic
        fun getInstance(project: Project) = project.service<GraphQLConfigProvider>()
    }

    private val alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)

    /**
     * Use this service as a dependency for tracking content changes in configuration files.
     * Computations resolving config locations usually
     * should additionally depend on [com.intellij.openapi.vfs.VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS].
     */
    private val modificationTracker = SimpleModificationTracker()

    private val configData: MutableMap<VirtualFile, ConfigEntry> = ConcurrentHashMap()

    private val configFiles: CachedValue<Collection<VirtualFile>> = CachedValuesManager.getManager(project).createCachedValue {
        CachedValueProvider.Result.create(
            queryAllConfigFiles(),
            VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
            ProjectRootManager.getInstance(project)
        )
    }

    @RequiresReadLock
    fun resolveConfig(context: PsiFile): GraphQLConfig? =
        findClosestConfigFile(context)?.let { getConfig(it) }

    @RequiresReadLock
    fun resolveProjectConfig(context: PsiFile): GraphQLProjectConfig? =
        resolveConfig(context)?.matchProject(context)

    @RequiresReadLock
    fun resolveConfig(file: VirtualFile): GraphQLConfig? =
        PsiManager.getInstance(project).findFile(file)?.let { resolveConfig(it) }

    @RequiresReadLock
    fun resolveProjectConfig(virtualFile: VirtualFile): GraphQLProjectConfig? =
        PsiManager.getInstance(project).findFile(virtualFile)?.let { resolveProjectConfig(it) }

    fun getConfig(configFile: VirtualFile?): GraphQLConfig? {
        return configData[configFile]?.config
    }

    fun getAllConfigs(): List<GraphQLConfig> {
        return configData.mapNotNull { it.value.config }
    }

    val hasConfigurationFiles
        get() = configData.isNotEmpty()

    @RequiresReadLock
    fun findClosestConfigFile(context: PsiFile): VirtualFile? {
        return CachedValuesManager.getCachedValue(context, CONFIG_FILE_KEY) {
            val configFile = findConfigFileInParents(context)
            CachedValueProvider.Result.create(configFile, context, this, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
        }
    }

    private fun findConfigFileInParents(file: PsiFile): VirtualFile? {
        // TODO: better starting point including light files, injections and scratches
        val initial = PsiUtil.getVirtualFile(file) ?: return null
        var result: VirtualFile? = null
        GraphQLResolveUtil.processDirectoriesUpToContentRoot(file.project, initial) { dir ->
            val configFile = findConfigFileInDirectory(dir)
            if (configFile != null) {
                result = configFile
                false
            } else {
                true
            }
        }
        return result
    }

    @RequiresReadLock
    fun findConfigFileInDirectory(dir: VirtualFile): VirtualFile? {
        if (!dir.isDirectory) return null

        return CachedValuesManager.getManager(project).getCachedValue(dir, CONFIG_FILE_IN_DIRECTORY_KEY, {
            val candidates = dir.children.filter { it.name in CONFIG_NAMES }.associateBy { it.name }
            val configFile = CONFIG_NAMES.find { it in candidates }?.let { candidates[it] }
            CachedValueProvider.Result.create(configFile, this, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
        }, false)
    }

    @JvmOverloads
    fun invalidate(configFile: VirtualFile? = null) {
        if (configFile != null) {
            configData.remove(configFile)
        } else {
            configData.clear()
        }

        scheduleConfigurationReload()
    }

    fun scheduleConfigurationReload() {
        if (ApplicationManager.getApplication().isUnitTestMode) {
            invokeLater { reload() }
        } else {
            alarm.cancelAllRequests()
            alarm.addRequest({
                BackgroundTaskUtil.runUnderDisposeAwareIndicator(this, ::reload)
            }, CONFIG_RELOAD_TIMEOUT)
        }
    }

    private fun reload() {
        val files = configFiles.value
        saveModifiedDocuments(files)

        val loader = GraphQLConfigLoader.getInstance(project)
        var hasChanged = configData.keys.removeIf { !it.isValid }

        for (file in files) {
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

            val rawConfig = loader.load(file)
            val entry = if (rawConfig == null) {
                ConfigEntry(null, timeStamp)
            } else {
                ConfigEntry(GraphQLConfig(project, dir, file, rawConfig), timeStamp)
            }

            hasChanged = true
            if (cached == null) {
                configData.putIfAbsent(file, entry)
            } else {
                configData.replace(file, cached, entry)
            }
        }

        if (hasChanged) {
            notifyConfigurationChanged()
        }
    }

    private fun notifyConfigurationChanged() {
        invokeLater(ModalityState.NON_MODAL) {
            modificationTracker.incModificationCount()
            PsiManager.getInstance(project).dropPsiCaches()
            DaemonCodeAnalyzer.getInstance(project).restart()

            project.messageBus.syncPublisher(GraphQLConfigListener.TOPIC).onConfigurationChanged()
        }
    }

    private fun saveModifiedDocuments(files: Collection<VirtualFile>) {
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
    private fun queryAllConfigFiles(): Collection<VirtualFile> =
        ReadAction.nonBlocking<Collection<VirtualFile>> {
            val processor = CommonProcessors.CollectUniquesProcessor<VirtualFile>()
            FilenameIndex.processFilesByNames(CONFIG_NAMES, true, GlobalSearchScope.projectScope(project), null, processor)
            processor.results
        }
            .inSmartMode(project)
            .expireWith(this)
            .executeSynchronously()

    private data class ConfigEntry(
        val config: GraphQLConfig? = null,
        val timeStamp: Long = -1,
    )

    override fun getModificationCount(): Long = modificationTracker.modificationCount

    override fun dispose() {
    }
}
