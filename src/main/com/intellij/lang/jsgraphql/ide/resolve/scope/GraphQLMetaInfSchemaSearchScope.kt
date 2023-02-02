package com.intellij.lang.jsgraphql.ide.resolve.scope

import com.intellij.lang.jsgraphql.GraphQLFileType
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.DelegatingGlobalSearchScope

private const val META_INF_DIR = "META-INF/schema"

class GraphQLMetaInfSchemaSearchScope(project: Project) :
    DelegatingGlobalSearchScope(getScopeRestrictedByFileTypes(allScope(project), GraphQLFileType.INSTANCE)) {

    private val myIndex: ProjectFileIndex = ProjectRootManager.getInstance(project).fileIndex

    override fun contains(file: VirtualFile): Boolean {
        return super.contains(file)
            && myIndex.isInLibrary(file)
            && myIndex.isInLibraryClasses(file)
            && file.parent != null
            && file.parent.path.endsWith(META_INF_DIR)
    }

    override fun isSearchInModuleContent(aModule: Module): Boolean {
        return false
    }

    override fun isSearchInLibraries(): Boolean {
        return true
    }

    override fun toString(): String {
        return "META-INF/schema files in Libraries in ($myBaseScope)"
    }
}
