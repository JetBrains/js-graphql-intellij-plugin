/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.v1.JSGraphQLTokenTypes;
import org.jetbrains.annotations.NotNull;

public class JSGraphQLPsiElement extends ASTWrapperPsiElement {

    public JSGraphQLPsiElement(@NotNull ASTNode node) {
        super(node);
    }

    public String getKeyword() {
        final ASTNode firstNode = getNode().getFirstChildNode();
        if (firstNode != null && firstNode.getElementType() == JSGraphQLTokenTypes.KEYWORD) {
            return firstNode.getText();
        }
        return null;
    }
}
