/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointLanguage;
import org.jetbrains.annotations.NotNull;

public abstract class JSGraphQLEndpointPsiElement extends ASTWrapperPsiElement {

    public JSGraphQLEndpointPsiElement(@NotNull ASTNode node) {
        super(node);
    }

    @NotNull
    @Override
    public Language getLanguage() {
        return JSGraphQLEndpointLanguage.INSTANCE;
    }
}
