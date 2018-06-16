/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.schema.ide.project;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons;
import com.intellij.lang.jsgraphql.v1.schema.psi.JSGraphQLSchemaFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

/**
 * A "GraphQL Schemas" node in the root project tree view.
 * Shows the currently loaded schema file, if any, as its child node.
 * @see JSGraphQLSchemaFileNode
 */
public class JSGraphQLSchemaDirectoryNode extends ProjectViewNode<String> {

    public static final String GRAPHQL_SCHEMAS_LABEL = "GraphQL Schemas";

    private static final Key<Boolean> IS_GRAPH_QL_SCHEMA_NODE_SHOWN = Key.create("JSGraphQL.schema.node.shown");

    private JSGraphQLSchemaFileNode fileNode;

    public JSGraphQLSchemaDirectoryNode(Project project, ViewSettings viewSettings) {
        super(project, GRAPHQL_SCHEMAS_LABEL, viewSettings);
        project.putUserData(IS_GRAPH_QL_SCHEMA_NODE_SHOWN, true);
    }

    @Override
    public boolean contains(@NotNull VirtualFile file) {
        return JSGraphQLSchemaLanguageProjectService.isProjectSchemaFile(file);
    }

    @NotNull
    @Override
    public Collection<? extends AbstractTreeNode> getChildren() {
        if(fileNode == null) {
            final Project project = getProject();
            if(project != null) {
                final JSGraphQLSchemaFile schemaFile = JSGraphQLSchemaLanguageProjectService.getService(project).getSchemaFile();
                if (schemaFile != null) {
                    fileNode = new JSGraphQLSchemaFileNode(project, schemaFile, getSettings());
                }
            }
        }
        return fileNode != null ? Collections.singleton(fileNode) : Collections.emptyList();
    }

    @Override
    protected void update(PresentationData presentation) {
        presentation.setPresentableText(GRAPHQL_SCHEMAS_LABEL);
        presentation.setIcon(JSGraphQLIcons.UI.GraphQLNode);
    }


    public static boolean isShownForProject(Project project) {
        return Boolean.TRUE.equals(project.getUserData(IS_GRAPH_QL_SCHEMA_NODE_SHOWN));
    }

}
