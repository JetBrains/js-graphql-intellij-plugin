package com.intellij.lang.jsgraphql.ide.search

import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.jsgraphql.asSafely
import com.intellij.lang.jsgraphql.ide.injection.GraphQLFileTypeContributor
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.vfs.VirtualFile
import java.util.concurrent.atomic.AtomicReference

@Service(Service.Level.APP)
class GraphQLFileTypesProvider : Disposable {
    companion object {
        @JvmStatic
        fun getService() = service<GraphQLFileTypesProvider>()
    }

    private val myContributedFileTypes = AtomicReference(GraphQLFileTypeContributor.getAllFileTypes())

    init {
        GraphQLFileTypeContributor.EP_NAME.addChangeListener({
            myContributedFileTypes.set(GraphQLFileTypeContributor.getAllFileTypes())
        }, this)
    }

    fun isAcceptedFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if (fileType in getContributedFileTypes()) {
            return true
        }

        return fileType.asSafely<LanguageFileType>()?.language?.isKindOf(HTMLLanguage.INSTANCE) ?: false
    }

    fun getContributedFileTypes(): Collection<FileType> {
        return myContributedFileTypes.get()
    }

    override fun dispose() {
    }
}
