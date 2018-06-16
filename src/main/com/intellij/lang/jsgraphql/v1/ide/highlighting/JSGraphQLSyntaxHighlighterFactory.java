/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.highlighting;

import com.intellij.lang.jsgraphql.v1.schema.JSGraphQLSchemaFileType;
import com.intellij.openapi.fileTypes.PlainSyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class JSGraphQLSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
    @NotNull
    @Override
    public SyntaxHighlighter getSyntaxHighlighter(Project project, VirtualFile virtualFile) {
        final boolean schema = virtualFile != null && virtualFile.getFileType() == JSGraphQLSchemaFileType.INSTANCE;
        if(project == null) {
            Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
            if(openProjects.length > 0) {
                project = openProjects[0];
            } else {
                // can't syntax highlight GraphQL without a project to associate the language service instance to
                return new PlainSyntaxHighlighter();
            }
        }
        return new JSGraphQLSyntaxHighlighter(project, schema);
    }
}