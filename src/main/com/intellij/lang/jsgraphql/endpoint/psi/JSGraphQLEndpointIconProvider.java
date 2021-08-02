/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.psi;

import com.intellij.ide.IconProvider;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointLanguage;
import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Provides icons for Endpoint Language PSI Elements. Used by structure tree view and find usages
 */
public class JSGraphQLEndpointIconProvider extends IconProvider {

    @Nullable
    @Override
    public Icon getIcon(@NotNull PsiElement element, @Iconable.IconFlags int flags) {
        if (element.getLanguage() != JSGraphQLEndpointLanguage.INSTANCE) {
            return null;
        }
        if (element instanceof JSGraphQLEndpointNamedTypeDef) {
            if (element.getParent() instanceof JSGraphQLEndpointNamedTypeDefinition) {
                return getTypeDefinitionIcon((JSGraphQLEndpointNamedTypeDefinition) element.getParent());
            }
        }
        if (element instanceof JSGraphQLEndpointNamedTypeDefinition) {
            return getTypeDefinitionIcon((JSGraphQLEndpointNamedTypeDefinition) element);
        }
        if (element instanceof JSGraphQLEndpointProperty) {
            return JSGraphQLIcons.Schema.Field;
        }
        if (element instanceof JSGraphQLEndpointInputValueDefinition) {
            return JSGraphQLIcons.Schema.Attribute;
        }
        if(element instanceof JSGraphQLEndpointImportDeclaration) {
            return JSGraphQLIcons.Files.GraphQLSchema;
        }
        return null;
    }

    private Icon getTypeDefinitionIcon(JSGraphQLEndpointNamedTypeDefinition typeDefinition) {
        if (typeDefinition instanceof JSGraphQLEndpointObjectTypeDefinition) {
            return JSGraphQLIcons.Schema.Type;
        }
        if (typeDefinition instanceof JSGraphQLEndpointInterfaceTypeDefinition) {
            return JSGraphQLIcons.Schema.Interface;
        }
        if (typeDefinition instanceof JSGraphQLEndpointInputObjectTypeDefinition) {
            return JSGraphQLIcons.Schema.Interface;
        }
        if (typeDefinition instanceof JSGraphQLEndpointEnumTypeDefinition) {
            return JSGraphQLIcons.Schema.Enum;
        }
        if (typeDefinition instanceof JSGraphQLEndpointScalarTypeDefinition) {
            return JSGraphQLIcons.Schema.Scalar;
        }
        if (typeDefinition instanceof JSGraphQLEndpointUnionTypeDefinition) {
            return JSGraphQLIcons.Schema.Type;
        }
        return null;
    }
}
