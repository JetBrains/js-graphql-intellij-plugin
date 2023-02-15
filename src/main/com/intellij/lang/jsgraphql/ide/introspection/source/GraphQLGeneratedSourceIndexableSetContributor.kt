package com.intellij.lang.jsgraphql.ide.introspection.source

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.IndexableSetContributor

class GraphQLGeneratedSourceIndexableSetContributor : IndexableSetContributor() {
    override fun getAdditionalRootsToIndex(): Set<VirtualFile> {
        return LocalFileSystem.getInstance().findFileByPath(GraphQLGeneratedSourceManager.getGeneratedFilesPath())
            ?.takeIf { it.isValid }
            ?.let { setOf(it) }
            ?: emptySet()
    }
}
