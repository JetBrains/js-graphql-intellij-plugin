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

public class JSGraphQLEndpointImportDeclarationImpl extends JSGraphQLEndpointPsiElement implements JSGraphQLEndpointImportDeclaration {

  public JSGraphQLEndpointImportDeclarationImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull JSGraphQLEndpointVisitor visitor) {
    visitor.visitImportDeclaration(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JSGraphQLEndpointVisitor) accept((JSGraphQLEndpointVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public JSGraphQLEndpointImportFileReference getImportFileReference() {
    return findChildByClass(JSGraphQLEndpointImportFileReference.class);
  }

}
