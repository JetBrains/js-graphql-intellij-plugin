/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus;

import com.intellij.icons.AllIcons;
import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.lang.jsgraphql.icons.GraphQLIcons;
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigEndpoint;
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLIntrospectionService;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.treeStructure.CachingSimpleNode;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;

import static com.intellij.lang.jsgraphql.ide.config.GraphQLConfigConstants.GRAPHQLCONFIG_COMMENT;
import static com.intellij.lang.jsgraphql.ui.GraphQLUIProjectService.GRAPH_QL_ENDPOINTS_MODEL;

/**
 * Tree node which provides a schema endpoints list
 */
public class GraphQLSchemaEndpointsListNode extends CachingSimpleNode {

    @Nullable
    private final String projectKey;

    @Nullable
    private final List<GraphQLConfigEndpoint> endpoints;

    public GraphQLSchemaEndpointsListNode(@Nullable SimpleNode parent,
                                          @Nullable String projectKey,
                                          @Nullable List<GraphQLConfigEndpoint> endpoints) {
        super(parent);
        this.projectKey = projectKey;
        this.endpoints = endpoints;
        myName = "Endpoints";
        setIcon(AllIcons.Nodes.WebFolder);
    }

    @Override
    public SimpleNode[] buildChildren() {
        if (endpoints == null || endpoints.isEmpty()) {
            return new SimpleNode[]{new DefaultEndpointNode(myProject)};
        } else {
            return endpoints.stream().map(endpoint -> new ConfigurableEndpointNode(this, projectKey, endpoint)).toArray(SimpleNode[]::new);
        }
    }

    @Override
    public boolean isAutoExpandNode() {
        return true;
    }

    private static class ConfigurableEndpointNode extends SimpleNode {

        private final @Nullable String projectKey;
        private final @NotNull GraphQLConfigEndpoint endpoint;

        public ConfigurableEndpointNode(@Nullable SimpleNode parent, @Nullable String projectKey, @NotNull GraphQLConfigEndpoint endpoint) {
            super(parent);
            this.projectKey = projectKey;
            this.endpoint = endpoint;
            myName = endpoint.getName();
            getTemplatePresentation().setTooltip("Endpoints allow you to perform GraphQL introspection, queries and mutations");
            getTemplatePresentation().setLocationString(endpoint.getUrl());
            setIcon(GraphQLIcons.UI.GraphQLNode);
        }

        @Override
        public void handleDoubleClickOrEnter(SimpleTree tree, InputEvent inputEvent) {
            final String introspect = "Get GraphQL Schema from Endpoint (introspection)";
            final String createScratch = "New GraphQL Scratch File (for query, mutation testing)";
            ListPopup listPopup = JBPopupFactory.getInstance().createListPopup(
                new BaseListPopupStep<>("Choose Endpoint Action", introspect, createScratch) {

                    @Override
                    public PopupStep onChosen(String selectedValue, boolean finalChoice) {
                        return doFinalStep(() -> {
                            if (introspect.equals(selectedValue)) {
                                GraphQLIntrospectionService.getInstance(myProject)
                                    .performIntrospectionQueryAndUpdateSchemaPathFile(myProject, endpoint);
                            } else if (createScratch.equals(selectedValue)) {
                                final String configBaseDir = endpoint.getDir().getPresentableUrl();
                                final String text = "# " + GRAPHQLCONFIG_COMMENT + configBaseDir + "!" +
                                    Optional.ofNullable(projectKey).orElse("") + "\n\nquery ScratchQuery {\n\n}";
                                final VirtualFile scratchFile = ScratchRootType.getInstance().createScratchFile(myProject,
                                    "scratch.graphql", GraphQLLanguage.INSTANCE, text);
                                if (scratchFile != null) {
                                    FileEditor[] fileEditors = FileEditorManager.getInstance(myProject).openFile(scratchFile, true);
                                    for (FileEditor editor : fileEditors) {
                                        if (editor instanceof TextEditor) {
                                            final GraphQLEndpointsModel endpointsModel = ((TextEditor) editor).getEditor().getUserData(
                                                GRAPH_QL_ENDPOINTS_MODEL);
                                            if (endpointsModel != null) {
                                                endpointsModel.setSelectedItem(endpoint);
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                });
            if (inputEvent instanceof KeyEvent) {
                listPopup.showInFocusCenter();
            } else if (inputEvent instanceof MouseEvent) {
                listPopup.show(new RelativePoint((MouseEvent) inputEvent));
            }
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

        public DefaultEndpointNode(@NotNull Project project) {
            super(project);
            myName = "No endpoints available in the default schema";
            getTemplatePresentation().setTooltip("Endpoints allow you to perform GraphQL introspection, queries and mutations");
            getTemplatePresentation().setLocationString(
                "- Click the \"+\" button to create a schema configuration with configurable endpoints");
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
