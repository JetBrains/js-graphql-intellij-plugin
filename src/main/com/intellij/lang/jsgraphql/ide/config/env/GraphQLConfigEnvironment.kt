package com.intellij.lang.jsgraphql.ide.config.env

import com.intellij.lang.jsgraphql.ide.resolve.GraphQLResolveUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.util.BackgroundTaskUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.AsyncFileListener.ChangeApplier
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.events.*
import com.intellij.testFramework.LightVirtualFile
import com.intellij.util.Alarm
import com.intellij.util.EnvironmentUtil
import com.intellij.util.concurrency.annotations.RequiresEdt
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.DotenvBuilder
import io.github.cdimascio.dotenv.DotenvException
import org.jetbrains.annotations.VisibleForTesting
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.function.Function


@Service(Service.Level.PROJECT)
class GraphQLConfigEnvironment(private val project: Project) : ModificationTracker, Disposable, AsyncFileListener, DocumentListener {
  companion object {
    @JvmField
    @VisibleForTesting
    var getEnvVariable = Function<String?, String?> { key: String? -> key?.let { System.getProperty(it) } }

    @JvmStatic
    fun getInstance(project: Project) = project.service<GraphQLConfigEnvironment>()

    private val FILENAMES = linkedSetOf(
      ".env.local",
      ".env.development.local",
      ".env.development",
      ".env.dev.local",
      ".env.dev",
      ".env"
    )

    private const val SAVE_DOCUMENTS_DELAY = 2000
  }

  private val variables: ConcurrentMap<VirtualFile, ConcurrentMap<String, String>> = ConcurrentHashMap()
  private val modificationTracker = SimpleModificationTracker()

