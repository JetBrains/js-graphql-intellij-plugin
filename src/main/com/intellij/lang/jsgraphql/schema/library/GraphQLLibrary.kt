package com.intellij.lang.jsgraphql.schema.library

import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.icons.GraphQLIcons
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.backend.workspace.virtualFile
import com.intellij.platform.workspace.storage.url.VirtualFileUrl
import javax.swing.Icon

data class GraphQLLibrary(
  val descriptor: GraphQLLibraryDescriptor,
  val rootUrls: Set<VirtualFileUrl>,
) : ItemPresentation {
  val sourceRoots: Collection<VirtualFile>
    get() = rootUrls.mapNotNull { url -> url.virtualFile?.takeIf { it.isValid } }.toSet()

  override fun getPresentableText(): String =
    GraphQLBundle.message("graphql.library.prefix", descriptor.displayName)

  override fun getIcon(unused: Boolean): Icon = GraphQLIcons.Logos.GraphQL
}
