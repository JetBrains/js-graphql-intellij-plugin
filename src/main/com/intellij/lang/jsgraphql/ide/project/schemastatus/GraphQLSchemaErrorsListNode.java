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
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo;
import com.intellij.ui.treeStructure.CachingSimpleNode;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.lang.jsgraphql.types.GraphQLError;

import java.util.List;

/**
 * Tree node with children for each error in a GraphQL schema
 */
public class GraphQLSchemaErrorsListNode extends CachingSimpleNode {

    private final GraphQLSchemaInfo myValidatedSchema;

    public GraphQLSchemaErrorsListNode(SimpleNode parent, GraphQLSchemaInfo validatedSchema) {
        super(parent);
        myValidatedSchema = validatedSchema;
        myName = "Schema errors";
        setIcon(AllIcons.Nodes.Folder);
    }

    @Override
    public SimpleNode[] buildChildren() {
        if  (!myValidatedSchema.getRegistryInfo().isProcessedGraphQL()) {
            // no GraphQL PSI files parse yet, so no need to show the "no query defined" error for a non-existing schema
            return SimpleNode.NO_CHILDREN;
        }
        final List<SimpleNode> children = Lists.newArrayList();
        for (GraphQLError error : myValidatedSchema.getErrors()) {
            children.add(new GraphQLSchemaErrorNode(this, error));
        }
        if (children.isEmpty()) {
            SimpleNode noErrors = new SimpleNode(this) {
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
