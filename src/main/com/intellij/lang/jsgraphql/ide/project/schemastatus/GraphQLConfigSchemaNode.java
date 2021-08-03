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
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.icons.GraphQLIcons;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLNamedScope;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigData;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigEndpoint;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLResolvedConfigData;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.CachingSimpleNode;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.util.SlowOperations;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Tree node that represents the a graphql-config schema
 */
public class GraphQLConfigSchemaNode extends CachingSimpleNode {

    private final GraphQLSchemaInfo mySchemaInfo;
    private final GraphQLConfigManager configManager;
    private final GraphQLResolvedConfigData configData;
    private final VirtualFile configBaseDir;
    private final VirtualFile configFile;

    @Nullable
    private final List<GraphQLConfigEndpoint> endpoints;
    private final GraphQLFile configurationEntryFile;

    private Map<String, GraphQLResolvedConfigData> projectsConfigData;
    private boolean performSchemaDiscovery = true;

    protected GraphQLConfigSchemaNode(Project project,
                                      SimpleNode parent,
                                      GraphQLConfigManager configManager,
                                      GraphQLResolvedConfigData configData,
                                      VirtualFile configBaseDir) {
        super(project, parent);
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

        getPresentation().setIcon(GraphQLIcons.Files.GraphQLSchema);

        if (configData instanceof GraphQLConfigData) {
            getPresentation().setLocationString(configBaseDir.getPresentableUrl());
            projectsConfigData = ((GraphQLConfigData) configData).projects;
            // this node is only considered a "real" schema that should be discovered if the config file doesn't use projects
            // if the config uses projects we can't do discovery at the root level as that's likely to consider multiple distinct schemas
            // as one with resulting re-declaration errors during validation
            performSchemaDiscovery = projectsConfigData == null || projectsConfigData.isEmpty();
        }

        if (performSchemaDiscovery) {
            final GraphQLSchemaProvider registry = GraphQLSchemaProvider.getInstance(myProject);
            configurationEntryFile = configManager.getConfigurationEntryFile(configData);
            endpoints = GraphQLFileType.isGraphQLFile(project, configurationEntryFile.getVirtualFile())
                ? configManager.getEndpoints(configurationEntryFile.getVirtualFile()) : null;
            mySchemaInfo = SlowOperations.allowSlowOperations(() -> registry.getSchemaInfo(configurationEntryFile));
        } else {
            mySchemaInfo = null;
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
    public SimpleNode[] buildChildren() {
        final List<SimpleNode> children = Lists.newArrayList();
        if (performSchemaDiscovery) {
            children.add(new GraphQLSchemaContentNode(this, mySchemaInfo));
            if (mySchemaInfo.getRegistryInfo().isProcessedGraphQL()) {
                children.add(new GraphQLSchemaErrorsListNode(this, mySchemaInfo));
            }
        }
        if (projectsConfigData != null && !projectsConfigData.isEmpty()) {
            children.add(new GraphQLConfigProjectsNode(this));
        }
        if (endpoints != null) {
            final String projectKey = this.configData instanceof GraphQLConfigData ? null : this.configData.name;
            children.add(new GraphQLSchemaEndpointsListNode(this, projectKey, endpoints));
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
        return new Object[]{configData, mySchemaInfo};
    }

    private boolean representsCurrentFile() {
        final Ref<Boolean> represents = Ref.create(false);
        final FileEditorManagerEx fileEditorManagerEx = FileEditorManagerEx.getInstanceEx(myProject);
        UIUtil.invokeLaterIfNeeded(() -> {
            represents.set(representsFile(fileEditorManagerEx.getCurrentFile()));
        });
        return represents.get();
    }

    private static class GraphQLConfigProjectsNode extends CachingSimpleNode {

        private final GraphQLConfigSchemaNode parent;

        public GraphQLConfigProjectsNode(GraphQLConfigSchemaNode parent) {
            super(parent.myProject, parent);
            this.parent = parent;
            myName = "Projects";
            setIcon(AllIcons.Nodes.Folder);
        }

        @Override
        public SimpleNode[] buildChildren() {
            if (parent.projectsConfigData != null) {
                try {
                    return parent.projectsConfigData.values().stream().map(config -> {
                        return new GraphQLConfigSchemaNode(myProject, this, parent.configManager, config, parent.configBaseDir);
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
