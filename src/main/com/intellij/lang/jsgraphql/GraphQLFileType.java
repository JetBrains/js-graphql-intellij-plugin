/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql;

import com.intellij.ide.scratch.ScratchUtil;
import com.intellij.lang.jsgraphql.icons.GraphQLIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GraphQLFileType extends LanguageFileType {
    public static final GraphQLFileType INSTANCE = new GraphQLFileType();

    private GraphQLFileType() {
        super(GraphQLLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return GraphQLConstants.GraphQL;
    }

    @NotNull
    @Override
    public String getDescription() {
        return getName();
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "graphql";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return GraphQLIcons.FILE;
    }

    /**
     * Scratch virtual files don't return their actual file type, so we need to find the PsiFile to determine
     * whether it's a GraphQL scratch file
     *
     * @return true if the scratch file contains a GraphQL PsiFile
     */
    public static boolean isGraphQLScratchFile(@NotNull Project project, @NotNull VirtualFile file) {
        if (ScratchUtil.isScratch(file)) {
            final PsiManager psiManager = PsiManager.getInstance(project);
            try {
                final PsiFile psiFile = psiManager.findFile(file);
                if (psiFile != null && psiFile.getFileType() == GraphQLFileType.INSTANCE) {
                    return true;
                }
            } catch (ProcessCanceledException e) {
                // can be thrown from psiManager.findFile
            }
        }
        return false;
    }

    public static boolean isGraphQLFile(@NotNull Project project, @Nullable VirtualFile virtualFile) {
        if (virtualFile == null) {
            return false;
        }

        return virtualFile.getFileType() == GraphQLFileType.INSTANCE || GraphQLFileType.isGraphQLScratchFile(project, virtualFile);
    }

}
