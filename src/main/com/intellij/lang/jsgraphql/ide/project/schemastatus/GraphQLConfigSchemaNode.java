/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLNamedScope;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigData;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigEndpoint;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLResolvedConfigData;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaWithErrors;
import com.intellij.lang.jsgraphql.schema.GraphQLTypeDefinitionRegistryServiceImpl;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import org.apache.commons.lang3.StringUtils;
import org.fest.util.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Tree node that represents the a graphql-config schema
 */
public class GraphQLConfigSchemaNode extends SimpleNode {

    private final GraphQLSchemaWithErrors schemaWithErrors;
    private final GraphQLConfigManager configManager;
    private final GraphQLResolvedConfigData configData;
    private final VirtualFile configBaseDir;

    private final List<GraphQLConfigEndpoint> endpoints;
    private final GraphQLFile configurationEntryFile;

    private Map<String, GraphQLResolvedConfigData> projectsConfigData;

    protected GraphQLConfigSchemaNode(Project project, GraphQLConfigManager configManager, GraphQLResolvedConfigData configData, VirtualFile configBaseDir) {
        super(project);
        this.configManager = configManager;
        this.configData = configData;
        this.configBaseDir = configBaseDir;
        if (configData.name != null && !configData.name.isEmpty()) {
            myName = configData.name;
        } else {
            // use the last part of the folder as name
            myName = StringUtils.substringAfterLast(configBaseDir.getPath(), "/");
        }
        getPresentation().setLocationString(configBaseDir.getPresentableUrl());
        getPresentation().setIcon(JSGraphQLIcons.Files.GraphQLSchema);

        final GraphQLTypeDefinitionRegistryServiceImpl registry = GraphQLTypeDefinitionRegistryServiceImpl.getService(myProject);

        configurationEntryFile = configManager.getConfigurationEntryFile(configData);
        endpoints = configManager.getEndpoints(configurationEntryFile.getVirtualFile());
        schemaWithErrors = registry.getSchemaWithErrors(configurationEntryFile);

        if (configData instanceof GraphQLConfigData) {
            projectsConfigData = ((GraphQLConfigData) configData).projects;
        }

    }

    /**
     * Gets whether this node contains a schema that includes the specified file
     */
    public boolean representsFile(VirtualFile virtualFile) {
        if (virtualFile != null) {
            final GraphQLNamedScope schemaScope = configManager.getSchemaScope(configurationEntryFile.getVirtualFile());
            if (schemaScope != null) {
                if (schemaScope.getPackageSet().includesVirtualFile(virtualFile)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void update(PresentationData presentation) {
        super.update(presentation);
        final int style = representsCurrentFile() ? SimpleTextAttributes.STYLE_BOLD : SimpleTextAttributes.STYLE_PLAIN;
        presentation.addText(getName(), new SimpleTextAttributes(style, getColor()));
    }

    @Override
    public SimpleNode[] getChildren() {
        final List<SimpleNode> children = Lists.newArrayList(
                new GraphQLSchemaContentNode(myProject, this, schemaWithErrors),
                new GraphQLSchemaErrorsListNode(myProject, schemaWithErrors)
        );
        if (projectsConfigData != null && !projectsConfigData.isEmpty()) {
            children.add(new GraphQLConfigProjectsNode(this));
        }
        children.add(new GraphQLSchemaEndpointsListNode(myProject, endpoints));
        return children.toArray(SimpleNode.NO_CHILDREN);
    }

    @Override
    public boolean isAutoExpandNode() {
        return representsCurrentFile();
    }

    @NotNull
    @Override
    public Object[] getEqualityObjects() {
        return new Object[]{configData, schemaWithErrors};
    }

    private boolean representsCurrentFile() {
        return representsFile(FileEditorManagerEx.getInstanceEx(myProject).getCurrentFile());
    }

    private static class GraphQLConfigProjectsNode extends SimpleNode {

        private final GraphQLConfigSchemaNode parent;

        public GraphQLConfigProjectsNode(GraphQLConfigSchemaNode parent) {
            super(parent.myProject);
            this.parent = parent;
            myName = "Projects";
            setIcon(AllIcons.Nodes.Folder);
        }

        @Override
        public SimpleNode[] getChildren() {
            if (parent.projectsConfigData != null) {
                return parent.projectsConfigData.values().stream().map(config -> {
                    return new GraphQLConfigSchemaNode(myProject, parent.configManager, config, parent.configBaseDir);
                }).toArray(SimpleNode[]::new);
            }
            return SimpleNode.NO_CHILDREN;
        }

        @Override
        public boolean isAutoExpandNode() {
            return true;
        }
    }
}
