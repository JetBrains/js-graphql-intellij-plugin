/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.documentation;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.FakePsiElement;
import kotlin.text.StringsKt;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

final class GraphQLDocumentationPsiElement extends FakePsiElement {
  private final PsiElement context;
  private final String type;
  private JSGraphQLDocItemPresentation itemPresentation;

  GraphQLDocumentationPsiElement(PsiElement context, String link) {
    this.context = context;
    this.type = StringsKt.substringAfterLast(link, "/", link);
  }

  public String getType() {
    return type;
  }

  @Override
  public ItemPresentation getPresentation() {
    if (itemPresentation == null) {
      itemPresentation = new JSGraphQLDocItemPresentation();
    }
    return itemPresentation;
  }

  @Override
  public PsiElement getParent() {
    return context;
  }

  private class JSGraphQLDocItemPresentation implements ItemPresentation {
    @Nullable
    @Override
    public String getPresentableText() {
      return type;
    }

    @Nullable
    @Override
    public String getLocationString() {
      return null;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean unused) {
      return null;
    }
  }
}
