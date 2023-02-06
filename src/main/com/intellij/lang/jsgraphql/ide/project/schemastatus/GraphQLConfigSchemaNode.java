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
import com.intellij.lang.jsgraphql.icons.GraphQLIcons;
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig;
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigEndpoint;
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.CachingSimpleNode;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.util.SlowOperations;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Tree node that represents a graphql-config schema
 */
public class GraphQLConfigSchemaNode extends CachingSimpleNode {

    private final GraphQLConfig myConfig;
    private final @Nullable GraphQLProjectConfig myProjectConfig;
    private final @Nullable GraphQLSchemaInfo mySchemaInfo;
    private final @Nullable List<GraphQLConfigEndpoint> myEndpoints;
    private final boolean myPerformSchemaDiscovery;
    private final boolean myIsProjectLevelNode;

    protected GraphQLConfigSchemaNode(@NotNull Project project,
                                      @NotNull SimpleNode parent,
                                      @NotNull GraphQLConfig config,
                                      @Nullable GraphQLProjectConfig projectConfig) {
        super(project, parent);
        myConfig = config;
        // use the last part of the folder as name
        myName = StringUtils.substringAfterLast(config.getDir().getPath(), "/");
        myIsProjectLevelNode = projectConfig != null;

        getPresentation().setIcon(GraphQLIcons.Files.GraphQLSchema);
        getPresentation().setLocationString(config.getDir().getPresentableUrl());

        GraphQLProjectConfig defaultProjectConfig = null;
        if (!myIsProjectLevelNode && config.hasOnlyDefaultProject()) {
            defaultProjectConfig = config.getDefault();
        }

        // this node is only considered a "real" schema that should be discovered if the config file doesn't use projects
        // if the config uses projects we can't do discovery at the root level as that's likely to consider multiple distinct schemas
        // as one with resulting re-declaration errors during validation
        myPerformSchemaDiscovery = myIsProjectLevelNode || defaultProjectConfig != null;

        if (myPerformSchemaDiscovery) {
            myProjectConfig = projectConfig != null ? projectConfig : defaultProjectConfig;
            myEndpoints = myProjectConfig.getEndpoints();
            mySchemaInfo = SlowOperations.allowSlowOperations(() -> ReadAction.compute(() -> {
                GlobalSearchScope scope = myProjectConfig.getScope();
                return GraphQLSchemaProvider.getInstance(myProject).getSchemaInfo(scope);
            }));
        } else {
            myEndpoints = null;
            mySchemaInfo = null;
            myProjectConfig = null;
        }
    }

    /**
     * Gets whether this node contains a schema that includes the specified file
     */
    public boolean representsFile(@Nullable VirtualFile virtualFile) {
        if (virtualFile != null) {
            if (virtualFile.equals(getConfigFile())) {
                return true;
            }
            if (myPerformSchemaDiscovery) {
                GlobalSearchScope scope = myProjectConfig != null ? myProjectConfig.getScope() : null;
                if (scope != null) {
                    return scope.contains(virtualFile);
                }
            }
        }
        return false;
    }

    public @Nullable VirtualFile getConfigFile() {
        return myProjectConfig != null ? myProjectConfig.getFile() : null;
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
        if (myPerformSchemaDiscovery && mySchemaInfo != null) {
            children.add(new GraphQLSchemaContentNode(this, mySchemaInfo));
            if (mySchemaInfo.getRegistryInfo().isProcessedGraphQL()) {
                children.add(new GraphQLSchemaErrorsListNode(this, mySchemaInfo));
            }
        }
        if (!myIsProjectLevelNode && !myConfig.hasOnlyDefaultProject()) {
            children.add(new GraphQLConfigProjectsNode(this));
        }
        if (myProjectConfig != null && myEndpoints != null) {
            final String projectKey = myProjectConfig.getName();
            children.add(new GraphQLSchemaEndpointsListNode(this, projectKey, myEndpoints));
        }
        return children.toArray(SimpleNode.NO_CHILDREN);
    }

    @Override
    public boolean isAutoExpandNode() {
        return representsCurrentFile() || !myPerformSchemaDiscovery;
    }

    @Override
    public Object @NotNull [] getEqualityObjects() {
        return new Object[]{myConfig, mySchemaInfo};
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

        public GraphQLConfigProjectsNode(@NotNull GraphQLConfigSchemaNode parent) {
            super(parent.myProject, parent);
            this.parent = parent;
            myName = "Projects";
            setIcon(AllIcons.Nodes.Folder);
        }

        @Override
        public SimpleNode[] buildChildren() {
            GraphQLConfig config = parent.myConfig;
            if (config != null) {
                try {
                    return config.getProjects().values().stream()
                            .map(projectConfig -> new GraphQLConfigSchemaNode(myProject, this, config, projectConfig))
                            .toArray(SimpleNode[]::new);
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
