package com.intellij.lang.jsgraphql.ide.search

import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.jsgraphql.asSafely
import com.intellij.lang.jsgraphql.ide.injection.GraphQLFileTypeContributor
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.ExtensionPointUtil
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.SynchronizedClearableLazy

@Service(Service.Level.APP)
internal class GraphQLFileTypesProvider : Disposable {
  companion object {
    @JvmStatic
    fun getService() = service<GraphQLFileTypesProvider>()
  }

  private val contributedFileTypes = ExtensionPointUtil.dropLazyValueOnChange(SynchronizedClearableLazy {
    GraphQLFileTypeContributor.getAllFileTypes()
  }, GraphQLFileTypeContributor.EP_NAME, this)

  fun isAcceptedFile(file: VirtualFile): Boolean {
    val fileType = file.fileType
    if (fileType in getContributedFileTypes()) {
      return true
    }

    return fileType.asSafely<LanguageFileType>()?.language?.isKindOf(HTMLLanguage.INSTANCE) ?: false
  }

  private fun getContributedFileTypes(): Collection<FileType> = contributedFileTypes.get()

  override fun dispose() {
  }
}
