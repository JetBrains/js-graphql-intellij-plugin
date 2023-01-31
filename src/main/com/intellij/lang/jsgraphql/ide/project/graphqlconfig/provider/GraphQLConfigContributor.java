/*
 * Copyright (c) 2022-present, Benoit Lubek
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.graphqlconfig.provider;

import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigData;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Plugins can implement this EP to provide GraphQL configs for a project.
 */
public interface GraphQLConfigContributor {

    ExtensionPointName<GraphQLConfigContributor> EP_NAME = ExtensionPointName.create("com.intellij.lang.jsgraphql.configContributor");

    /**
     * Provide GraphQL configs by path for a given project.
     *
     * @return key: a VirtualFile that must point to a directory, value: the GraphQLConfigData for that directory.
     */
    @NotNull
    Map<@NotNull VirtualFile, @NotNull GraphQLConfigData> getGraphQLConfigurationsByPath(@NotNull Project project);
}
