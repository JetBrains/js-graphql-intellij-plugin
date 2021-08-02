/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.findUsages;

import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandlerFactory;
import com.intellij.psi.PsiElement;

/**
 * Find usages handler factory for the GraphQL Endpoint language
 */
public class JSGraphQLEndpointFindUsagesHandlerFactory extends FindUsagesHandlerFactory {

    @Override
    public boolean canFindUsages(@NotNull PsiElement element) {
        if(!element.isValid()) {
            return false;
        }
        return element instanceof JSGraphQLEndpointPsiElement;
    }

    @Nullable
    @Override
    public FindUsagesHandler createFindUsagesHandler(@NotNull PsiElement element, boolean forHighlightUsages) {

        if (canFindUsages(element)) {
            return new FindUsagesHandler(element) {};
        }

        return null;

    }
}
