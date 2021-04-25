/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInspection;
import com.intellij.lang.jsgraphql.schema.GraphQLUnexpectedSchemaError;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.treeStructure.CachingSimpleNode;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.util.ExceptionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.util.List;

/**
 * Tree node for an error in a GraphQL schema
 */
public class GraphQLSchemaErrorNode extends CachingSimpleNode {

    private final GraphQLError myError;

    public GraphQLSchemaErrorNode(@Nullable SimpleNode parent, @NotNull GraphQLError error) {
        super(parent);
        myError = error;
        myName = error.getMessage();
        SourceLocation location = getLocation();
        if (location != null) {
            String tooltip = getTooltip(location);
            if (tooltip != null) {
                getTemplatePresentation().setTooltip(tooltip);
            }
        } else if (error instanceof GraphQLUnexpectedSchemaError) {
            getTemplatePresentation().setLocationString(" - double click to open stack trace");
        }

        setIconFromError(error);
    }

    @Nullable
    private static String getTooltip(@NotNull SourceLocation location) {
        if (location.getSourceName() == null || location.getLine() == -1 || location.getColumn() == -1) {
            return null;
        }
        return location.getSourceName() + ":" + location.getLine() + ":" + location.getColumn();
    }

    private void setIconFromError(GraphQLError error) {
        Icon icon = HighlightDisplayLevel.ERROR.getIcon();

        Node<?> node = error.getNode();
        if (getProject() != null && error.getInspectionClass() != null && node != null) {
            PsiElement element = node.getElement();
            if (element != null) {
                icon = GraphQLInspection.getHighlightDisplayLevel(error.getInspectionClass(), element).getIcon();
            }
        }

        setIcon(icon);
    }

    @Override
    public void handleDoubleClickOrEnter(SimpleTree tree, InputEvent inputEvent) {
        final SourceLocation location = getLocation();
        if (location != null && location.getSourceName() != null) {
            GraphQLTreeNodeNavigationUtil.openSourceLocation(myProject, location, false);
        } else if (myError instanceof GraphQLUnexpectedSchemaError) {
            String stackTrace = ExceptionUtil.getThrowableText(((GraphQLUnexpectedSchemaError) myError).getException());
            PsiFile file = PsiFileFactory.getInstance(myProject).createFileFromText("graphql-error.txt", PlainTextLanguage.INSTANCE, stackTrace);
            new OpenFileDescriptor(myProject, file.getVirtualFile()).navigate(true);
        }
    }

    @Override
    public SimpleNode[] buildChildren() {
        return SimpleNode.NO_CHILDREN;
    }

    @Override
    public boolean isAlwaysLeaf() {
        return true;
    }

    private SourceLocation getLocation() {
        final List<SourceLocation> locations = myError.getLocations();
        return locations != null && !locations.isEmpty() ? locations.get(0) : null;
    }
}
