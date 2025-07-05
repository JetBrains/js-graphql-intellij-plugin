package com.intellij.lang.jsgraphql.ide.introspection

import com.intellij.ide.actions.CreateFileAction
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.GraphQLSettings.Companion.getSettings
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLIntrospectionService.IntrospectionOutput
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLIntrospectionService.IntrospectionOutputFormat
import com.intellij.lang.jsgraphql.ide.notifications.GRAPHQL_NOTIFICATION_GROUP_ID
import com.intellij.lang.jsgraphql.ide.notifications.addShowQueryErrorDetailsAction
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.readAndEdtWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.file.PsiDirectoryFactory
import com.intellij.util.concurrency.annotations.RequiresWriteLock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

@Service(Service.Level.PROJECT)
internal class GraphQLIntrospectionSchemaWriter(private val project: Project) {
  companion object {
    private val LOG = logger<GraphQLIntrospectionSchemaWriter>()

    private val SKIP_FORMATTING_KEY = Key.create<Boolean>("graphql.introspection.skip.formatting")

    fun getInstance(project: Project): GraphQLIntrospectionSchemaWriter = project.service()
  }

  /**
   * This method could take a very long time to execute, since a schema might have a size of 260k lines (e.g., Atlassian's schema).
   */
  suspend fun createOrUpdateIntrospectionFile(output: IntrospectionOutput, dir: VirtualFile, fileName: String) {
    val header = when (output.format) {
      IntrospectionOutputFormat.SDL -> "# This file was generated. Do not edit manually.\n\n"
      IntrospectionOutputFormat.JSON -> ""
    }

    val fileDocumentManager = FileDocumentManager.getInstance()
    val psiDocumentManager = PsiDocumentManager.getInstance(project)

    try {
      val outputFile = edtWriteAction {
        createOrUpdateSchemaFile(project, dir, FileUtil.toSystemIndependentName(fileName))
      }

      val document = readAction { fileDocumentManager.getDocument(outputFile) }
                     ?: throw IOException("Unable to get document for created introspection file: $outputFile")

      edtWriteAction {
        document.setText(StringUtil.convertLineSeparators(header + output.schemaText))
        psiDocumentManager.commitDocument(document)
      }

      reformatDocumentIfNeeded(psiDocumentManager, fileDocumentManager, outputFile, document, dir)
      openSchemaInEditor(project, outputFile)
    }
    catch (e: ProcessCanceledException) {
      throw e
    }
    catch (e: IOException) {
      LOG.info(e)
      val notification = Notification(
        GRAPHQL_NOTIFICATION_GROUP_ID,
        GraphQLBundle.message("graphql.notification.error.title"),
        GraphQLBundle.message("graphql.notification.unable.to.create.file", fileName, dir.path),
        NotificationType.ERROR
      )
      addShowQueryErrorDetailsAction(project, notification, e)
      Notifications.Bus.notify(notification)
    }
    catch (e: Exception) {
      LOG.error(e)
    }
  }

  private suspend fun reformatDocumentIfNeeded(
    psiDocumentManager: PsiDocumentManager,
    fileDocumentManager: FileDocumentManager,
    outputFile: VirtualFile,
    document: Document,
    dir: VirtualFile,
  ) {
    if (outputFile.getUserData(SKIP_FORMATTING_KEY) != true) {
      withTimeoutOrNull(3.seconds) {
        readAndEdtWriteAction {
          val psiFile = psiDocumentManager.getPsiFile(document)
          if (psiFile != null) {
            writeAction {
              CodeStyleManager.getInstance(project).reformat(psiFile);

              ProgressManager.checkCanceled()
              psiDocumentManager.commitDocument(document);
              fileDocumentManager.saveDocument(document);
            }
          }
          else {
            value(Unit)
          }
        }
      } ?: run {
        LOG.warn("Timed out waiting for reformat to complete: ${dir.path}$outputFile")
        outputFile.putUserData(SKIP_FORMATTING_KEY, true)
      }
    }
  }

  private suspend fun openSchemaInEditor(project: Project, file: VirtualFile) {
    if (!getSettings(project).isOpenEditorWithIntrospectionResult) {
      return
    }

    withContext(Dispatchers.EDT) {
      val fileEditors = project.serviceAsync<FileEditorManager>().openFile(file, true, true)
      val textEditor = fileEditors.firstOrNull() as? TextEditor
      if (textEditor == null) {
        showUnableToOpenEditorNotification(file)
      }
    }
  }

  @RequiresWriteLock
  @Throws(IOException::class)
  private fun createOrUpdateSchemaFile(project: Project, dir: VirtualFile, fileName: String): VirtualFile {
    var outputFile = dir.findFileByRelativePath(fileName)
    if (outputFile == null) {
      val directory = PsiDirectoryFactory.getInstance(project).createDirectory(dir)
      val result = CreateFileAction.MkDirs(fileName, directory)
      outputFile = result.directory.virtualFile.createChildData(dir, result.newName)
    }
    return outputFile
  }

  private fun showUnableToOpenEditorNotification(file: VirtualFile) {
    Notifications.Bus.notify(
      Notification(
        GRAPHQL_NOTIFICATION_GROUP_ID,
        GraphQLBundle.message("graphql.notification.error.title"),
        GraphQLBundle.message("graphql.notification.unable.to.open.editor", file.path),
        NotificationType.ERROR)
    )
  }
}