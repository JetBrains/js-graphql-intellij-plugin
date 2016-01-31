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
            if(JSGraphQLElementType.FIELD_KIND.equals(elementType.getKind())) {
                return new JSGraphQLFieldPsiElement(node);
            } else if(JSGraphQLElementType.PROPERTY_KIND.equals(elementType.getKind())) {
                return new JSGraphQLNamedPropertyPsiElement(node);
            } else if(JSGraphQLElementType.ATOM_KIND.equals(elementType.getKind()) || JSGraphQLElementType.DEFINITION_KIND.equals(elementType.getKind())) {
                return new JSGraphQLNamedTypePsiElement(node);
            } else if(JSGraphQLElementType.SELECTION_SET_KIND.equals(elementType.getKind())) {
                return new JSGraphQLSelectionSetPsiElement(node);
            } else if(JSGraphQLElementType.FRAGMENT_DEFINITION_KIND.equals(elementType.getKind())) {
                return new JSGraphQLFragmentDefinitionPsiElement(node);
            } else if(JSGraphQLElementType.INLINE_FRAGMENT_KIND.equals(elementType.getKind())) {
                return new JSGraphQLInlineFragmentPsiElement(node);
            } else if(JSGraphQLElementType.TEMPLATE_FRAGMENT_KIND.equals(elementType.getKind())) {
                return new JSGraphQLTemplateFragmentPsiElement(node);
            }
        }
        return new JSGraphQLPsiElement(node);
    }
}
