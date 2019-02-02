/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus;

import com.intellij.json.psi.JsonFile;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaKeys;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import graphql.language.SourceLocation;

public final class GraphQLTreeNodeNavigationUtil {

    public static void openSourceLocation(Project myProject, SourceLocation location, boolean resolveSDLFromJSON) {
        VirtualFile sourceFile = StandardFileSystems.local().findFileByPath(location.getSourceName());
        if (sourceFile != null) {
            PsiFile file = PsiManager.getInstance(myProject).findFile(sourceFile);
            if (file != null) {
                if(file instanceof JsonFile && resolveSDLFromJSON) {
                    GraphQLFile graphQLFile = file.getUserData(GraphQLSchemaKeys.GRAPHQL_INTROSPECTION_JSON_TO_SDL);
                    if (graphQLFile != null) {
                        // open the SDL file and not the JSON introspection file it was based on
                        file = graphQLFile;
                        sourceFile = file.getVirtualFile();
                    }
                }
                FileEditor[] fileEditors = FileEditorManager.getInstance(myProject).openFile(sourceFile, false);
                if (fileEditors.length > 0) {
                    if (fileEditors[0] instanceof TextEditor) {
                        int offset = ((TextEditor) fileEditors[0]).getEditor().logicalPositionToOffset(new LogicalPosition(
                                location.getLine() - 1,
                                location.getColumn() - 1
                        ));
                        final PsiElement errorElement = file.findElementAt(offset);
                        if (errorElement instanceof Navigatable) {
                            ((Navigatable) errorElement).navigate(true);
                        }
                    }
                }
            }
        }
    }
}
