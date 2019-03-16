/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus;

import com.google.common.collect.Lists;
import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons;
import com.intellij.lang.jsgraphql.ide.project.GraphQLPsiSearchHelper;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaWithErrors;
import com.intellij.lang.jsgraphql.schema.GraphQLTypeDefinitionRegistryServiceImpl;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.SimpleNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Tree node that represents the default project schema when no .graphqlconfig files exist
 */
public class GraphQLDefaultSchemaNode extends SimpleNode {

    private final GraphQLSchemaWithErrors schemaWithErrors;

    protected GraphQLDefaultSchemaNode(Project project) {
        super(project);
        myName = "Default project-wide schema";
        getPresentation().setLocationString(project.getPresentableUrl());
        getPresentation().setIcon(JSGraphQLIcons.Files.GraphQLSchema);
        final GraphQLFile defaultProjectFile = GraphQLPsiSearchHelper.getService(myProject).getDefaultProjectFile();
        final GraphQLTypeDefinitionRegistryServiceImpl registry = GraphQLTypeDefinitionRegistryServiceImpl.getService(myProject);
        schemaWithErrors = registry.getSchemaWithErrors(defaultProjectFile);
    }

    @Override
    public SimpleNode[] getChildren() {
        final List<SimpleNode> children = Lists.newArrayList(new GraphQLSchemaContentNode(this, schemaWithErrors));
        if (schemaWithErrors.getRegistry().isProcessedGraphQL()) {
            children.add(new GraphQLSchemaErrorsListNode(this, schemaWithErrors));
        }
        children.add(new GraphQLSchemaEndpointsListNode(this, null, null));
        return children.toArray(SimpleNode.NO_CHILDREN);
    }

    @Override
    public boolean isAutoExpandNode() {
        return true;
    }

    @NotNull
    @Override
    public Object[] getEqualityObjects() {
        return new Object[]{"Default schema", schemaWithErrors};
    }
}
