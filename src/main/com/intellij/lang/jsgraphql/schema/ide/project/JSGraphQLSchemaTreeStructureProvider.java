/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema.ide.project;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.ProjectViewProjectNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.lang.jsgraphql.JSGraphQLParserDefinition;
import com.intellij.lang.jsgraphql.ide.configuration.JSGraphQLConfigurationProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Adds the GraphQL Schema file to the project tree view.
 */
public class JSGraphQLSchemaTreeStructureProvider implements TreeStructureProvider, DumbAware {

    private final static Map<Project, JSGraphQLSchemaDirectoryNode> nodes = Maps.newConcurrentMap();

    @NotNull
    @Override
    public Collection<AbstractTreeNode> modify(@NotNull AbstractTreeNode parent, @NotNull Collection<AbstractTreeNode> children, ViewSettings settings) {

        if(parent instanceof ProjectViewProjectNode) {
            final Project project = parent.getProject();
            if (project != null && isGraphQLEnabled(project)) {
                final ArrayList<AbstractTreeNode> modifiedChildren = Lists.newArrayList(children);
                modifiedChildren.add(nodes.computeIfAbsent(project, p -> {
                    final JSGraphQLSchemaDirectoryNode node = new JSGraphQLSchemaDirectoryNode(parent.getProject(), settings);
                    Disposer.register(project, () -> nodes.remove(p));
                    return node;
                }));
                return modifiedChildren;
            }
        }

        return children;
    }

    private boolean isGraphQLEnabled(Project project) {
        if(Boolean.TRUE.equals(project.getUserData(JSGraphQLParserDefinition.JSGRAPHQL_ACTIVATED))) {
            return true;
        }
        return JSGraphQLConfigurationProvider.getService(project).hasGraphQLConfig();
    }

    @Nullable
    @Override
    public Object getData(Collection<AbstractTreeNode> selected, String dataName) {
        return null;
    }
}
