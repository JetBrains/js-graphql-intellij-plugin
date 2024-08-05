/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLResolveUtil;
import com.intellij.lang.jsgraphql.psi.GraphQLDirectiveLocation;
import com.intellij.lang.jsgraphql.psi.GraphQLEnumValue;
import com.intellij.lang.jsgraphql.psi.GraphQLIdentifier;
import com.intellij.lang.jsgraphql.psi.GraphQLRecursiveVisitor;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryTypes;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class GraphQLDirectiveLocationMixin extends GraphQLElementImpl implements GraphQLDirectiveLocation {

  public GraphQLDirectiveLocationMixin(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public PsiReference getReference() {
    return CachedValuesManager.getCachedValue(this, () -> {
      final Ref<PsiReference> reference = new Ref<>();
      final GraphQLDirectiveLocationMixin location = this;
      final String locationName = location.getText();

      // TODO: [vepanimas] move to a reference resolver: getReference should only create a reference, not resolve it.
      GraphQLResolveUtil.processFilesInLibrary(GraphQLLibraryTypes.SPECIFICATION, this, file -> {
        file.accept(new GraphQLRecursiveVisitor() {
          @Override
          public void visitEnumValue(@NotNull GraphQLEnumValue element) {
            if (Objects.equals(element.getName(), locationName)) {
              final GraphQLIdentifier referencedEnumIdentifier = element.getNameIdentifier();
              reference.set(new PsiReferenceBase<>(location, new TextRange(0, location.getTextLength())) {
                @Override
                public PsiElement resolve() {
                  return referencedEnumIdentifier;
                }

                @Override
                public @NotNull Object @NotNull [] getVariants() {
                  return PsiReference.EMPTY_ARRAY;
                }
              });
              stopWalking();
              return;
            }
            super.visitEnumValue(element);
          }
        });
        return reference.isNull();
      });

      return CachedValueProvider.Result.create(reference.get(), PsiModificationTracker.MODIFICATION_COUNT);
    });
  }
}
