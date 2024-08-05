/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.resolve;

import com.intellij.lang.jsgraphql.psi.impl.GraphQLReferenceMixin;
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
public class GraphQLCachingReference extends PsiReferenceBase<GraphQLReferenceMixin> {

  private final Function<GraphQLReferenceMixin, PsiElement> innerResolver;

  public GraphQLCachingReference(@NotNull GraphQLReferenceMixin element, Function<GraphQLReferenceMixin, PsiElement> innerResolver) {
    super(element);
    this.innerResolver = innerResolver;
  }

  @Override
  public @Nullable PsiElement resolve() {
    return ResolveCache.getInstance(getElement().getProject())
      .resolveWithCaching(this, GraphQLCachingReference.MyResolver.INSTANCE, false, false);
  }

  private @Nullable PsiElement resolveInner() {
    return innerResolver.apply(myElement);
  }

  @Override
  public @NotNull Object[] getVariants() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  private static class MyResolver implements ResolveCache.Resolver {
    private static final GraphQLCachingReference.MyResolver INSTANCE = new GraphQLCachingReference.MyResolver();

    public @Nullable PsiElement resolve(@NotNull PsiReference ref, boolean incompleteCode) {
      return ((GraphQLCachingReference)ref).resolveInner();
    }
  }
}
