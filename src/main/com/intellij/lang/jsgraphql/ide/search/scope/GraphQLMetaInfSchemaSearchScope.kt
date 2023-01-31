package com.intellij.lang.jsgraphql.ide.search.scope;

import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.DelegatingGlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

public class GraphQLMetaInfSchemaSearchScope extends DelegatingGlobalSearchScope {
    private final ProjectFileIndex myIndex;

    public GraphQLMetaInfSchemaSearchScope(@NotNull Project project) {
        super(GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(project), GraphQLFileType.INSTANCE));
        myIndex = ProjectRootManager.getInstance(project).getFileIndex();
    }

    @Override
    public boolean contains(@NotNull VirtualFile file) {
        return super.contains(file)
            && myIndex.isInLibrary(file)
            && myIndex.isInLibraryClasses(file)
            && file.getParent() != null
            && file.getParent().getPath().endsWith("META-INF/schema");
    }

    @Override
    public boolean isSearchInModuleContent(@NotNull Module aModule) {
        return false;
    }

    @Override
    public boolean isSearchInLibraries() {
        return true;
    }

    @Override
    public String toString() {
        return "META-INF/schema files in Libraries in (" + myBaseScope + ")";
    }
}
