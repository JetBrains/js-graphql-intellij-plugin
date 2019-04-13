/*
 *  Copyright (c) 2019-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.injection;

import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import org.intellij.plugins.intelliLang.inject.InjectorUtils;
import org.jetbrains.annotations.Nullable;

/**
 * Detects manual GraphQL injections using IntelliLang <code>language=GraphQL</code> injection comments.
 */
public class GraphQLCommentBasedInjectionHelperImpl implements GraphQLCommentBasedInjectionHelper {

    @Override
    public boolean isGraphQLInjectedUsingComment(PsiElement host, @Nullable Ref<String> envRef) {
        final InjectorUtils.CommentInjectionData injectionData = InjectorUtils.findCommentInjectionData(host, true, null);
        if (injectionData != null) {
            if (GraphQLLanguage.INSTANCE.getID().equals(injectionData.getInjectedLanguageId())) {
                if (envRef != null) {
                    envRef.set("graphql");
                }
                return true;
            }
        }
        return false;
    }
}
