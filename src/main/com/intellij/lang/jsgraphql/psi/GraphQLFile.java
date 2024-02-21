/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.jsgraphql.GraphQLConstants;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.lang.jsgraphql.schema.GraphQLPsiDocumentBuilder;
import com.intellij.lang.jsgraphql.types.language.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;

public class GraphQLFile extends PsiFileBase implements GraphQLElement {
  public GraphQLFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, GraphQLLanguage.INSTANCE);
  }

  @Override
  public @NotNull FileType getFileType() {
    return GraphQLFileType.INSTANCE;
  }

  @Override
  public String toString() {
    return GraphQLConstants.GraphQL;
  }

  @Override
  public Icon getIcon(int flags) {
    return super.getIcon(flags);
  }

  public void accept(@NotNull GraphQLVisitor visitor) {
    visitor.visitGraphQLFile(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GraphQLVisitor) {
      accept((GraphQLVisitor)visitor);
    }
    else {
      super.accept(visitor);
    }
  }

  public @NotNull Collection<GraphQLDefinition> getDefinitions() {
    return CachedValuesManager.getCachedValue(this, () -> CachedValueProvider.Result.create(
      PsiTreeUtil.getChildrenOfTypeAsList(this, GraphQLDefinition.class), this));
  }

  public @NotNull Collection<GraphQLTypeSystemDefinition> getTypeDefinitions() {
    return CachedValuesManager.getCachedValue(this, () -> CachedValueProvider.Result.create(
      PsiTreeUtil.getChildrenOfTypeAsList(this, GraphQLTypeSystemDefinition.class), this));
  }

  public @NotNull MultiMap<String, PsiNamedElement> getNamedElements() {
    return CachedValuesManager.getCachedValue(this, () -> {
      MultiMap<String, PsiNamedElement> map = MultiMap.create();
      for (PsiNamedElement namedElement : PsiTreeUtil.collectElementsOfType(this, PsiNamedElement.class)) {
        String name = namedElement.getName();
        if (name != null) {
          map.putValue(name, namedElement);
        }
      }
      return CachedValueProvider.Result.create(map, this);
    });
  }

  public @NotNull Document getDocument() {
    return CachedValuesManager.getCachedValue(this, () -> {
      Document document = new GraphQLPsiDocumentBuilder(this).createDocument();
      return CachedValueProvider.Result.createSingleDependency(document, this);
    });
  }
}
