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

public class GraphQLTypedOperationDefinitionImpl extends GraphQLTypedOperationDefinitionMixin implements GraphQLTypedOperationDefinition {

  public GraphQLTypedOperationDefinitionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull GraphQLVisitorBase visitor) {
    visitor.visitTypedOperationDefinition(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GraphQLVisitorBase) accept((GraphQLVisitorBase)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public GraphQLOperationType getOperationType() {
    return findNotNullChildByClass(GraphQLOperationType.class);
  }

  @Override
  @Nullable
  public GraphQLSelectionSet getSelectionSet() {
    return findChildByClass(GraphQLSelectionSet.class);
  }

  @Override
  @Nullable
  public GraphQLVariableDefinitions getVariableDefinitions() {
    return findChildByClass(GraphQLVariableDefinitions.class);
  }

  @Override
  @NotNull
  public List<GraphQLDirective> getDirectives() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, GraphQLDirective.class);
  }

  @Override
  @Nullable
  public GraphQLIdentifier getNameIdentifier() {
    return findChildByClass(GraphQLIdentifier.class);
  }

}
