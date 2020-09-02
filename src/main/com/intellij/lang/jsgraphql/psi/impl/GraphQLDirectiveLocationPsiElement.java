/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.ide.project.GraphQLPsiSearchHelper;
import com.intellij.lang.jsgraphql.psi.GraphQLDirectiveLocation;
import com.intellij.lang.jsgraphql.psi.GraphQLEnumValue;
import com.intellij.lang.jsgraphql.psi.GraphQLIdentifier;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GraphQLDirectiveLocationPsiElement extends GraphQLElementImpl implements GraphQLDirectiveLocation {

    public GraphQLDirectiveLocationPsiElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        final Ref<PsiReference> reference = new Ref<>();
        final GraphQLDirectiveLocationPsiElement psiElement = this;
        final String locationName = psiElement.getText();
        GraphQLPsiSearchHelper.getInstance(getProject()).getBuiltInSchema().accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if(element instanceof GraphQLEnumValue && element.getText().equals(locationName)) {
                    final GraphQLIdentifier referencedEnumValue = ((GraphQLEnumValue) element).getNameIdentifier();
                    reference.set(new PsiReferenceBase<PsiElement>(psiElement, new TextRange(0, psiElement.getTextLength())) {
                        @Nullable
                        @Override
                        public PsiElement resolve() {
                            return referencedEnumValue;
                        }

                        @NotNull
                        @Override
                        public Object[] getVariants() {
                            return PsiReference.EMPTY_ARRAY;
                        }
                    });
                    return; // done visiting
                }
                super.visitElement(element);
            }
        });
        return reference.get();
    }
}
