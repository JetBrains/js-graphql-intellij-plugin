/**
 *  Copyright (c) 2015, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class JSGraphQLSchemaFileType extends LanguageFileType {

    public static final JSGraphQLSchemaFileType INSTANCE = new JSGraphQLSchemaFileType();

    private JSGraphQLSchemaFileType() {
        super(JSGraphQLSchemaLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "GraphQL Schema";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "GraphQL schema file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "graphqls";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return JSGraphQLIcons.Files.GraphQLSchema;
    }
}