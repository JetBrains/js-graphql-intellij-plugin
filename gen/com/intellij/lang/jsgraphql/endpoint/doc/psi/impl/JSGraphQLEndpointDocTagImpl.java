// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.endpoint.doc.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.lang.jsgraphql.endpoint.doc.JSGraphQLEndpointDocTokenTypes.*;
import com.intellij.lang.jsgraphql.endpoint.doc.psi.JSGraphQLEndpointDocPsiElement;
import com.intellij.lang.jsgraphql.endpoint.doc.psi.*;

public class JSGraphQLEndpointDocTagImpl extends JSGraphQLEndpointDocPsiElement implements JSGraphQLEndpointDocTag {

  public JSGraphQLEndpointDocTagImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull JSGraphQLEndpointDocVisitor visitor) {
    visitor.visitTag(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JSGraphQLEndpointDocVisitor) accept((JSGraphQLEndpointDocVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getDocName() {
    return findNotNullChildByType(DOCNAME);
  }

  @Override
  @Nullable
  public PsiElement getDocValue() {
    return findChildByType(DOCVALUE);
  }

}
