/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.awt.event.InputEvent;
import java.util.List;

/**
 * Tree node for an error in a GraphQL schema
 */
public class GraphQLSchemaErrorNode extends SimpleNode {

    private final GraphQLError error;

    public GraphQLSchemaErrorNode(Project project, GraphQLError error) {
        super(project);
        this.error = error;
        myName = error.getMessage();
        setIcon(AllIcons.Ide.Error);
        SourceLocation location = getLocation();
        if (location != null) {
            getTemplatePresentation().setTooltip(location.getSourceName() + ":" + location.getLine() + ":" + location.getColumn());
        }
    }

    @Override
    public void handleDoubleClickOrEnter(SimpleTree tree, InputEvent inputEvent) {
        final SourceLocation location = getLocation();
        if (location != null && location.getSourceName() != null) {
            GraphQLTreeNodeNavigationUtil.openSourceLocation(myProject, location);
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

    private SourceLocation getLocation() {
        final List<SourceLocation> locations = error.getLocations();
        return locations != null && !locations.isEmpty() ? locations.get(0) : null;
    }
}
