/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.jsgraphql.JSGraphQLFileType;
import com.intellij.lang.jsgraphql.JSGraphQLLanguage;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class JSGraphQLFile extends PsiFileBase {

    public JSGraphQLFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, JSGraphQLLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return JSGraphQLFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "GraphQL File";
    }

}