/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi;

import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLDescriptionAware;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLTypeNameDefinitionOwner;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLTypeNameExtensionOwner;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GraphQLPsiUtil {

    public static @Nullable String getTypeName(@Nullable PsiElement psiElement, @Nullable Ref<GraphQLIdentifier> typeNameRef) {

        if (psiElement != null) {

            final PsiElement typeOwner = PsiTreeUtil.getParentOfType(psiElement, GraphQLTypeNameDefinitionOwner.class, GraphQLTypeNameExtensionOwner.class);

            GraphQLIdentifier nameIdentifier = null;

            if (typeOwner instanceof GraphQLTypeNameDefinitionOwner) {
                final GraphQLTypeNameDefinition typeNameDefinition = ((GraphQLTypeNameDefinitionOwner) typeOwner).getTypeNameDefinition();
                if (typeNameDefinition != null) {
                    nameIdentifier = typeNameDefinition.getNameIdentifier();
                }
            } else if (typeOwner instanceof GraphQLTypeNameExtensionOwner) {
                final GraphQLTypeName typeName = ((GraphQLTypeNameExtensionOwner) typeOwner).getTypeName();
                if (typeName != null) {
                    nameIdentifier = typeName.getNameIdentifier();
                }
            }

            if (nameIdentifier != null) {
                if (typeNameRef != null) {
                    typeNameRef.set(nameIdentifier);
                }
                return nameIdentifier.getText();
            }

        }

        return null;
    }

    @Nullable
    public static VirtualFile getOriginalVirtualFile(@Nullable PsiFile containingFile) {
        if (containingFile == null || !containingFile.isValid()) return null;

        VirtualFile file = containingFile.getVirtualFile();
        if (file == null) {
            PsiFile originalFile = containingFile.getOriginalFile();
            if (originalFile != containingFile && originalFile.isValid()) {
                file = originalFile.getVirtualFile();
            }
        }

        return file;
    }

    @Nullable
    public static VirtualFile getPhysicalVirtualFile(@Nullable VirtualFile virtualFile) {
        if (virtualFile == null) return null;

        if (virtualFile instanceof LightVirtualFile) {
            VirtualFile originalFile = ((LightVirtualFile) virtualFile).getOriginalFile();
            if (originalFile != null) {
                virtualFile = originalFile;
            }
        }

        if (virtualFile instanceof VirtualFileWindow) {
            // injected virtual files
            virtualFile = ((VirtualFileWindow) virtualFile).getDelegate();
        }
        return virtualFile;
    }

    @Nullable
    public static VirtualFile getPhysicalVirtualFile(@Nullable PsiFile psiFile) {
        if (psiFile == null) return null;
        return getPhysicalVirtualFile(getOriginalVirtualFile(psiFile));
    }

    /**
     * Gets the virtual file system path of a PSI file
     */
    @NotNull
    public static String getFileName(@NotNull PsiFile psiFile) {
        VirtualFile virtualFile = getPhysicalVirtualFile(psiFile);
        if (virtualFile != null) {
            return virtualFile.getPath();
        }
        return psiFile.getName();
    }

    public static @NotNull List<PsiComment> getLeadingFileComments(@NotNull PsiFile file) {
        List<PsiComment> comments = new SmartList<>();
        PsiElement child = file.getFirstChild();
        if (child instanceof PsiWhiteSpace) {
            child = PsiTreeUtil.skipWhitespacesForward(child);
        }
        while (child instanceof PsiComment) {
            comments.add(((PsiComment) child));
            child = PsiTreeUtil.skipWhitespacesForward(child);
        }

        return comments;
    }

    @NotNull
    public static PsiElement skipDescription(@NotNull PsiElement element) {
        if (element instanceof GraphQLDescriptionAware) {
            GraphQLDescription description = ((GraphQLDescriptionAware) element).getDescription();
            if (description != null) {
                PsiElement target = PsiTreeUtil.skipWhitespacesForward(description);
                if (target != null) {
                    return target;
                }
            }
        }

        return element;
    }
}
