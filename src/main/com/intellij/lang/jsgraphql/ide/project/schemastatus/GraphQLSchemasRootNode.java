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
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider;
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.SimpleNode;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

/**
 * Root node in the GraphQL schemas status tree.
 * Has a child node for each schema, and if no schemas have been configured a single node representing the default project-wide schema.
 */
public class GraphQLSchemasRootNode extends SimpleNode {

    public GraphQLSchemasRootNode(Project project) {
        super(project);
    }

    @Override
    public SimpleNode @NotNull [] getChildren() {
        try {
            GraphQLConfigProvider provider = GraphQLConfigProvider.getInstance(myProject);
            if (DumbService.getInstance(myProject).isDumb() || provider.getModificationCount() != 0) {
                // empty the tree view during indexing and until the config has been initialized
                return SimpleNode.NO_CHILDREN;
            }
            final List<SimpleNode> children = Lists.newArrayList();
            for (GraphQLConfig config : provider.getAllConfigs()) {
                children.add(new GraphQLConfigSchemaNode(myProject, this, config, null));
            }
            if (children.isEmpty()) {
                children.add(new GraphQLDefaultSchemaNode(myProject, this));
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
