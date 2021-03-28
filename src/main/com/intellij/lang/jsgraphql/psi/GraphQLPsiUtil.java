/**
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi;

import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLTypeNameDefinitionOwnerPsiElement;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLTypeNameExtensionOwnerPsiElement;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraphQLPsiUtil {

    public static @Nullable String getTypeName(@Nullable PsiElement psiElement, @Nullable Ref<GraphQLIdentifier> typeNameRef) {

        if (psiElement != null) {

            final PsiElement typeOwner = PsiTreeUtil.getParentOfType(psiElement, GraphQLTypeNameDefinitionOwnerPsiElement.class, GraphQLTypeNameExtensionOwnerPsiElement.class);

            GraphQLIdentifier nameIdentifier = null;

            if (typeOwner instanceof GraphQLTypeNameDefinitionOwnerPsiElement) {
                final GraphQLTypeNameDefinition typeNameDefinition = ((GraphQLTypeNameDefinitionOwnerPsiElement) typeOwner).getTypeNameDefinition();
                if (typeNameDefinition != null) {
                    nameIdentifier = typeNameDefinition.getNameIdentifier();
                }
            } else if (typeOwner instanceof GraphQLTypeNameExtensionOwnerPsiElement) {
                final GraphQLTypeName typeName = ((GraphQLTypeNameExtensionOwnerPsiElement) typeOwner).getTypeName();
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
}
