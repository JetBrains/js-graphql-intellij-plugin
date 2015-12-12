/**
 *  Copyright (c) 2015, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

public class JSGraphQLPsiFactory {

    public static PsiElement createElement(ASTNode node) {
        if(node.getElementType() instanceof JSGraphQLElementType) {
            final JSGraphQLElementType elementType = (JSGraphQLElementType)node.getElementType();
            if(JSGraphQLElementType.TEMPLATE_FRAGMENT_KIND.equals(elementType.getKind())) {
                return new JSGraphQLTemplateFragmentPsiElement(node);
            }
        }
        return new JSGraphQLPsiElement(node);
    }
}
