/**
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi;

import com.intellij.lang.jsgraphql.psi.impl.GraphQLTypeNameDefinitionOwnerPsiElement;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLTypeNameExtensionOwnerPsiElement;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

public class GraphQLPsiUtil {

    public static String getTypeName(PsiElement psiElement, @Nullable Ref<GraphQLIdentifier> typeNameRef) {

        if (psiElement != null) {

            final PsiElement typeOwner = PsiTreeUtil.getParentOfType(psiElement, GraphQLTypeNameDefinitionOwnerPsiElement.class, GraphQLTypeNameExtensionOwnerPsiElement.class);

            GraphQLIdentifier nameIdentifier = null;

            if (typeOwner instanceof GraphQLTypeNameDefinitionOwnerPsiElement) {
                final GraphQLTypeNameDefinition typeNameDefinition = ((GraphQLTypeNameDefinitionOwnerPsiElement) typeOwner).getTypeNameDefinition();
                if (typeNameDefinition != null) {
                    nameIdentifier = typeNameDefinition.getNameIdentifier();
                }
            } else if (typeOwner instanceof GraphQLTypeNameExtensionOwnerPsiElement) {
                final GraphQLTypeName typeName = ((GraphQLTypeNameExtensionOwnerPsiElement) typeOwner).getTypeName();
                if (typeName != null) {
                    nameIdentifier = typeName.getNameIdentifier();
                }
            }

            if (nameIdentifier != null) {
                if (typeNameRef != null) {
                    typeNameRef.set(nameIdentifier);
                }
                return nameIdentifier.getText();
            }

        }

        return null;
    }

}
