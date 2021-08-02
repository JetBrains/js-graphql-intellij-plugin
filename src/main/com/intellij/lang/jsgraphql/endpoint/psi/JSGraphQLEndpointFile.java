/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.psi;

import org.jetbrains.annotations.NotNull;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointFileType;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointLanguage;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;

public class JSGraphQLEndpointFile extends PsiFileBase {

    public JSGraphQLEndpointFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, JSGraphQLEndpointLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return JSGraphQLEndpointFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "GraphQL Endpoint File";
    }

}
