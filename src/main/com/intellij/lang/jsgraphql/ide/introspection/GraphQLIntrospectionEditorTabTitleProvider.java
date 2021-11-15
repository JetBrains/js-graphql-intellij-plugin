/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.introspection;

import com.intellij.lang.jsgraphql.schema.GraphQLSchemaKeys;
import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraphQLIntrospectionEditorTabTitleProvider implements EditorTabTitleProvider {
    @Nullable
    @Override
    public String getEditorTabTitle(@NotNull Project project, @NotNull VirtualFile file) {
        Boolean isIntrospectionSDL = file.getUserData(GraphQLSchemaKeys.IS_GRAPHQL_INTROSPECTION_SDL);
        if (Boolean.TRUE.equals(isIntrospectionSDL)) {
            return "GraphQL Schema (" + StringUtils.substringAfterLast(file.getName(), "/") + ")";
        }
        return null;
    }
}
