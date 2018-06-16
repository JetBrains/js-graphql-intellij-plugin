/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.structureView;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.lang.jsgraphql.v1.JSGraphQLTokenTypes;
import com.intellij.lang.jsgraphql.v1.psi.JSGraphQLNamedPropertyPsiElement;
import com.intellij.lang.jsgraphql.v1.psi.JSGraphQLPsiElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class JSGraphQLStructureViewModel extends TextEditorBasedStructureViewModel implements StructureViewModel.ElementInfoProvider {

    private final JSGraphQLStructureViewTreeElement root;

    public JSGraphQLStructureViewModel(PsiElement root, Editor editor) {
        super(editor, root.getContainingFile());
        this.root = new JSGraphQLStructureViewTreeElement(root, root);
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        if(element instanceof JSGraphQLStructureViewTreeElement) {
            JSGraphQLStructureViewTreeElement treeElement = (JSGraphQLStructureViewTreeElement)element;
            if (treeElement.childrenBase instanceof LeafPsiElement) {
                return true;
            }
            if (treeElement.childrenBase instanceof JSGraphQLNamedPropertyPsiElement) {
                final PsiElement[] children = treeElement.childrenBase.getChildren();
                if (children.length == 0) {
                    // field with no sub selections, but we have to check if there's attributes
                    final PsiElement nextVisible = PsiTreeUtil.nextVisibleLeaf(treeElement.childrenBase);
                    if(nextVisible != null && nextVisible.getNode().getElementType() == JSGraphQLTokenTypes.LPAREN) {
                        return false;
                    }
                    return true;
                }
                if (children.length == 1 && children[0] instanceof LeafPsiElement) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected boolean isSuitable(PsiElement element) {
        return element instanceof JSGraphQLPsiElement;
    }

    @NotNull
    @Override
    public StructureViewTreeElement getRoot() {
        return root;
    }
}