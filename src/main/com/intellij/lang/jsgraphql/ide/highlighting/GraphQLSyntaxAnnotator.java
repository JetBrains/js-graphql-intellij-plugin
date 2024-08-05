/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.highlighting;

import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public final class GraphQLSyntaxAnnotator implements Annotator {
  public static final TextAttributesKey OPERATION_DEFINITION =
    createTextAttributesKey("GRAPHQL_OPERATION_DEFINITION", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
  public static final TextAttributesKey FRAGMENT_DEFINITION =
    createTextAttributesKey("GRAPHQL_FRAGMENT_DEFINITION", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
  public static final TextAttributesKey FRAGMENT_SPREAD =
    createTextAttributesKey("GRAPHQL_FRAGMENT_SPREAD", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
  public static final TextAttributesKey FIELD_NAME =
    createTextAttributesKey("GRAPHQL_FIELD_NAME", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
  public static final TextAttributesKey FIELD_ALIAS =
    createTextAttributesKey("GRAPHQL_FIELD_ALIAS", DefaultLanguageHighlighterColors.LABEL);
  public static final TextAttributesKey ARGUMENT =
    createTextAttributesKey("GRAPHQL_ARGUMENT", DefaultLanguageHighlighterColors.PARAMETER);
  public static final TextAttributesKey PARAMETER =
    createTextAttributesKey("GRAPHQL_PARAMETER", ARGUMENT);
  public static final TextAttributesKey OBJECT_FIELD =
    createTextAttributesKey("GRAPHQL_OBJECT_FIELD", ARGUMENT);
  public static final TextAttributesKey VARIABLE_DEFINITION =
    createTextAttributesKey("GRAPHQL_VARIABLE_DEFINITION", PARAMETER);
  public static final TextAttributesKey VARIABLE =
    createTextAttributesKey("GRAPHQL_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
  public static final TextAttributesKey TYPE_NAME =
    createTextAttributesKey("GRAPHQL_TYPE_NAME", DefaultLanguageHighlighterColors.CLASS_NAME);
  public static final TextAttributesKey CONSTANT =
    createTextAttributesKey("GRAPHQL_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT);
  public static final TextAttributesKey DIRECTIVE =
    createTextAttributesKey("GRAPHQL_DIRECTIVE", DefaultLanguageHighlighterColors.METADATA);
  public static final TextAttributesKey UNUSED_FRAGMENT = createTextAttributesKey("GRAPHQL_UNUSED_FRAGMENT");

  @Override
  public void annotate(@NotNull PsiElement element, final @NotNull AnnotationHolder holder) {
    element.accept(new GraphQLSyntaxAnnotatorVisitor(holder));
  }

  private static class GraphQLSyntaxAnnotatorVisitor extends GraphQLVisitor {
    private final AnnotationHolder myHolder;

    private GraphQLSyntaxAnnotatorVisitor(@NotNull AnnotationHolder holder) {
      myHolder = holder;
    }

    @Override
    public void visitTypedOperationDefinition(@NotNull GraphQLTypedOperationDefinition operationDefinition) {
      applyTextAttributes(operationDefinition.getNameIdentifier(), OPERATION_DEFINITION);
    }

    @Override
    public void visitOperationType(@NotNull GraphQLOperationType operationType) {
      PsiElement parent = operationType.getParent();
      if (parent instanceof GraphQLOperationTypeDefinition) {
        resetKeywordAttributes(operationType);
        applyTextAttributes(operationType, FIELD_NAME);
      }
    }

    public void visitFragmentDefinition(@NotNull GraphQLFragmentDefinition fragmentDefinition) {
      applyTextAttributes(fragmentDefinition.getNameIdentifier(), FRAGMENT_DEFINITION);
    }

    @Override
    public void visitFragmentSpread(@NotNull GraphQLFragmentSpread fragmentSpread) {
      applyTextAttributes(fragmentSpread.getNameIdentifier(), FRAGMENT_SPREAD);
    }

    @Override
    public void visitTypeNameDefinition(@NotNull GraphQLTypeNameDefinition definition) {
      applyTextAttributes(definition.getNameIdentifier(), TYPE_NAME);
    }

    @Override
    public void visitField(@NotNull GraphQLField field) {
      GraphQLIdentifier nameIdentifier = field.getNameIdentifier();
      resetKeywordAttributes(nameIdentifier);
      applyTextAttributes(nameIdentifier, FIELD_NAME);
    }

    @Override
    public void visitFieldDefinition(@NotNull GraphQLFieldDefinition fieldDefinition) {
      GraphQLIdentifier nameIdentifier = fieldDefinition.getNameIdentifier();
      resetKeywordAttributes(nameIdentifier);
      applyTextAttributes(nameIdentifier, FIELD_NAME);
    }

    @Override
    public void visitInputValueDefinition(@NotNull GraphQLInputValueDefinition element) {
      // first reset the bold font display from keywords such as input/type being used as field name
      GraphQLIdentifier identifier = element.getNameIdentifier();
      resetKeywordAttributes(identifier);

      PsiElement parent = element.getParent();
      if (parent instanceof GraphQLArgumentsDefinition) {
        // element is a parameter declaration so color as such
        applyTextAttributes(identifier, PARAMETER);
      }
      else {
        // otherwise consider the "fields" in an in put
        applyTextAttributes(identifier, FIELD_NAME);
      }
    }

    @Override
    public void visitArgument(@NotNull GraphQLArgument argument) {
      GraphQLIdentifier identifier = argument.getNameIdentifier();

      // first reset the bold font display from keywords such as input/type being used as argument name
      resetKeywordAttributes(identifier);

      // then apply argument font style
      applyTextAttributes(identifier, ARGUMENT);
    }

    @Override
    public void visitVariable(@NotNull GraphQLVariable variable) {
      PsiElement parent = variable.getParent();
      if (parent instanceof GraphQLVariableDefinition) {
        applyTextAttributes(variable, VARIABLE_DEFINITION);
      }
      else {
        applyTextAttributes(variable, VARIABLE);
      }
    }

    @Override
    public void visitTypeName(@NotNull GraphQLTypeName typeName) {
      applyTextAttributes(typeName.getNameIdentifier(), TYPE_NAME);
    }

    @Override
    public void visitDirectiveLocation(@NotNull GraphQLDirectiveLocation location) {
      applyTextAttributes(location, TYPE_NAME);
    }

    @Override
    public void visitBooleanValue(@NotNull GraphQLBooleanValue value) {
      applyTextAttributes(value, DefaultLanguageHighlighterColors.KEYWORD);
    }

    @Override
    public void visitNullValue(@NotNull GraphQLNullValue value) {
      applyTextAttributes(value, DefaultLanguageHighlighterColors.KEYWORD);
    }

    @Override
    public void visitEnumValue(@NotNull GraphQLEnumValue value) {
      applyTextAttributes(value, CONSTANT);
    }

    @Override
    public void visitDirective(@NotNull GraphQLDirective directive) {
      highlightDirectiveName(directive, directive.getNameIdentifier());
    }

    @Override
    public void visitDirectiveDefinition(@NotNull GraphQLDirectiveDefinition directiveDefinition) {
      highlightDirectiveName(directiveDefinition, directiveDefinition.getNameIdentifier());
    }

    private void highlightDirectiveName(@NotNull GraphQLElement element, @Nullable GraphQLIdentifier identifier) {
      if (identifier == null) {
        return;
      }
      TextRange textRange = identifier.getTextRange();
      PsiElement prevSibling = identifier.getPrevSibling();
      if (PsiUtilCore.getElementType(prevSibling) == GraphQLElementTypes.AT) {
        TextRange prevTextRange = prevSibling.getTextRange();
        textRange = textRange.union(prevTextRange);
      }
      applyTextAttributes(element, DIRECTIVE, textRange);
    }

    @Override
    public void visitObjectField(@NotNull GraphQLObjectField objectField) {
      GraphQLIdentifier nameIdentifier = objectField.getNameIdentifier();

      // first reset the bold font display from keywords such as input/type being used as object field name
      resetKeywordAttributes(nameIdentifier);
      // then apply argument font style
      applyTextAttributes(nameIdentifier, OBJECT_FIELD);
    }

    private void applyTextAttributes(@Nullable PsiElement element, @NotNull TextAttributesKey attributes) {
      if (element == null) return;
      applyTextAttributes(element, attributes, element.getTextRange());
    }

    private void applyTextAttributes(@Nullable PsiElement element, @NotNull TextAttributesKey attributes, @NotNull TextRange range) {
      if (element == null) return;

      AnnotationBuilder builder = ApplicationManager.getApplication().isUnitTestMode()
                                  ? myHolder.newAnnotation(HighlightInfoType.SYMBOL_TYPE_SEVERITY, attributes.getExternalName())
                                  : myHolder.newSilentAnnotation(HighlightInfoType.SYMBOL_TYPE_SEVERITY);

      builder.range(range).textAttributes(attributes).create();
    }

    private void resetKeywordAttributes(@Nullable PsiElement element) {
      if (element == null) return;

      // reset only for keywords used as fields, arguments, etc
      ASTNode node = element.getNode();
      if (node == null) return;
      node = TreeUtil.findFirstLeaf(node);
      IElementType elementType = PsiUtilCore.getElementType(node);
      if (!GraphQLExtendedElementTypes.KEYWORDS.contains(elementType)) return;

      myHolder
        .newSilentAnnotation(HighlightInfoType.SYMBOL_TYPE_SEVERITY)
        .range(element.getTextRange())
        .enforcedTextAttributes(TextAttributes.ERASE_MARKER)
        .create();
    }
  }
}
