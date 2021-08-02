/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.structureView;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.lang.jsgraphql.psi.GraphQLElement;
import com.intellij.lang.jsgraphql.psi.GraphQLElementTypes;
import com.intellij.lang.jsgraphql.psi.GraphQLField;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class GraphQLStructureViewModel extends TextEditorBasedStructureViewModel implements StructureViewModel.ElementInfoProvider {

    private final GraphQLStructureViewTreeElement root;

    public GraphQLStructureViewModel(PsiElement root, Editor editor) {
        super(editor, root.getContainingFile());
        this.root = new GraphQLStructureViewTreeElement(root, root);
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        if (element instanceof GraphQLStructureViewTreeElement) {
            GraphQLStructureViewTreeElement treeElement = (GraphQLStructureViewTreeElement) element;
            if (treeElement.childrenBase instanceof LeafPsiElement) {
                return true;
            }
            if (treeElement.childrenBase instanceof GraphQLField) {
                final PsiElement[] children = treeElement.childrenBase.getChildren();
                if (children.length == 0) {
                    // field with no sub selections, but we have to check if there's attributes
                    final PsiElement nextVisible = PsiTreeUtil.nextVisibleLeaf(treeElement.childrenBase);
                    if (nextVisible != null && nextVisible.getNode().getElementType() == GraphQLElementTypes.PAREN_L) {
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
        return element instanceof GraphQLElement;
    }

    @NotNull
    @Override
    public StructureViewTreeElement getRoot() {
        return root;
    }
}
