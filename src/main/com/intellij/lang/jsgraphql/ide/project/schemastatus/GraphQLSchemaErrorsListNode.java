/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus;

import com.google.common.collect.Lists;
import com.intellij.icons.AllIcons;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaWithErrors;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.SimpleNode;
import graphql.GraphQLError;

import java.util.List;

/**
 * Tree node with children for each error in a GraphQL schema
 */
public class GraphQLSchemaErrorsListNode extends SimpleNode {

    private final GraphQLSchemaWithErrors schemaWithErrors;

    public GraphQLSchemaErrorsListNode(Project project, GraphQLSchemaWithErrors schemaWithErrors) {
        super(project);
        this.schemaWithErrors = schemaWithErrors;
        myName = "Schema errors";
        setIcon(AllIcons.Nodes.TreeClosed);
    }

    @Override
    public SimpleNode[] getChildren() {
        final List<SimpleNode> children = Lists.newArrayList();
        for (GraphQLError error : schemaWithErrors.getErrors()) {
            children.add(new GraphQLSchemaErrorNode(myProject, error));
        }
        if (children.isEmpty()) {
            SimpleNode noErrors = new SimpleNode() {
                @Override
                public SimpleNode[] getChildren() {
                    return NO_CHILDREN;
                }

                @Override
                public String getName() {
                    return "No errors found";
                }
            };
            noErrors.setIcon(AllIcons.General.InspectionsOK);
            children.add(noErrors);
        }

        return children.toArray(SimpleNode.NO_CHILDREN);
    }

    @Override
    public boolean isAutoExpandNode() {
        return true;
    }
}
