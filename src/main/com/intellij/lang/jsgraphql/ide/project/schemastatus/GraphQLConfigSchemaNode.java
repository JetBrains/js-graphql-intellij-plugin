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
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import org.apache.commons.lang.StringUtils;
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
    private final VirtualFile configFile;

    private final List<GraphQLConfigEndpoint> endpoints;
    private final GraphQLFile configurationEntryFile;

    private Map<String, GraphQLResolvedConfigData> projectsConfigData;
    private boolean performSchemaDiscovery = true;

    protected GraphQLConfigSchemaNode(Project project, GraphQLConfigManager configManager, GraphQLResolvedConfigData configData, VirtualFile configBaseDir) {
        super(project);
        this.configManager = configManager;
        this.configData = configData;
        this.configBaseDir = configBaseDir;
        this.configFile = configManager.getClosestConfigFile(configBaseDir);
        if (configData.name != null && !configData.name.isEmpty()) {
            myName = configData.name;
        } else {
            // use the last part of the folder as name
            myName = StringUtils.substringAfterLast(configBaseDir.getPath(), "/");
        }

        getPresentation().setIcon(JSGraphQLIcons.Files.GraphQLSchema);

        if (configData instanceof GraphQLConfigData) {
            getPresentation().setLocationString(configBaseDir.getPresentableUrl());
            projectsConfigData = ((GraphQLConfigData) configData).projects;
            // this node is only considered a "real" schema that should be discovered if the config file doesn't use projects
            // if the config uses projects we can't do discovery at the root level as that's likely to consider multiple distinct schemas
            // as one with resulting re-declaration errors during validation
            performSchemaDiscovery = projectsConfigData == null || projectsConfigData.isEmpty();
        }

        if (performSchemaDiscovery) {
            final GraphQLTypeDefinitionRegistryServiceImpl registry = GraphQLTypeDefinitionRegistryServiceImpl.getService(myProject);
            configurationEntryFile = configManager.getConfigurationEntryFile(configData);
            endpoints = configManager.getEndpoints(configurationEntryFile.getVirtualFile());
            schemaWithErrors = registry.getSchemaWithErrors(configurationEntryFile);
        } else {
            schemaWithErrors = null;
            endpoints = null;
            configurationEntryFile = null;
        }
    }

    /**
     * Gets whether this node contains a schema that includes the specified file
     */
    public boolean representsFile(VirtualFile virtualFile) {
        if (virtualFile != null) {
            if (virtualFile.equals(configFile)) {
                return true;
            }
            if (performSchemaDiscovery) {
                final GraphQLNamedScope schemaScope = configManager.getSchemaScope(configurationEntryFile.getVirtualFile());
                if (schemaScope != null) {
                    if (schemaScope.getPackageSet().includesVirtualFile(virtualFile)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public VirtualFile getConfigFile() {
        return configFile;
    }

    @Override
    protected void update(PresentationData presentation) {
        super.update(presentation);
        final int style = representsCurrentFile() ? SimpleTextAttributes.STYLE_BOLD : SimpleTextAttributes.STYLE_PLAIN;
        presentation.addText(getName(), new SimpleTextAttributes(style, getColor()));
    }

    @Override
    public SimpleNode[] getChildren() {
        final List<SimpleNode> children = Lists.newArrayList();
        if (performSchemaDiscovery) {
            children.add(new GraphQLSchemaContentNode(this, schemaWithErrors));
            children.add(new GraphQLSchemaErrorsListNode(this, schemaWithErrors));
        }
        if (projectsConfigData != null && !projectsConfigData.isEmpty()) {
            children.add(new GraphQLConfigProjectsNode(this));
        }
        if (endpoints != null) {
            children.add(new GraphQLSchemaEndpointsListNode(this, endpoints));
        }
        return children.toArray(SimpleNode.NO_CHILDREN);
    }

    @Override
    public boolean isAutoExpandNode() {
        return representsCurrentFile() || !performSchemaDiscovery;
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
                try {
                    return parent.projectsConfigData.values().stream().map(config -> {
                        return new GraphQLConfigSchemaNode(myProject, parent.configManager, config, parent.configBaseDir);
                    }).toArray(SimpleNode[]::new);
                } catch (IndexNotReadyException ignored) {
                    // entered "dumb" mode, so just return no children as the tree view will be rebuilt as empty shortly (GraphQLSchemasRootNode)
                }
            }
            return SimpleNode.NO_CHILDREN;
        }

        @Override
        public boolean isAutoExpandNode() {
            return true;
        }
    }
}
