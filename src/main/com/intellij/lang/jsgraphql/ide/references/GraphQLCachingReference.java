/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.references;

import com.intellij.lang.jsgraphql.psi.impl.GraphQLReferencePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;


/**
 * Adds a layer of GraphQL Reference Caching based on ResolveCache
 */
public class GraphQLCachingReference extends PsiReferenceBase<GraphQLReferencePsiElement> {

    private final Function<GraphQLReferencePsiElement, PsiElement> innerResolver;

    public GraphQLCachingReference(@NotNull GraphQLReferencePsiElement element, Function<GraphQLReferencePsiElement, PsiElement> innerResolver) {
        super(element);
        this.innerResolver = innerResolver;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return ResolveCache.getInstance(getElement().getProject()).resolveWithCaching(this, GraphQLCachingReference.MyResolver.INSTANCE, false, false);
    }

    @Nullable
    private PsiElement resolveInner() {
        return innerResolver.apply(myElement);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    private static class MyResolver implements ResolveCache.Resolver {
        private static final GraphQLCachingReference.MyResolver INSTANCE = new GraphQLCachingReference.MyResolver();

        @Nullable
        public PsiElement resolve(@NotNull PsiReference ref, boolean incompleteCode) {
            return ((GraphQLCachingReference) ref).resolveInner();
        }
    }

}
