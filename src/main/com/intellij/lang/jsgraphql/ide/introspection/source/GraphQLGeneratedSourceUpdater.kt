package com.intellij.lang.jsgraphql.ide.introspection.source

import com.intellij.json.JsonFileType
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigListener
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.introspection.isJsonSchemaCandidate
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.AsyncFileListener.ChangeApplier
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Alarm
import com.intellij.util.concurrency.SequentialTaskExecutor
import io.ktor.util.collections.*
import java.util.*
import java.util.concurrent.Callable


@Service(Service.Level.PROJECT)
class GraphQLGeneratedSourceUpdater(private val project: Project) : Disposable, AsyncFileListener, GraphQLConfigListener {
    companion object {
        fun getInstance(project: Project) = project.service<GraphQLGeneratedSourceUpdater>()

        private const val REFRESH_DELAY = 500
        private const val RETRY_REFRESH_DELAY = 10_000
    }

    private val generateSourceManager = GraphQLGeneratedSourceManager.getInstance(project)
    private val fileDocumentManager = FileDocumentManager.getInstance()

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
            if (file in jsonSchemaFiles) {
                changed = true
                break
            }
            if (file.isDirectory) {
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

    fun refreshJsonSchemaFiles(retry: Boolean = false) {
        queue.cancelAllRequests()
        queue.addRequest({
            ReadAction.nonBlocking(Callable {
                val schemaScope = GraphQLConfigProvider.getInstance(project)
                    .getAllConfigs()
                    .asSequence()
                    .flatMap { it.getProjects().values }
                    .map { it.schemaScope }
                    .reduceOrNull(GlobalSearchScope::union) ?: return@Callable emptySet()

                FileTypeIndex.getFiles(JsonFileType.INSTANCE, schemaScope)
                    .filter { isJsonSchemaCandidate(fileDocumentManager.getDocument(it)) }
                    .toSet()
            })
                .expireWith(this)
                .inSmartMode(project)
                .withDocumentsCommitted(project)
                .finishOnUiThread(ModalityState.defaultModalityState()) {
                    val prevFiles = jsonSchemaFiles
                    jsonSchemaFiles = Collections.unmodifiableSet(it)

                    if (it.isNotEmpty()) {
                        ApplicationManager.getApplication().executeOnPooledThread {
                            for (virtualFile in it) {
                                generateSourceManager.requestGeneratedFile(virtualFile)
                            }
                        }
                    } else {
                        if (prevFiles != it) {
                            generateSourceManager.notifySourcesChanged()
                        }
                    }
                }
                .submit(executor)
        }, if (retry) RETRY_REFRESH_DELAY else REFRESH_DELAY)
    }

    override fun dispose() {
        executor.shutdown()
    }
}
