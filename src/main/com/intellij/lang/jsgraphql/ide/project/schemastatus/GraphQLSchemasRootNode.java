/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus;

import com.google.common.collect.Lists;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigData;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.SimpleNode;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Root node in the GraphQL schemas status tree.
 * Has a child node for each schema, and if no schemas have been configured a single node representing the default project-wide schema.
 */
public class GraphQLSchemasRootNode extends SimpleNode {

    private final GraphQLConfigManager configManager;

    public GraphQLSchemasRootNode(Project project) {
        super(project);
        configManager = GraphQLConfigManager.getService(myProject);
    }

    @NotNull
    @Override
    public SimpleNode[] getChildren() {
        try {
            if (DumbService.getInstance(myProject).isDumb()) {
                // empty the tree view during indexing
                return SimpleNode.NO_CHILDREN;
            }
            final List<SimpleNode> children = Lists.newArrayList();
            for (Map.Entry<VirtualFile, GraphQLConfigData> entry : configManager.getConfigurationsByPath().entrySet()) {
                children.add(new GraphQLConfigSchemaNode(myProject, configManager, entry.getValue(), entry.getKey()));
            }
            if (children.isEmpty()) {
                children.add(new GraphQLDefaultSchemaNode(myProject));
            }
            children.sort(Comparator.comparing(PresentableNodeDescriptor::getName));
            return children.toArray(SimpleNode.NO_CHILDREN);
        } catch (IndexNotReadyException e) {
            return SimpleNode.NO_CHILDREN;
        }
    }

    @Override
    public boolean isAutoExpandNode() {
        return true;
    }
}
