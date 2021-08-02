/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.structureView;

import com.google.common.collect.Lists;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes;
import com.intellij.lang.jsgraphql.endpoint.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * A node in the GraphQL Endpoint structure tree view
 */
public class JSGraphQLEndpointStructureViewTreeElement extends PsiTreeElementBase<PsiElement> {


    final PsiElement childrenBase;
    final PsiElement element;

    public JSGraphQLEndpointStructureViewTreeElement(PsiElement childrenBase, PsiElement psiElement) {
        super(psiElement);
        this.element = psiElement;
        this.childrenBase = childrenBase;
    }

    @NotNull
    @Override
    public Collection<StructureViewTreeElement> getChildrenBase() {
        final Collection<StructureViewTreeElement> children = Lists.newArrayList();
        if (childrenBase instanceof PsiFile) {
            for (PsiElement child : childrenBase.getChildren()) {
                if (child instanceof JSGraphQLEndpointNamedTypeDefinition) {
                    final JSGraphQLEndpointNamedTypeDefinition typeDefinition = (JSGraphQLEndpointNamedTypeDefinition) child;
                    children.add(new JSGraphQLEndpointStructureViewTreeElement(typeDefinition, typeDefinition.getNamedTypeDef()));
                } else if (child instanceof JSGraphQLEndpointImportDeclaration) {
                    children.add(new JSGraphQLEndpointStructureViewTreeElement(child, child));
                }
            }
        } else if (childrenBase instanceof JSGraphQLEndpointNamedTypeDefinition) {
            childrenBase.accept(new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(PsiElement element) {
                    if (element instanceof JSGraphQLEndpointProperty) {
                        children.add(new JSGraphQLEndpointStructureViewTreeElement(element.getParent(), element));
                        return;
                    }
                    if (element instanceof JSGraphQLEndpointEnumValueDefinition) {
                        children.add(new JSGraphQLEndpointStructureViewTreeElement(element, element));
                        return;
                    }
                    if (element instanceof JSGraphQLEndpointUnionMember) {
                        children.add(new JSGraphQLEndpointStructureViewTreeElement(element, element));
                        return;
                    }
                    super.visitElement(element);
                }
            });
        } else if (childrenBase instanceof JSGraphQLEndpointFieldDefinition) {
            childrenBase.accept(new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(PsiElement element) {
                    if (element instanceof JSGraphQLEndpointInputValueDefinition) {
                        children.add(new JSGraphQLEndpointStructureViewTreeElement(element, element));
                        return;
                    }
                    super.visitElement(element);
                }
            });
        }
        return children;
    }


    @Nullable
    @Override
    public String getPresentableText() {
        if(element instanceof JSGraphQLEndpointImportDeclaration) {
            return element.getText();
        }
        final PsiNameIdentifierOwner identifier = PsiTreeUtil.getChildOfType(element, PsiNameIdentifierOwner.class);
        if (identifier != null) {
            return identifier.getText();
        }
        final ASTNode astIdentifier = element.getNode().getFirstChildNode();
        if (astIdentifier != null && astIdentifier.getElementType() == JSGraphQLEndpointTokenTypes.IDENTIFIER) {
            return astIdentifier.getText();
        }
        if (element instanceof PsiFile) {
            return null;
        }
        return element.getText();
    }
}
