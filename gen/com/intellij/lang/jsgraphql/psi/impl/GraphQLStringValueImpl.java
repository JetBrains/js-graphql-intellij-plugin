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

public class GraphQLStringValueImpl extends GraphQLValueImpl implements GraphQLStringValue {

  public GraphQLStringValueImpl(ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull GraphQLVisitor visitor) {
    visitor.visitStringValue(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GraphQLVisitor) accept((GraphQLVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public GraphQLStringLiteral getStringLiteral() {
    return findNotNullChildByClass(GraphQLStringLiteral.class);
  }

}
