/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus;

import com.google.common.collect.Lists;
import com.intellij.lang.jsgraphql.icons.GraphQLIcons;
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeProvider;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.treeStructure.CachingSimpleNode;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.util.SlowOperations;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Tree node that represents the default project schema when no config files exist
 */
public class GraphQLDefaultSchemaNode extends CachingSimpleNode {

    private final GraphQLSchemaInfo mySchemaInfo;

    protected GraphQLDefaultSchemaNode(@NotNull Project project, @NotNull GraphQLSchemasRootNode parent) {
        super(project, parent);
        myName = "Default project-wide schema";
        getPresentation().setLocationString(project.getPresentableUrl());
        getPresentation().setIcon(GraphQLIcons.Files.GraphQLSchema);
        mySchemaInfo = SlowOperations.allowSlowOperations(() -> ReadAction.compute(() -> {
            GlobalSearchScope globalScope = GraphQLScopeProvider.getInstance(project).getGlobalScope();
            return GraphQLSchemaProvider.getInstance(myProject).getSchemaInfo(globalScope);
        }));
    }

    @Override
    public SimpleNode[] buildChildren() {
        final List<SimpleNode> children = Lists.newArrayList(new GraphQLSchemaContentNode(this, mySchemaInfo));
        if (mySchemaInfo.getRegistryInfo().isProcessedGraphQL()) {
            children.add(new GraphQLSchemaErrorsListNode(this, mySchemaInfo));
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
        return new Object[]{"Default schema", mySchemaInfo};
    }
}
