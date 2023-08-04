package com.intellij.lang.jsgraphql.ide.introspection.source

import com.google.common.hash.Hashing
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.json.JsonFileType
import com.intellij.lang.jsgraphql.*
import com.intellij.lang.jsgraphql.ide.config.CONFIG_NAMES
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLIntrospectionService
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeDependency
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaContentTracker
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.*
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopes
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.EditorNotifications
import com.intellij.util.Alarm
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XCollection
import java.io.FileNotFoundException
import java.nio.charset.StandardCharsets
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write


@Service(Service.Level.PROJECT)
@State(name = "GraphQLGeneratedSources", storages = [Storage(value = StoragePathMacros.CACHE_FILE, roamingType = RoamingType.DISABLED)])
class GraphQLGeneratedSourcesManager(
  private val project: Project,
) : Disposable,
    ModificationTracker,
    PersistentStateComponent<GraphQLGeneratedSourcesManager.GraphQLGeneratedSourceState> {

  companion object {
    private val LOG = logger<GraphQLGeneratedSourcesManager>()

    @JvmStatic
    fun getInstance(project: Project) = project.service<GraphQLGeneratedSourcesManager>()

    private const val EXECUTION_TIMEOUT = 10000L
    private const val NOTIFY_DELAY = 500

    private const val RETRY_DELAY = 5000
    private const val RETRIES_COUNT = 5

    private val IGNORED_INTROSPECTION_FILES = setOf(*CONFIG_NAMES.toTypedArray(), "package.json")

    private const val GRAPHQL_SDL_DIR = "sdl"

    @JvmStatic
    val generatedSdlDirPath: String
      get() = FileUtil.join(PathManager.getConfigPath(), GRAPHQL_CACHE_DIR_NAME, GRAPHQL_SDL_DIR)

    private fun isInGeneratedSourcesRoot(file: VirtualFile?) =
      file != null &&
      file.fileType == GraphQLFileType.INSTANCE &&
      file.parent?.name == GRAPHQL_SDL_DIR &&
      FileUtil.isAncestor(generatedSdlDirPath, file.path, true)
  }

  private val lock = ReentrantReadWriteLock()
  private val generatedFiles: MutableMap<Source, GeneratedEntry> = mutableMapOf() // lock
  private val reverseMappings: MutableMap<VirtualFile, Source> = mutableMapOf() // lock

  private val pendingTasks = ConcurrentHashMap<Source, CompletableFuture<GeneratedEntry>>()
  private val retries = ConcurrentHashMap<Source, AtomicInteger>()

  private val modificationTracker = SimpleModificationTracker()

  private val notifyChangedAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)
  private val retryAlarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)

  private val executor = if (ApplicationManager.getApplication().isUnitTestMode) {
    inEdt()
  }
  else {
    AppExecutorUtil.createBoundedApplicationPoolExecutor(
      "GraphQL Source File Generation",
      AppExecutorUtil.getAppExecutorService(),
      1,
      this
    )
  }

  fun createGeneratedSourcesScope(): GlobalSearchScope {
    val dir = LocalFileSystem.getInstance().findFileByPath(generatedSdlDirPath)
              ?: return GlobalSearchScope.EMPTY_SCOPE
    return GlobalSearchScopes.directoryScope(project, dir, false)
  }

  fun requestGeneratedFile(file: VirtualFile?): VirtualFile? {
    if (file == null || project.isDisposed) return null

    val source = Source.create(file) ?: return null
    val entry = lock.read { generatedFiles[source] }
    if (entry?.output != null) {
      if (!entry.output.isValid) {
        removeEntry(source, entry)
        startAsyncProcessing(source)
        return null
      }
    }

    if (entry == null || source.isEntryOutdated(entry)) {
      startAsyncProcessing(source)
    }

    return when (entry?.status) {
      RequestStatus.SUCCESS -> entry.output
      else -> null
    }?.takeIf { it.isValid }
  }

  private fun addEntry(source: Source, entry: GeneratedEntry?) {
    if (entry == null) return

    lock.write {
      generatedFiles[source] = entry
      entry.output?.let { reverseMappings[it] = source }
    }

    sourcesChanged()
  }

  private fun removeEntry(source: Source, entry: GeneratedEntry?) {
    if (entry == null) return

    lock.write {
      generatedFiles.remove(source, entry)
      entry.output?.let { reverseMappings.remove(it, source) }
    }

    sourcesChanged()
  }

  private fun startAsyncProcessing(source: Source) {
    pendingTasks.compute(source) { _, running ->
      if (running != null && !running.isDone) {
        return@compute running
      }

      scheduleTask(source)
        .orTimeout(EXECUTION_TIMEOUT, TimeUnit.MILLISECONDS)
        .whenComplete { entry, error ->
          if (!isCancellation(error) && error !is TimeoutException) {
            if (error != null) {
              processResult(source, source.createErrorResult(error))
            }
            else {
              processResult(source, entry)
              resetRetries(source)
            }
          }
          else {
            LOG.warn("GraphQL SDL generation cancelled: source=$source, isTimeout=${error is TimeoutException}")
            scheduleRetry(source)
          }
        }
        .apply {
          whenComplete { _, _ ->
            pendingTasks.remove(source, this)
          }
        }
    }
  }

  private fun resetRetries(source: Source) {
    retries.computeIfAbsent(source) { AtomicInteger() }.set(0)
  }

  private fun scheduleRetry(source: Source) {
    if (project.isDisposed || ApplicationManager.getApplication().isUnitTestMode) return

    val retry =
      retries.computeIfAbsent(source) { AtomicInteger() }.incrementAndGet() <= RETRIES_COUNT
    if (retry) {
      LOG.info("Retry GraphQL SDL generation: source=$source")
      retryAlarm.addRequest({ requestGeneratedFile(source.file) }, RETRY_DELAY)
    }
    else {
      LOG.warn("Retry GraphQL SDL generation limit exceeded: source=$source")
    }
  }

  private fun scheduleTask(source: Source): CompletableFuture<GeneratedEntry> {
    LOG.info("Scheduling SDL generation task: source=$source")

    return CompletableFuture
      .supplyAsync({
                     val sourceText = runReadAction { FileDocumentManager.getInstance().getDocument(source.file)?.text }
                                      ?: throw FileNotFoundException("Unable to read file: ${source.file.path}")

                     GraphQLIntrospectionService.getInstance(project).printIntrospectionAsGraphQL(sourceText)
                   }, executor)
      .thenApplyAsync({ introspection ->
                        if (project.isDisposed) throw ProcessCanceledException()

                        val file = generatedSdlDirPath
                          .let { VfsUtil.createDirectories(it) }
                          .findOrCreateChildData(null, source.targetFileName)

                        val fileDocumentManager = FileDocumentManager.getInstance()
                        if (fileDocumentManager.isFileModified(file)) {
                          fileDocumentManager.reloadFiles(file)
                        }

                        VfsUtil.saveText(file, introspection)
                        file.refresh(false, false)

                        reformatLater(file)
                        source.createResult(file)
                      }, inWriteAction(ModalityState.defaultModalityState()))
  }

  private fun reformatLater(file: VirtualFile) {
    invokeLater {
      if (project.isDisposed) return@invokeLater

      WriteCommandAction.runWriteCommandAction(project) {
        val psiDocumentManager = PsiDocumentManager.getInstance(project)
        val fileDocumentManager = FileDocumentManager.getInstance()
        val document = fileDocumentManager.getDocument(file)
        if (document != null) {
          psiDocumentManager.commitDocument(document)
          val psiFile = psiDocumentManager.getPsiFile(document)
          if (psiFile != null) {
            CodeStyleManager.getInstance(project).reformat(psiFile)
          }
        }
      }
    }
  }

  private fun processResult(source: Source, result: GeneratedEntry?) {
    if (result == null) {
      LOG.warn("GraphQL SDL generation result is null: $source")
      return
    }

    if (result.exception != null) {
      LOG.info("Error during GraphQL SDL generation: $source", result.exception)
    }
    else {
      LOG.info("GraphQL SDL generation finished: $source")
    }

    addEntry(source, result)
  }

  fun sourcesChanged() {
    if (project.isDisposed) return

    if (ApplicationManager.getApplication().isUnitTestMode) {
      invokeLater { notifySourcesChanged() }
    }
    else {
      notifyChangedAlarm.cancelAllRequests()
      notifyChangedAlarm.addRequest(::notifySourcesChanged, NOTIFY_DELAY)
    }
  }

  private fun notifySourcesChanged() {
    if (project.isDisposed) return

    modificationTracker.incModificationCount()
    GraphQLScopeDependency.getInstance(project).update()
    PsiManager.getInstance(project).dropPsiCaches()
    GraphQLSchemaContentTracker.getInstance(project).schemaChanged()

    DaemonCodeAnalyzer.getInstance(project).restart()
    EditorNotifications.getInstance(project).updateAllNotifications()
  }

  private fun Source.createResult(file: VirtualFile) =
    GeneratedEntry(RequestStatus.SUCCESS, timeStamp, file, null)

  private fun Source.createErrorResult(e: Throwable? = null) =
    GeneratedEntry(
      RequestStatus.ERROR, timeStamp, null, when (e) {
      is CompletionException -> e.cause ?: e
      else -> e
    }
    )

  fun reset() {
    lock.write {
      generatedFiles.clear()
      reverseMappings.clear()
    }

    retries.clear()
    sourcesChanged()
  }

  fun isSourceForGeneratedFile(file: VirtualFile?): Boolean {
    val source = Source.create(file) ?: return false
    return lock.read { generatedFiles[source] != null }
  }

  fun isGeneratedFile(file: VirtualFile?): Boolean {
    if (file == null) return false

    if (lock.read { reverseMappings.containsKey(file) }) {
      return true
    }

    // need this for cases when the file mapping is not registered, but the file already exists is in the generated directory
    return isInGeneratedSourcesRoot(file)
  }

  fun getSourceFile(generatedFile: VirtualFile?): VirtualFile? {
    return generatedFile?.let { lock.read { reverseMappings[it] } }?.file?.takeIf { it.isValid }
  }

  fun getErrorForSource(sourceFile: VirtualFile?): Throwable? {
    val source = Source.create(sourceFile) ?: return null
    return lock.read { generatedFiles[source] }
      ?.takeIf { it.status == RequestStatus.ERROR }
      ?.exception
  }

  override fun getModificationCount(): Long = modificationTracker.modificationCount

  override fun dispose() {
  }

  private class Source(val file: VirtualFile) {

    val targetFileName: String
      get() {
        val fileName = Hashing.sha256()
          .hashString(FileUtil.toSystemIndependentName(file.path), StandardCharsets.UTF_8)
          .toString()
        return "$fileName.graphql"
      }

    val timeStamp: Long = runReadAction { file.timeStamp }

    fun isEntryOutdated(entry: GeneratedEntry): Boolean = timeStamp != entry.timeStamp

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as Source

      return file == other.file
    }

    override fun hashCode(): Int {
      return file.hashCode()
    }

    override fun toString(): String {
      return "Source(file=$file)"
    }

    companion object {
      fun create(file: VirtualFile?): Source? {
        if (file == null ||
            !file.isValid ||
            file is LightVirtualFile ||
            file.name in IGNORED_INTROSPECTION_FILES ||
            file.fileType != JsonFileType.INSTANCE
        ) {
          return null
        }

        return Source(file)
      }
    }
  }

  private data class GeneratedEntry(
    val status: RequestStatus,
    val timeStamp: Long,
    val output: VirtualFile?,
    val exception: Throwable?,
  )

  enum class RequestStatus {
    SUCCESS,
    ERROR,
  }

  override fun getState(): GraphQLGeneratedSourceState {
    val items = lock.read {
      generatedFiles.mapNotNull { (source, entry) ->
        val sourcePath = source.file.takeIf { it.isValid }?.path ?: return@mapNotNull null
        val outputPath = entry.output?.takeIf { it.isValid }?.path ?: return@mapNotNull null
        GraphQLGeneratedSourceStateItem(entry.status, entry.timeStamp, sourcePath, outputPath)
      }
    }

    return GraphQLGeneratedSourceState(items)
  }

  override fun loadState(state: GraphQLGeneratedSourceState) {
    val items = state.items?.mapNotNull {
      val status = it.status
      val sourcePath = it.sourcePath
      val outputPath = it.outputPath

      if (status == null ||
          sourcePath.isNullOrEmpty() ||
          outputPath.isNullOrEmpty()
      ) {
        return@mapNotNull null
      }

      val sourceFile = LocalFileSystem.getInstance().findFileByPath(sourcePath)
      val outputFile = LocalFileSystem.getInstance().findFileByPath(outputPath)
      if (sourceFile == null || outputFile == null) {
        return@mapNotNull null
      }

      val source = Source.create(sourceFile) ?: return@mapNotNull null
      source to GeneratedEntry(status, it.timeStamp, outputFile, null)
    } ?: emptyList()

    if (items.isNotEmpty()) {
      lock.write {
        generatedFiles.clear()
        reverseMappings.clear()
        items.forEach { (source, entry) ->
          generatedFiles[source] = entry
          entry.output?.let { reverseMappings[it] = source }
        }
      }

      ApplicationManager.getApplication().executeOnPooledThread {
        generatedFiles.forEach {
          requestGeneratedFile(it.key.file)
        }
      }
    }
  }

  data class GraphQLGeneratedSourceState(
    @get:XCollection(propertyElementName = "sources")
    var items: List<GraphQLGeneratedSourceStateItem>? = emptyList(),
  )

  @Tag("source")
  data class GraphQLGeneratedSourceStateItem(
    @get:Attribute
    var status: RequestStatus? = null,
    @get:Attribute
    var timeStamp: Long = 0,
    @get:Tag
    var sourcePath: String? = null,
    @get:Tag
    var outputPath: String? = null,
  )
}
