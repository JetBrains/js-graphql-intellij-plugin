/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons;
import com.intellij.lang.jsgraphql.schema.ide.project.JSGraphQLSchemaLanguageProjectService;
import com.intellij.lang.jsgraphql.schema.ide.type.JSGraphQLNamedType;
import com.intellij.lang.jsgraphql.schema.ide.type.JSGraphQLPropertyType;
import com.intellij.lang.jsgraphql.schema.psi.JSGraphQLSchemaFile;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class JSGraphQLNamedPropertyPsiElement extends JSGraphQLNamedPsiElement {

    public JSGraphQLNamedPropertyPsiElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {

            @Override
            public String getPresentableText() {
                return getName();
            }

            @Nullable
            @Override
            public String getLocationString() {
                return null;
            }

            @Override
            public Icon getIcon(boolean open) {
                return resolveIcon();
            }
        };
    }

    @Override
    public Icon getElementIcon(final int flags) {
        return resolveIcon();
    }

    /**
     * @return whether the value type of this property is a scalar
     * @see JSGraphQLSchemaLanguageProjectService#SCALAR_TYPES
     */
    public boolean isScalar() {

        JSGraphQLNamedPropertyPsiElement typedReferenceElement = this;
        if(!(getContainingFile() instanceof JSGraphQLSchemaFile)) {
            // this element isn't part of the schema file, so we need to resolve the reference
            final PsiReference psiReference = getReference();
            if(psiReference != null) {
                final PsiElement resolvedReference = psiReference.resolve();
                if (resolvedReference instanceof JSGraphQLNamedPropertyPsiElement) {
                    typedReferenceElement = (JSGraphQLNamedPropertyPsiElement) resolvedReference;
                }
            }
        }
        final JSGraphQLSchemaLanguageProjectService schemaService = JSGraphQLSchemaLanguageProjectService.getService(getProject());
        final String propertyOwnerType = schemaService.getTypeName(typedReferenceElement);
        if(propertyOwnerType != null) {
            final JSGraphQLNamedType namedType = schemaService.getNamedType(propertyOwnerType);
            if(namedType != null) {
                final JSGraphQLPropertyType propertyType = namedType.properties.get(getName());
                if(propertyType != null) {
                    if(propertyType.propertyValueTypeName != null) {
                        if(JSGraphQLSchemaLanguageProjectService.SCALAR_TYPES.contains(propertyType.propertyValueTypeName)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private Icon resolveIcon() {
        return isScalar() ? JSGraphQLIcons.Schema.Scalar : JSGraphQLIcons.Schema.Field;
    }

}
