// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.endpoint.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiLiteralValue;

public class JSGraphQLEndpointVisitor extends PsiElementVisitor {

  public void visitAnnotation(@NotNull JSGraphQLEndpointAnnotation o) {
    visitPsiElement(o);
  }

  public void visitAnnotationArgumentListValue(@NotNull JSGraphQLEndpointAnnotationArgumentListValue o) {
    visitAnnotationArgumentValue(o);
  }

  public void visitAnnotationArgumentObjectField(@NotNull JSGraphQLEndpointAnnotationArgumentObjectField o) {
    visitPsiElement(o);
  }

  public void visitAnnotationArgumentObjectValue(@NotNull JSGraphQLEndpointAnnotationArgumentObjectValue o) {
    visitAnnotationArgumentValue(o);
  }

  public void visitAnnotationArgumentValue(@NotNull JSGraphQLEndpointAnnotationArgumentValue o) {
    visitPsiElement(o);
  }

  public void visitAnnotationArguments(@NotNull JSGraphQLEndpointAnnotationArguments o) {
    visitPsiElement(o);
  }

  public void visitAnnotationDefinition(@NotNull JSGraphQLEndpointAnnotationDefinition o) {
    visitNamedTypeDefinition(o);
  }

  public void visitArgumentsDefinition(@NotNull JSGraphQLEndpointArgumentsDefinition o) {
    visitPsiElement(o);
  }

  public void visitCompositeType(@NotNull JSGraphQLEndpointCompositeType o) {
    visitPsiElement(o);
  }

  public void visitEnumTypeDefinition(@NotNull JSGraphQLEndpointEnumTypeDefinition o) {
    visitNamedTypeDefinition(o);
  }

  public void visitEnumValueDefinition(@NotNull JSGraphQLEndpointEnumValueDefinition o) {
    visitPsiElement(o);
  }

  public void visitEnumValueDefinitionSet(@NotNull JSGraphQLEndpointEnumValueDefinitionSet o) {
    visitPsiElement(o);
  }

  public void visitFieldDefinition(@NotNull JSGraphQLEndpointFieldDefinition o) {
    visitPsiElement(o);
  }

  public void visitFieldDefinitionSet(@NotNull JSGraphQLEndpointFieldDefinitionSet o) {
    visitPsiElement(o);
  }

  public void visitImplementsInterfaces(@NotNull JSGraphQLEndpointImplementsInterfaces o) {
    visitPsiElement(o);
  }

  public void visitImportDeclaration(@NotNull JSGraphQLEndpointImportDeclaration o) {
    visitPsiElement(o);
  }

  public void visitImportFileReference(@NotNull JSGraphQLEndpointImportFileReference o) {
    visitPsiNameIdentifierOwner(o);
  }

  public void visitInputObjectTypeDefinition(@NotNull JSGraphQLEndpointInputObjectTypeDefinition o) {
    visitNamedTypeDefinition(o);
  }

  public void visitInputValueDefinition(@NotNull JSGraphQLEndpointInputValueDefinition o) {
    visitPsiElement(o);
  }

  public void visitInputValueDefinitionIdentifier(@NotNull JSGraphQLEndpointInputValueDefinitionIdentifier o) {
    visitPsiNameIdentifierOwner(o);
  }

  public void visitInputValueDefinitions(@NotNull JSGraphQLEndpointInputValueDefinitions o) {
    visitPsiElement(o);
  }

  public void visitInterfaceTypeDefinition(@NotNull JSGraphQLEndpointInterfaceTypeDefinition o) {
    visitNamedTypeDefinition(o);
  }

  public void visitListType(@NotNull JSGraphQLEndpointListType o) {
    visitPsiElement(o);
  }

  public void visitNamedAnnotationArgument(@NotNull JSGraphQLEndpointNamedAnnotationArgument o) {
    visitPsiElement(o);
  }

  public void visitNamedAnnotationArguments(@NotNull JSGraphQLEndpointNamedAnnotationArguments o) {
    visitPsiElement(o);
  }

  public void visitNamedType(@NotNull JSGraphQLEndpointNamedType o) {
    visitPsiNameIdentifierOwner(o);
  }

  public void visitNamedTypeDef(@NotNull JSGraphQLEndpointNamedTypeDef o) {
    visitPsiNameIdentifierOwner(o);
  }

  public void visitObjectTypeDefinition(@NotNull JSGraphQLEndpointObjectTypeDefinition o) {
    visitNamedTypeDefinition(o);
  }

  public void visitOperationTypeDefinition(@NotNull JSGraphQLEndpointOperationTypeDefinition o) {
    visitPsiElement(o);
  }

  public void visitOperationTypeDefinitionSet(@NotNull JSGraphQLEndpointOperationTypeDefinitionSet o) {
    visitPsiElement(o);
  }

  public void visitProperty(@NotNull JSGraphQLEndpointProperty o) {
    visitPsiNameIdentifierOwner(o);
  }

  public void visitQuotedString(@NotNull JSGraphQLEndpointQuotedString o) {
    visitPsiElement(o);
  }

  public void visitScalarTypeDefinition(@NotNull JSGraphQLEndpointScalarTypeDefinition o) {
    visitNamedTypeDefinition(o);
  }

  public void visitSchemaDefinition(@NotNull JSGraphQLEndpointSchemaDefinition o) {
    visitPsiElement(o);
  }

  public void visitString(@NotNull JSGraphQLEndpointString o) {
    visitPsiLiteralValue(o);
  }

  public void visitUnionMember(@NotNull JSGraphQLEndpointUnionMember o) {
    visitPsiElement(o);
  }

  public void visitUnionMemberSet(@NotNull JSGraphQLEndpointUnionMemberSet o) {
    visitPsiElement(o);
  }

  public void visitUnionTypeDefinition(@NotNull JSGraphQLEndpointUnionTypeDefinition o) {
    visitNamedTypeDefinition(o);
  }

  public void visitNamedTypeDefinition(@NotNull JSGraphQLEndpointNamedTypeDefinition o) {
    visitPsiElement(o);
  }

  public void visitPsiLiteralValue(@NotNull PsiLiteralValue o) {
    visitElement(o);
  }

  public void visitPsiNameIdentifierOwner(@NotNull PsiNameIdentifierOwner o) {
    visitElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
