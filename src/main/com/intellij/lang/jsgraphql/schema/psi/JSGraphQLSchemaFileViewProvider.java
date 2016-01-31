/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema.psi;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import org.jetbrains.annotations.NotNull;

public class JSGraphQLSchemaFileViewProvider extends SingleRootFileViewProvider {

    protected JSGraphQLSchemaFileViewProvider(@NotNull PsiManager manager, @NotNull VirtualFile virtualFile, boolean eventSystemEnabled, @NotNull Language language, @NotNull FileType type) {
        super(manager, virtualFile, eventSystemEnabled, language, type);
    }
}
