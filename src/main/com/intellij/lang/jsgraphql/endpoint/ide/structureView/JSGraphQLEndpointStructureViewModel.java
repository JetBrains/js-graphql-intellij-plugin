/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.structureView;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointInputValueDefinition;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointPsiElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;

public class JSGraphQLEndpointStructureViewModel extends TextEditorBasedStructureViewModel implements StructureViewModel.ElementInfoProvider {

    private final JSGraphQLEndpointStructureViewTreeElement root;

    public JSGraphQLEndpointStructureViewModel(PsiElement root, Editor editor) {
        super(editor, root.getContainingFile());
        this.root = new JSGraphQLEndpointStructureViewTreeElement(root, root);
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        if (element instanceof JSGraphQLEndpointStructureViewTreeElement) {
            JSGraphQLEndpointStructureViewTreeElement treeElement = (JSGraphQLEndpointStructureViewTreeElement) element;
            if (treeElement.childrenBase instanceof LeafPsiElement) {
                return true;
            }
            if (treeElement.childrenBase instanceof JSGraphQLEndpointInputValueDefinition) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean isSuitable(PsiElement element) {
        return element instanceof JSGraphQLEndpointPsiElement;
    }

    @NotNull
    @Override
    public StructureViewTreeElement getRoot() {
        return root;
    }
}
