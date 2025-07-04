package com.intellij.lang.jsgraphql.ide.introspection.source

import com.intellij.json.JsonFileType
import com.intellij.lang.jsgraphql.emitOrRunImmediateInTests
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigListener
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.introspection.isJsonSchemaCandidate
import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourcesManager.Companion.generatedSdlDirPath
import com.intellij.lang.jsgraphql.skipInTests
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.AsyncFileListener.ChangeApplier
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.util.*
import kotlin.time.Duration.Companion.milliseconds


@OptIn(FlowPreview::class)
@Service(Service.Level.PROJECT)
class GraphQLGeneratedSourcesUpdater(private val project: Project, coroutineScope: CoroutineScope) : Disposable, AsyncFileListener, GraphQLConfigListener {
  companion object {
    @JvmStatic
    fun getInstance(project: Project): GraphQLGeneratedSourcesUpdater = project.service()

    private const val REFRESH_DELAY = 500
  }

  private val jsonSchemaSyncRequests = MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  @Volatile
  private var jsonSchemaFiles: Set<VirtualFile> = emptySet()

  init {
    project.messageBus.connect(this).subscribe(GraphQLConfigListener.TOPIC, this)
    VirtualFileManager.getInstance().addAsyncFileListener(this, this)

    skipInTests {
      coroutineScope.launch {
        jsonSchemaSyncRequests.debounce(REFRESH_DELAY.milliseconds).collect {
          runJsonSchemaFilesGeneration()
        }
      }
    }
  }

  override fun prepareChange(events: MutableList<out VFileEvent>): ChangeApplier? {
    val fileIndex = ProjectRootManager.getInstance(project).fileIndex
    var changed = false

    for (event in events) {
      if (event is VFileCreateEvent) {
        if (FileUtil.extensionEquals(event.childName, JsonFileType.DEFAULT_EXTENSION)) {
          changed = true
          break
        }
        continue
      }

      val file = event.file ?: continue
      if (!fileIndex.isInProject(file) && !FileUtil.isAncestor(generatedSdlDirPath, file.path, false)) continue

      if (event is VFileDeleteEvent) {
        // regenerate GraphQL SDLs from cache directory on deletion
        if (file.isDirectory && FileUtil.pathsEqual(file.path, generatedSdlDirPath) ||
            FileUtil.pathsEqual(file.parent?.path, generatedSdlDirPath)
        ) {
          changed = true
          break
        }
      }
      if (file in jsonSchemaFiles) {
        changed = true
        break
      }
      if (file.isDirectory && event !is VFileDeleteEvent) {
        if (file.children.any { it in jsonSchemaFiles || it.fileType == JsonFileType.INSTANCE }) {
          changed = true
          break
        }
      }
    }

    if (!changed) return null

    return object : ChangeApplier {
      override fun afterVfsChange() {
        scheduleJsonSchemaGeneration()
      }
    }
  }

  override fun onConfigurationChanged() {
    scheduleJsonSchemaGeneration()
  }

  fun scheduleJsonSchemaGeneration() {
    if (project.isDisposed) return

    emitOrRunImmediateInTests(jsonSchemaSyncRequests, Unit) {
      runWithModalProgressBlocking(project, "") {
        runJsonSchemaFilesGeneration()
      }
    }
  }

  @RequiresBackgroundThread
  private suspend fun runJsonSchemaFilesGeneration() {
    if (project.isDisposed) return
    updateCachedSchemas(findJsonSchemaCandidates())
  }

  private suspend fun findJsonSchemaCandidates(): Set<VirtualFile> {
    val schemaScope = readAction {
      GraphQLConfigProvider.getInstance(project)
        .getAllConfigs()
        .asSequence()
        .flatMap { it.getProjects().values }
        .map { it.schemaScope }
        .reduceOrNull(GlobalSearchScope::union)
    } ?: return emptySet()

    val fileDocumentManager = FileDocumentManager.getInstance()
    return smartReadAction(project) { FileTypeIndex.getFiles(JsonFileType.INSTANCE, schemaScope) }
      .filter { readAction { isJsonSchemaCandidate(fileDocumentManager.getDocument(it)) } }
      .toSet()
  }

  private suspend fun updateCachedSchemas(newSchemas: Set<VirtualFile>) {
    val generatedSourcesManager = GraphQLGeneratedSourcesManager.getInstance(project)
    val prevSchemas = jsonSchemaFiles
    jsonSchemaFiles = Collections.unmodifiableSet(newSchemas)

    for (virtualFile in newSchemas) {
      generatedSourcesManager.requestGeneratedFile(virtualFile)
    }

    if (prevSchemas != newSchemas) {
      generatedSourcesManager.sourcesChanged()
    }
  }

  override fun dispose() {
  }
}