  private val documentsToSave = ConcurrentHashMap.newKeySet<WatchedFile>()
  private val documentsSaveAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)

  init {
    VirtualFileManager.getInstance().addAsyncFileListener(this, this)
    EditorFactory.getInstance().eventMulticaster.addDocumentListener(this, this)
  }

  fun createSnapshot(variables: Collection<String>, fileOrDir: VirtualFile?): GraphQLEnvironmentSnapshot {
    return GraphQLEnvironmentSnapshot(variables.associateWith { getVariable(it, fileOrDir) })
  }

  fun setExplicitVariable(name: String, value: String?, configFileOrDir: VirtualFile) {
    val key = configFileOrDir.parentDirectory ?: return
    val variables = variables.computeIfAbsent(key) { ConcurrentHashMap() }

    if (value.isNullOrBlank()) {
      variables.remove(name)
    }
    else {
      variables[name] = value
    }

    notifyEnvironmentChanged()
  }

  fun setExplicitVariables(newVariables: Map<String, String?>, configFileOrDir: VirtualFile) {
    val key = configFileOrDir.parentDirectory ?: return
    val variables = variables.computeIfAbsent(key) { ConcurrentHashMap() }

    newVariables.forEach {
      if (it.value.isNullOrBlank()) {
        variables.remove(it.key)
      }
      else {
        variables[it.key] = it.value
      }
    }

    notifyEnvironmentChanged()
  }

  fun getExplicitVariable(name: String, configFileOrDir: VirtualFile): String? {
    return configFileOrDir.parentDirectory?.let { variables[it] }?.get(name)
  }

  private fun getVariable(name: String, fileOrDir: VirtualFile?): String? {
    // Try to load the variable from the jvm parameters
    var value = getEnvVariable.apply(name)
    if (value.isNullOrBlank() && fileOrDir != null) {
      value = getExplicitVariable(name, fileOrDir)
    }
    if (value.isNullOrBlank()) {
      value = tryToGetVariableFromDotEnvFile(name, fileOrDir)
    }
    if (value.isNullOrBlank()) {
      value = EnvironmentUtil.getValue(name)
    }
    return value
  }

  private fun tryToGetVariableFromDotEnvFile(varName: String, fileOrDir: VirtualFile?): String? {
    var value = findClosestEnvFile(fileOrDir)?.let { findVariableValueInFile(it, varName) }
    if (value == null) {
      value = project.guessProjectDir()
        ?.let { findEnvFileInDirectory(it) }
        ?.let { findVariableValueInFile(it, varName) }
    }
    return value
  }

  private fun findClosestEnvFile(fileOrDir: VirtualFile?): VirtualFile? {
    if (fileOrDir == null) return null

    return runReadAction {
      var result: VirtualFile? = null
      GraphQLResolveUtil.processDirectoriesUpToContentRoot(project, fileOrDir) {
        result = findEnvFileInDirectory(it)
        result == null
      }
      result
    }
  }

  private fun findVariableValueInFile(file: VirtualFile, name: String): String? {
    return try {
      if (ApplicationManager.getApplication().isUnitTestMode) {
        return loadFromVirtualFile(file)[name]
      }

      val dotenv = createDotenvBuilder(file.parent.path).filename(file.name).load()
      dotenv[name]
    }
    catch (e: DotenvException) {
      thisLogger().warn(e)
      null
    }
  }

  // test only, dotenv can't read from the TempFS
  private fun loadFromVirtualFile(file: VirtualFile?): Map<String, String?> {
    if (file == null) return emptyMap()
    return VfsUtil.loadText(file)
      .lines()
      .asSequence()
      .map { it.split('=', limit = 2) }
      .filter { it.isNotEmpty() }
      .associate { it[0].trim() to it.getOrNull(1)?.trim() }
  }

  private fun findEnvFileInDirectory(dir: VirtualFile): VirtualFile? {
    if (!dir.isDirectory) return null

    for (candidate in FILENAMES) {
      val file = dir.findChild(candidate)
      if (file != null && file.isValid) {
        return file
      }
    }
    return null
  }

  fun notifyEnvironmentChanged() {
    invokeLater {
      if (project.isDisposed) return@invokeLater

      modificationTracker.incModificationCount()
      project.messageBus.syncPublisher(GraphQLConfigEnvironmentListener.TOPIC).onEnvironmentChanged()
    }
  }

  private fun createDotenvBuilder(path: String): DotenvBuilder {
    return Dotenv
      .configure()
      .directory(path)
      .ignoreIfMalformed()
      .ignoreIfMissing()
  }

  override fun getModificationCount(): Long = modificationTracker.modificationCount

  private val VirtualFile.parentDirectory: VirtualFile?
    get() = if (isDirectory) this else parent

  override fun prepareChange(events: List<VFileEvent>): ChangeApplier? {
    var changed = false

    for (event in events) {
      if (changed) break

      when (event) {
        is VFileCreateEvent -> if (event.childName in FILENAMES) {
          changed = true
        }

        is VFileCopyEvent -> if (event.newChildName in FILENAMES) {
          changed = true
        }

        is VFileDeleteEvent -> if (event.file.name in FILENAMES) {
          changed = true
        }

        is VFileContentChangeEvent -> if (event.file.name in FILENAMES) {
          changed = true
        }

        is VFileMoveEvent -> if (event.file.name in FILENAMES) {
          changed = true
        }

        is VFilePropertyChangeEvent -> if (event.propertyName == VirtualFile.PROP_NAME) {
          if (event.oldValue in FILENAMES || event.newValue in FILENAMES) {
            changed = true
          }
        }
      }
    }

    if (!changed) {
      return null
    }

    return object : ChangeApplier {
      override fun afterVfsChange() {
        notifyEnvironmentChanged()
      }
    }
  }

  override fun documentChanged(event: DocumentEvent) {
    val file = FileDocumentManager.getInstance().getFile(event.document) ?: return
    if (file is LightVirtualFile || file.name !in FILENAMES) return

    documentsToSave.add(WatchedFile(file, event.document))
    scheduleDocumentSave()
  }

  private fun scheduleDocumentSave() {
    if (ApplicationManager.getApplication().isUnitTestMode) {
      invokeLater { saveDocuments() }
    }
    else {
      documentsSaveAlarm.cancelAllRequests()
      documentsSaveAlarm.addRequest({
                                      BackgroundTaskUtil.runUnderDisposeAwareIndicator(this, ::saveDocuments)
                                    }, SAVE_DOCUMENTS_DELAY)
    }
  }

  @RequiresEdt
  private fun saveDocuments() {
    if (project.isDisposed || documentsToSave.isEmpty()) {
      return
    }

    val documentManager = FileDocumentManager.getInstance()
    HashSet(documentsToSave)
      .also { documentsToSave.removeAll(it) }
      .filter { it.file.isValid }
      .forEach {
        ProgressManager.checkCanceled()
        documentManager.saveDocument(it.document)
      }
  }

  private data class WatchedFile(val file: VirtualFile, val document: Document)

  override fun dispose() {
  }
}

data class GraphQLEnvironmentSnapshot(val variables: Map<String, String?>) {
  companion object {
    val EMPTY = GraphQLEnvironmentSnapshot(emptyMap())
  }

  fun hasMissingValues(nameFilter: Collection<String>? = null): Boolean {
    return variables.any { (nameFilter == null || it.key in nameFilter) && it.value.isNullOrBlank() }
  }

  val hasVariables = variables.isNotEmpty()

  fun update(project: Project, dir: VirtualFile): GraphQLEnvironmentSnapshot =
    GraphQLConfigEnvironment.getInstance(project).createSnapshot(variables.keys, dir)

  override fun toString(): String {
    // don't write env values to log
    return "GraphQLEnvironmentSnapshot(variables=${variables.keys.joinToString()}, hasMissingValues=${hasMissingValues()})"
  }
}
