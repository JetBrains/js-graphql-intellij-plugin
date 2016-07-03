/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.psi;

import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

public class JSGraphQLEndpointTypeResult {

    public final String name;

    @Nullable
    public final PsiFile fileToImport;

    public JSGraphQLEndpointTypeResult(String name, @Nullable PsiFile fileToImport) {
        this.name = name;
        this.fileToImport = fileToImport;
    }
}
