/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.schema;

import com.intellij.ide.scratch.ScratchFileType;
import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class JSGraphQLSchemaFileType extends LanguageFileType {

    public static final JSGraphQLSchemaFileType INSTANCE = new JSGraphQLSchemaFileType();

    private JSGraphQLSchemaFileType() {
        super(JSGraphQLSchemaLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "GraphQL Schema";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "GraphQL schema file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "graphqls";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return JSGraphQLIcons.Files.GraphQLSchema;
    }

    /**
     * Scratch virtual files don't return their actual file type, so we need to find the PsiFile to determine
     * whether it's a GraphQL schema scratch file
     * @return true if the scratch file contains a GraphQL Schema PsiFile
     */
    public static boolean isGraphQLScratchFile(Project project, VirtualFile file) {
        if(file.getFileType() instanceof ScratchFileType) {
            final PsiManager psiManager = PsiManager.getInstance(project);
            final PsiFile psiFile = psiManager.findFile(file);
            if(psiFile != null && psiFile.getFileType() == JSGraphQLSchemaFileType.INSTANCE) {
                return true;
            }
        }
        return false;
    }
}