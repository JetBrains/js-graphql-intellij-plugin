/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus;

import com.intellij.icons.AllIcons;
import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigEndpoint;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.SimpleNode;

import java.util.List;

/**
 * Tree node which provides a schema endpoints list
 */
public class GraphQLSchemaEndpointsListNode extends SimpleNode {

    private final List<GraphQLConfigEndpoint> endpoints;

    public GraphQLSchemaEndpointsListNode(Project project, List<GraphQLConfigEndpoint> endpoints) {
        super(project);
        this.endpoints = endpoints;
        myName = "Endpoints";
        setIcon(AllIcons.Nodes.WebFolder);
    }

    @Override
    public SimpleNode[] getChildren() {
        if (endpoints == null) {
            return new SimpleNode[]{new DefaultEndpointNode(myProject)};
        } else {
            return endpoints.stream().map(endpoint -> new ConfigurableEndpointNode(myProject, endpoint)).toArray(SimpleNode[]::new);
        }
    }

    @Override
    public boolean isAutoExpandNode() {
        return true;
    }

    private static class ConfigurableEndpointNode extends SimpleNode {

        public ConfigurableEndpointNode(Project project, GraphQLConfigEndpoint endpoint) {
            super(project);
            myName = endpoint.name;
            getTemplatePresentation().setTooltip("Endpoints allow you to perform GraphQL introspection, queries and mutations");
            getTemplatePresentation().setLocationString(endpoint.url);
            setIcon(JSGraphQLIcons.UI.GraphQLNode);
        }

        @Override
        public SimpleNode[] getChildren() {
            return SimpleNode.NO_CHILDREN;
        }

        @Override
        public boolean isAlwaysLeaf() {
            return true;
        }
    }

    private static class DefaultEndpointNode extends SimpleNode {

        public DefaultEndpointNode(Project project) {
            super(project);
            myName = "No endpoints available in the default schema";
            getTemplatePresentation().setTooltip("Endpoints allow you to perform GraphQL introspection, queries and mutations");
            getTemplatePresentation().setLocationString("- Click the \"+\" button to create a schema configuration with configurable endpoints");
            setIcon(AllIcons.General.Information);
        }

        @Override
        public SimpleNode[] getChildren() {
            return SimpleNode.NO_CHILDREN;
        }

        @Override
        public boolean isAlwaysLeaf() {
            return true;
        }
    }
}
