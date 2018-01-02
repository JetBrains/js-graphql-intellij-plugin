// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.endpoint.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes.*;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointPsiElement;
import com.intellij.lang.jsgraphql.endpoint.psi.*;

public class JSGraphQLEndpointAnnotationArgumentObjectFieldImpl extends JSGraphQLEndpointPsiElement implements JSGraphQLEndpointAnnotationArgumentObjectField {

  public JSGraphQLEndpointAnnotationArgumentObjectFieldImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull JSGraphQLEndpointVisitor visitor) {
    visitor.visitAnnotationArgumentObjectField(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JSGraphQLEndpointVisitor) accept((JSGraphQLEndpointVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public JSGraphQLEndpointAnnotationArgumentValue getAnnotationArgumentValue() {
    return findNotNullChildByClass(JSGraphQLEndpointAnnotationArgumentValue.class);
  }

  @Override
  @NotNull
  public PsiElement getNameIdentifier() {
    return findNotNullChildByType(IDENTIFIER);
  }

}
