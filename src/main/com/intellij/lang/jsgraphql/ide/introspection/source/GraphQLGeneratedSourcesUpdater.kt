package com.intellij.lang.jsgraphql.ide.introspection.source

import com.intellij.json.JsonFileType
import com.intellij.lang.jsgraphql.executeOnPooledThread
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigListener
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.introspection.isJsonSchemaCandidate
import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourcesManager.Companion.generatedSdlDirPath
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbService
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
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Alarm
import com.intellij.util.concurrency.SequentialTaskExecutor
import java.util.*


@Service(Service.Level.PROJECT)
class GraphQLGeneratedSourcesUpdater(private val project: Project) : Disposable, AsyncFileListener, GraphQLConfigListener {
  companion object {
    @JvmStatic
    fun getInstance(project: Project): GraphQLGeneratedSourcesUpdater = project.service<GraphQLGeneratedSourcesUpdater>()

    private const val REFRESH_DELAY = 500
  }

  private val queue = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)
  private val executor =
    SequentialTaskExecutor.createSequentialApplicationPoolExecutor("GraphQL Generated Source Updater")

  @Volatile
  private var jsonSchemaFiles: Set<VirtualFile> = emptySet()

  init {
    project.messageBus.connect(this).subscribe(GraphQLConfigListener.TOPIC, this)
    VirtualFileManager.getInstance().addAsyncFileListener(this, this)
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
        refreshJsonSchemaFiles()
      }
    }
  }

  override fun onConfigurationChanged() {
    refreshJsonSchemaFiles()
  }

  fun refreshJsonSchemaFiles() {
    if (project.isDisposed) return

    if (ApplicationManager.getApplication().isUnitTestMode) {
      DumbService.getInstance(project).smartInvokeLater { refreshJsonSchemaFilesSync() }
    }
    else {
      queue.cancelAllRequests()
      queue.addRequest({
                         ReadAction.nonBlocking(::findJsonSchemaCandidates)
                           .expireWith(this)
                           .inSmartMode(project)
                           .withDocumentsCommitted(project)
                           .finishOnUiThread(ModalityState.defaultModalityState(), ::updateCachedSchemas)
                           .submit(executor)
                       }, REFRESH_DELAY)
    }
  }

  private fun refreshJsonSchemaFilesSync() {
    if (project.isDisposed) return
    updateCachedSchemas(findJsonSchemaCandidates())
  }

  private fun findJsonSchemaCandidates(): Set<VirtualFile> {
    val schemaScope = GraphQLConfigProvider.getInstance(project)
                        .getAllConfigs()
                        .asSequence()
                        .flatMap { it.getProjects().values }
                        .map { it.schemaScope }
                        .reduceOrNull(GlobalSearchScope::union) ?: return emptySet()

    val fileDocumentManager = FileDocumentManager.getInstance()
    return FileTypeIndex.getFiles(JsonFileType.INSTANCE, schemaScope)
      .filter { isJsonSchemaCandidate(fileDocumentManager.getDocument(it)) }
      .toSet()
  }

  private fun updateCachedSchemas(schemas: Set<VirtualFile>) {
    val generatedSourcesManager = GraphQLGeneratedSourcesManager.getInstance(project)
    val prevSchemas = jsonSchemaFiles
    jsonSchemaFiles = Collections.unmodifiableSet(schemas)

    if (schemas.isNotEmpty()) {
      executeOnPooledThread {
        for (virtualFile in schemas) {
          generatedSourcesManager.requestGeneratedFile(virtualFile)
        }
      }
    }

    if (prevSchemas != schemas) {
      generatedSourcesManager.sourcesChanged()
    }
  }

  override fun dispose() {
    executor.shutdown()
  }
}
