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
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.checkCanceled
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
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XCollection
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import org.jetbrains.annotations.VisibleForTesting
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletionException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


@OptIn(FlowPreview::class)
@Service(Service.Level.PROJECT)
@State(name = "GraphQLGeneratedSources", storages = [Storage(value = StoragePathMacros.CACHE_FILE, roamingType = RoamingType.DISABLED)])
class GraphQLGeneratedSourcesManager(
  private val project: Project,
  @VisibleForTesting val coroutineScope: CoroutineScope,
) : Disposable,
    ModificationTracker,
    PersistentStateComponent<GraphQLGeneratedSourcesManager.GraphQLGeneratedSourceState> {

  companion object {
    private val LOG = logger<GraphQLGeneratedSourcesManager>()

    @JvmStatic
    fun getInstance(project: Project): GraphQLGeneratedSourcesManager = project.service<GraphQLGeneratedSourcesManager>()

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

  private val sdlGenerationDispatcher = Dispatchers.IO.limitedParallelism(1, "GraphQL SDL Generation")

  private val mapping = GeneratedEntriesMapping()
  private val pendingTasks = ConcurrentHashMap<Source, Job>()
  private val retries = ConcurrentHashMap<Source, AtomicInteger>()

  private val modificationTracker = SimpleModificationTracker()

  private val notificationFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  init {
    skipInTests {
      coroutineScope.launch {
        notificationFlow.debounce(NOTIFY_DELAY.milliseconds).collect {
          notifySourcesChanged()
        }
      }
    }
  }

  fun createGeneratedSourcesScope(): GlobalSearchScope {
    val dir = LocalFileSystem.getInstance().findFileByPath(generatedSdlDirPath)
              ?: return GlobalSearchScope.EMPTY_SCOPE
    return GlobalSearchScopes.directoryScope(project, dir, false)
  }

  suspend fun requestGeneratedFile(file: VirtualFile?): VirtualFile? {
    if (file == null || project.isDisposed) return null

    val source = Source.create(file) ?: return null
    val entry = mapping[source]
    if (entry?.output != null) {
      if (!readAction { entry.output.isValid }) {
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
    }?.takeIf { readAction { it.isValid } }
  }

  private fun addEntry(source: Source, entry: GeneratedEntry?) {
    if (entry == null) return

    mapping[source] = entry
    sourcesChanged()
  }

  private fun removeEntry(source: Source, entry: GeneratedEntry?) {
    if (entry == null) return

    mapping.remove(source, entry)
    sourcesChanged()
  }

  private fun startAsyncProcessing(source: Source) {
    pendingTasks.compute(source) { _, operation ->
      // let the ongoing operation finish first
      if (operation != null && !operation.isCompleted) {
        return@compute operation
      }

      scheduleTask(source).apply {
        invokeOnCompletion {
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

    val retry = retries.computeIfAbsent(source) { AtomicInteger() }.incrementAndGet() <= RETRIES_COUNT
    if (retry) {
      LOG.info("Retry GraphQL SDL generation: source=$source")

      coroutineScope.launch {
        delay(RETRY_DELAY.milliseconds)
        requestGeneratedFile(source.file)
      }
    }
    else {
      LOG.warn("Retry GraphQL SDL generation limit exceeded: source=$source")
    }
  }

  private fun scheduleTask(source: Source): Job {
    LOG.info("Scheduling SDL generation task: source=$source")

    return coroutineScope.launch(sdlGenerationDispatcher) {
      coroutineScope {
        try {
          val entry = generateEntryFromSource(source)
          processResult(source, entry)
          resetRetries(source)
        }
        catch (e: Exception) {
          if (!isCancellation(e)) {
            if (ApplicationManager.getApplication().isUnitTestMode) {
              LOG.error("GraphQL SDL generation failed: source=$source", e)
            }
            processResult(source, source.createErrorResult(e))
          }
          else {
            LOG.warn("GraphQL SDL generation cancelled: source=$source")
            scheduleRetry(source)
          }
        }
      }
    }
  }

  private suspend fun generateEntryFromSource(source: Source): GeneratedEntry {
    val sourceText = readAction { FileDocumentManager.getInstance().getDocument(source.file)?.text }
                     ?: throw FileNotFoundException("Unable to read file: ${source.file.path}")

    val introspection = GraphQLIntrospectionService.printIntrospectionAsGraphQL(project, sourceText)

    checkCanceled()
    val file = edtWriteAction {
      generatedSdlDirPath
        .let { VfsUtil.createDirectoryIfMissing(it) }
        ?.findOrCreateChildData(null, source.targetFileName)
    } ?: throw IOException("Unable to create file in $generatedSdlDirPath: ${source.targetFileName}")

    checkCanceled()
    edtWriteAction {
      val fileDocumentManager = FileDocumentManager.getInstance()
      if (fileDocumentManager.isFileModified(file)) {
        fileDocumentManager.reloadFiles(file)
      }

      VfsUtil.saveText(file, introspection)
      file.refresh(false, false)
    }

    checkCanceled()
    try {
      withTimeout(10.seconds) {
        reformat(file)
      }
    }
    catch (_: TimeoutCancellationException) {
      LOG.warn("GraphQL SDL generation reformatting timed out: $file")
    }
    return source.createResult(file)
  }

  private suspend fun reformat(file: VirtualFile) {
    val psiDocumentManager = PsiDocumentManager.getInstance(project)
    val fileDocumentManager = FileDocumentManager.getInstance()

    val document = readAndEdtWriteAction {
      ProgressManager.checkCanceled()
      val document = fileDocumentManager.getDocument(file)
      if (document == null || psiDocumentManager.isCommitted(document)) {
        value(document)
      }
      else {
        writeAction {
          psiDocumentManager.commitDocument(document)
          document
        }
      }
    } ?: return

    readAndEdtWriteAction {
      ProgressManager.checkCanceled()
      val psiFile = psiDocumentManager.getPsiFile(document) ?: return@readAndEdtWriteAction value(Unit)
      writeCommandAction(project, GraphQLBundle.message("graphql.command.name.reformat.generated.graphql.sdl")) {
        CodeStyleManager.getInstance(project).reformat(psiFile)
        Unit
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
      updateModificationTrackers()
      return
    }

    check(notificationFlow.tryEmit(Unit))
  }

  private suspend fun notifySourcesChanged() {
    if (project.isDisposed) return

    updateModificationTrackers()
    withContext(Dispatchers.EDT) {
      PsiManager.getInstance(project).dropPsiCaches()
      EditorNotifications.getInstance(project).updateAllNotifications()
    }
    DaemonCodeAnalyzer.getInstance(project).restart()
  }

  private fun updateModificationTrackers() {
    modificationTracker.incModificationCount()
    GraphQLScopeDependency.getInstance(project).update()
    GraphQLSchemaContentTracker.getInstance(project).update()
  }

  fun reset() {
    mapping.clear()
    retries.clear()
    sourcesChanged()
  }

  fun isSourceForGeneratedFile(file: VirtualFile?): Boolean {
    val source = Source.create(file) ?: return false
    return mapping[source] != null
  }

  fun isGeneratedFile(file: VirtualFile?): Boolean {
    if (file == null) return false

    if (mapping[file] != null) {
      return true
    }

    // need this for cases when the file mapping is not registered, but the file already exists is in the generated directory
    return isInGeneratedSourcesRoot(file)
  }

  fun getSourceFile(generatedFile: VirtualFile?): VirtualFile? {
    return generatedFile?.let { mapping[it] }?.file?.takeIf { it.isValid }
  }

  fun getErrorForSource(sourceFile: VirtualFile?): Throwable? {
    val source = Source.create(sourceFile) ?: return null
    return mapping[source]
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

    val timeStamp: Long = file.timeStamp

    fun isEntryOutdated(entry: GeneratedEntry): Boolean = timeStamp != entry.timeStamp

    fun createResult(file: VirtualFile) =
      GeneratedEntry(RequestStatus.SUCCESS, timeStamp, file, null)

    fun createErrorResult(e: Throwable? = null) =
      GeneratedEntry(
        RequestStatus.ERROR, timeStamp, null,
        when (e) {
          is CompletionException -> e.cause ?: e
          else -> e
        }
      )

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
    val items = mapping.getAll().mapNotNull { (source, entry) ->
      val sourcePath = source.file.takeIf { it.isValid }?.path ?: return@mapNotNull null
      val outputPath = entry.output?.takeIf { it.isValid }?.path ?: return@mapNotNull null
      GraphQLGeneratedSourceStateItem(entry.status, entry.timeStamp, sourcePath, outputPath)
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
    }?.toMap() ?: emptyMap()

    if (items.isNotEmpty()) {
      mapping.replaceAll(items)

      coroutineScope.launch {
        items.forEach {
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

  private class GeneratedEntriesMapping {
    private val lock = ReentrantLock()
    private val generatedFiles: MutableMap<Source, GeneratedEntry> = mutableMapOf() // lock
    private val reverseMappings: MutableMap<VirtualFile, Source> = mutableMapOf() // lock

    operator fun get(source: Source): GeneratedEntry? {
      return lock.withLock { generatedFiles[source] }
    }

    operator fun get(file: VirtualFile): Source? {
      return lock.withLock { reverseMappings[file] }
    }

    operator fun set(source: Source, entry: GeneratedEntry) {
      lock.withLock {
        generatedFiles[source] = entry
        entry.output?.let { reverseMappings[it] = source }
      }
    }

    fun remove(source: Source, entry: GeneratedEntry) {
      lock.withLock {
        generatedFiles.remove(source, entry)
        entry.output?.let { reverseMappings.remove(it, source) }
      }
    }

    fun clear() {
      lock.withLock {
        generatedFiles.clear()
        reverseMappings.clear()
      }
    }

    fun getAll(): Map<Source, GeneratedEntry> {
      return lock.withLock { generatedFiles.toMap() }
    }

    fun replaceAll(entries: Map<Source, GeneratedEntry>) {
      lock.withLock {
        generatedFiles.clear()
        reverseMappings.clear()

        entries.forEach { (source, entry) ->
          generatedFiles[source] = entry
          entry.output?.let { reverseMappings[it] = source }
        }
      }
    }
  }
}
