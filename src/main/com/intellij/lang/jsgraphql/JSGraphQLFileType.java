/**
 *  Copyright (c) 2015, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql;

import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class JSGraphQLFileType extends LanguageFileType {

    public static final JSGraphQLFileType INSTANCE = new JSGraphQLFileType();

    private JSGraphQLFileType() {
        super(JSGraphQLLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "GraphQL";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "GraphQL language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "graphql";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return JSGraphQLIcons.Files.GraphQL;
    }
}