package com.intellij.lang.jsgraphql.ide.config

import com.intellij.lang.jsgraphql.skipInTests
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds


/**
 * Watches for changes in the GraphQL config files and saves them on disk after a small delay
 * to trigger the VFS refresh and start reloading the config.
 */
@OptIn(FlowPreview::class)
@Service(Service.Level.PROJECT)
class GraphQLConfigWatcher(private val project: Project, coroutineScope: CoroutineScope) : Disposable {

  companion object {
    @JvmStatic
    fun getInstance(project: Project): GraphQLConfigWatcher = project.service()

    private const val SAVE_DOCUMENTS_TIMEOUT = 2000
  }

  private val documentsSaveEvents = MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
  private val documentsToSave = ConcurrentHashMap.newKeySet<WatchedFile>()

  init {
    val eventMulticaster = EditorFactory.getInstance().eventMulticaster
    eventMulticaster.addDocumentListener(object : DocumentListener {
      override fun documentChanged(event: DocumentEvent) {
        val virtualFile = FileDocumentManager.getInstance().getFile(event.document)
        if (virtualFile != null && virtualFile !is LightVirtualFile && virtualFile.name in CONFIG_NAMES) {
          documentsToSave.add(WatchedFile(virtualFile, event.document))
          scheduleDocumentSave()
        }
      }
    }, this)

    skipInTests {
      coroutineScope.launch {
        documentsSaveEvents.debounce(SAVE_DOCUMENTS_TIMEOUT.milliseconds).collect {
          saveDocuments()
        }
      }
    }
  }

  private fun scheduleDocumentSave() {
    if (project.isDisposed) return
    if (ApplicationManager.getApplication().isUnitTestMode) return

    check(documentsSaveEvents.tryEmit(Unit))
  }

  private suspend fun saveDocuments() {
    if (documentsToSave.isEmpty()) {
      return
    }

    edtWriteAction {
      val documentManager = FileDocumentManager.getInstance()
      val documents = HashSet(documentsToSave)
        .also { documentsToSave.removeAll(it) }
        .filter { it.file.isValid }
        .map { it.document }

      documents.forEach {
        ProgressManager.checkCanceled()
        documentManager.saveDocument(it)
      }
    }
  }

  private data class WatchedFile(val file: VirtualFile, val document: Document)

  override fun dispose() {
  }
}
