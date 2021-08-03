/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.jsgraphql.icons.GraphQLIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;

public class JSGraphQLEndpointFileType extends LanguageFileType {

    public static final JSGraphQLEndpointFileType INSTANCE = new JSGraphQLEndpointFileType();

    private JSGraphQLEndpointFileType() {
        super(JSGraphQLEndpointLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "GraphQL Endpoint";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "GraphQL Endpoint file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "graphqle";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return GraphQLIcons.Files.GraphQLSchema;
    }
}
