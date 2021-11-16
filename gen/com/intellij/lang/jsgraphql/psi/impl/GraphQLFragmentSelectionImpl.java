// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.*;
import com.intellij.lang.jsgraphql.psi.*;

public class GraphQLFragmentSelectionImpl extends GraphQLElementImpl implements GraphQLFragmentSelection {

  public GraphQLFragmentSelectionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull GraphQLVisitorBase visitor) {
    visitor.visitFragmentSelection(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GraphQLVisitorBase) accept((GraphQLVisitorBase)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public GraphQLFragmentSpread getFragmentSpread() {
    return findChildByClass(GraphQLFragmentSpread.class);
  }

  @Override
  @Nullable
  public GraphQLInlineFragment getInlineFragment() {
    return findChildByClass(GraphQLInlineFragment.class);
  }

}
