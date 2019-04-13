/*
 *  Copyright (c) 2019-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.injection;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public interface GraphQLCommentBasedInjectionHelper {

    /**
     * Gets whether the specified host element has a GraphQL injection based a language=GraphQL comment
     * @param host the host to check
     * @param envRef optional GraphQL environment ref to set if the host is a match
     * @return <code>true</code> if the host has an active GraphQL injection, <code>false</code> otherwise
     */
    boolean isGraphQLInjectedUsingComment(PsiElement host, @Nullable Ref<String> envRef);
}
