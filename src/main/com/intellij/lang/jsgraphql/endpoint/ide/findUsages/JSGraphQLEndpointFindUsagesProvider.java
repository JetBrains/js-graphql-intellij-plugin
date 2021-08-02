/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.findUsages;

import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointPropertyPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypesSets;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointInputValueDefinitionIdentifier;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointNamedTypeDefinition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * Find usages for named GraphQL Endpoint PSI elements
 */
public class JSGraphQLEndpointFindUsagesProvider implements FindUsagesProvider {

    @Nullable
    @Override
    public WordsScanner getWordsScanner() {
        return null;
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        if(!psiElement.isValid()) {
            return false;
        }
        return psiElement instanceof PsiNamedElement;
    }

    @Nullable
    @Override
    public String getHelpId(@NotNull PsiElement psiElement) {
        return "reference.dialogs.findUsages.other";
    }

    @NotNull
    @Override
    public String getType(@NotNull PsiElement element) {
        if(element instanceof JSGraphQLEndpointInputValueDefinitionIdentifier) {
            return "argument";
        }
        if(element instanceof JSGraphQLEndpointPropertyPsiElement) {
            return "field";
        }
        final JSGraphQLEndpointNamedTypeDefinition definition = PsiTreeUtil.getParentOfType(element, JSGraphQLEndpointNamedTypeDefinition.class);
        if(definition != null) {
            // if it's a definition, use the keyword, e.g. 'type', 'interface' etc.
            final ASTNode keyword = definition.getNode().findChildByType(JSGraphQLEndpointTokenTypesSets.KEYWORDS);
            if(keyword != null) {
                return keyword.getText();
            }
        }
        return "element";
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull PsiElement element) {
        if (element.getParent() instanceof PsiNamedElement) {
            return StringUtil.notNullize(((PsiNamedElement)element.getParent()).getName());
        }
        return "";
    }

    @NotNull
    @Override
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        final String name = element.getParent() instanceof PsiNamedElement ? ((PsiNamedElement)element.getParent()).getName() : null;
        return name != null ? name : element.getText();
    }
}
