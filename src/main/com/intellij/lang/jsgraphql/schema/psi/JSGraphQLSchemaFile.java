/**
 *  Copyright (c) 2015, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.jsgraphql.schema.JSGraphQLSchemaFileType;
import com.intellij.lang.jsgraphql.schema.JSGraphQLSchemaLanguage;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;

public class JSGraphQLSchemaFile extends PsiFileBase {

    public JSGraphQLSchemaFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, JSGraphQLSchemaLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
        // for find usages, we want GraphQL files to be considered project-wide
        // this enables find usages in modules that live outside the project base dir
        return ProjectScope.getAllScope(getProject());
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return JSGraphQLSchemaFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "GraphQL Schema File";
    }

}