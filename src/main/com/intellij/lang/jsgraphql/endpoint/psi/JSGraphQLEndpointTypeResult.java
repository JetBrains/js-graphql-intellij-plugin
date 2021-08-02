/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.psi;

import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

public class JSGraphQLEndpointTypeResult<T extends JSGraphQLEndpointNamedTypeDefinition> {

    public final String name;

    public final T element;

    @Nullable
    public final PsiFile fileToImport;

    public JSGraphQLEndpointTypeResult(String name, T element, @Nullable PsiFile fileToImport) {
        this.name = name;
        this.element = element;
        this.fileToImport = fileToImport;
    }
}
