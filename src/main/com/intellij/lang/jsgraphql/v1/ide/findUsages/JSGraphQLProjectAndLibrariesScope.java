/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.findUsages;

import com.intellij.lang.jsgraphql.ide.references.GraphQLFindUsagesUtil;
import com.intellij.lang.jsgraphql.v1.schema.ide.project.JSGraphQLSchemaLanguageProjectService;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import org.jetbrains.annotations.NotNull;

/**
 * Scope that includes the in-memory GraphQL schema file into the 'Project and Libraries' usages search
 */
public class JSGraphQLProjectAndLibrariesScope extends ProjectAndLibrariesScope {


    private final ProjectAndLibrariesScope delegate;

    @SuppressWarnings("ConstantConditions")
    public JSGraphQLProjectAndLibrariesScope(ProjectAndLibrariesScope delegate) {
        super(delegate.getProject(), delegate.isSearchOutsideRootModel());
        this.delegate = delegate;
    }

    @Override
    public boolean contains(@NotNull VirtualFile file) {

        // always include the current schema file
        if (JSGraphQLSchemaLanguageProjectService.isProjectSchemaFile(file)) {
            return true;
        }

        // we're only interested in usages in GraphQL, JavaScript and TypeScript files
        return GraphQLFindUsagesUtil.getService().getIncludedFileTypes().contains(file.getFileType()) && super.contains(file);

    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object o) {
        // we want equality with our delegate
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
