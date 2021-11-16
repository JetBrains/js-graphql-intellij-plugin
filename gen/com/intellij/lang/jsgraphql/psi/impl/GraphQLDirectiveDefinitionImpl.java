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

public class GraphQLDirectiveDefinitionImpl extends GraphQLTypeSystemDefinitionImpl implements GraphQLDirectiveDefinition {

  public GraphQLDirectiveDefinitionImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull GraphQLVisitorBase visitor) {
    visitor.visitDirectiveDefinition(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof GraphQLVisitorBase) accept((GraphQLVisitorBase)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public GraphQLArgumentsDefinition getArgumentsDefinition() {
    return findChildByClass(GraphQLArgumentsDefinition.class);
  }

  @Override
  @Nullable
  public GraphQLDescription getDescription() {
    return findChildByClass(GraphQLDescription.class);
  }

  @Override
  @Nullable
  public GraphQLDirectiveLocations getDirectiveLocations() {
    return findChildByClass(GraphQLDirectiveLocations.class);
  }

  @Override
  @Nullable
  public GraphQLIdentifier getNameIdentifier() {
    return findChildByClass(GraphQLIdentifier.class);
  }

  @Override
  @Nullable
  public PsiElement getRepeatable() {
    return findChildByType(REPEATABLE_KEYWORD);
  }

}
