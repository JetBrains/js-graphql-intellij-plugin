/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.references;

import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.lang.jsgraphql.psi.GraphQLDirectiveDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLElement;
import com.intellij.lang.jsgraphql.psi.GraphQLEnumValue;
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLIdentifier;
import com.intellij.lang.jsgraphql.psi.GraphQLInputValueDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLNamedElement;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeNameDefinition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Find usages for named GraphQL PSI elements
 * @see GraphQLNamedElement
 */
public class GraphQLFindUsagesProvider implements FindUsagesProvider {

    @Nullable
    @Override
    public WordsScanner getWordsScanner() {
        return null;
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return psiElement.isValid() && psiElement instanceof GraphQLElement && psiElement instanceof PsiNamedElement;
    }

    @Nullable
    @Override
    public String getHelpId(@NotNull PsiElement psiElement) {
        return "reference.dialogs.findUsages.other";
    }

    @NotNull
    @Override
    public String getType(@NotNull PsiElement element) {
        if(element instanceof GraphQLIdentifier) {
            final PsiElement parent = element.getParent();
            if(parent instanceof GraphQLTypeNameDefinition) {
                return "type";
            }
            if(parent instanceof GraphQLFieldDefinition) {
                return "field";
            }
            if(parent instanceof GraphQLInputValueDefinition) {
                return "argument";
            }
            if(parent instanceof GraphQLFragmentDefinition) {
                return "fragment";
            }
            if(parent instanceof GraphQLEnumValue) {
                return "enum value";
            }
            if(parent instanceof GraphQLDirectiveDefinition) {
                return "directive";
            }
        }
        return "unknown";
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull PsiElement element) {
        if (element.getParent() instanceof PsiNamedElement) {
            return StringUtil.notNullize(((PsiNamedElement) element.getParent()).getName());
        }
        return "";
    }

    @NotNull
    @Override
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        final String name = element.getParent() instanceof PsiNamedElement ? ((PsiNamedElement) element.getParent()).getName() : null;
        return name != null ? name : element.getText();
    }
}
