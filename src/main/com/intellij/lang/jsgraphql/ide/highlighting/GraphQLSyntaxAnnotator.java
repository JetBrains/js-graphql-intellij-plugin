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
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class GraphQLSyntaxAnnotator implements Annotator {
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
        createTextAttributesKey("GRAPHQL_ARGUMENT", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey VARIABLE =
        createTextAttributesKey("GRAPHQL_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey TYPE_NAME =
        createTextAttributesKey("GRAPHQL_TYPE_NAME", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);
    public static final TextAttributesKey CONSTANT =
        createTextAttributesKey("GRAPHQL_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey DIRECTIVE =
        createTextAttributesKey("GRAPHQL_DIRECTIVE", DefaultLanguageHighlighterColors.METADATA);
    public static final TextAttributesKey UNUSED_FRAGMENT = createTextAttributesKey("GRAPHQL_UNUSED_FRAGMENT");

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull final AnnotationHolder holder) {
        element.accept(new GraphQLSyntaxAnnotatorVisitor(holder));
    }

    private static class GraphQLSyntaxAnnotatorVisitor extends GraphQLVisitor {
        private final AnnotationHolder myHolder;

        private GraphQLSyntaxAnnotatorVisitor(@NotNull AnnotationHolder holder) {
            myHolder = holder;
        }

        @Override
        public void visitOperationDefinition(@NotNull GraphQLOperationDefinition operationDefinition) {
            applyTextAttributes(operationDefinition.getNameIdentifier(), OPERATION_DEFINITION);
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
            applyTextAttributes(field.getNameIdentifier(), FIELD_NAME);
        }

        @Override
        public void visitFieldDefinition(@NotNull GraphQLFieldDefinition fieldDefinition) {
            applyTextAttributes(fieldDefinition.getNameIdentifier(), FIELD_NAME);
        }

        @Override
        public void visitInputValueDefinition(@NotNull GraphQLInputValueDefinition element) {
            // first reset the bold font display from keywords such as input/type being used as field name
            GraphQLIdentifier identifier = element.getNameIdentifier();
            resetKeywordAttributes(identifier);

            PsiElement parent = element.getParent();
            if (parent instanceof GraphQLArgumentsDefinition) {
                // element is an argument so color as such
                applyTextAttributes(identifier, ARGUMENT);
            } else {
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
            applyTextAttributes(variable, VARIABLE);
        }

        @Override
        public void visitTypeName(@NotNull GraphQLTypeName typeName) {
            applyTextAttributes(typeName.getNameIdentifier(), TYPE_NAME);
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
            GraphQLIdentifier identifier = directive.getNameIdentifier();
            if (identifier != null) {
                applyTextAttributes(identifier, DIRECTIVE);
                applyTextAttributes(identifier.getPrevSibling(), DIRECTIVE);
            }
        }

        @Override
        public void visitObjectField(@NotNull GraphQLObjectField objectField) {
            // first reset the bold font display from keywords such as input/type being used as object field name
            resetKeywordAttributes(objectField.getNameIdentifier());

            // then apply argument font style
            applyTextAttributes(objectField.getNameIdentifier(), ARGUMENT);
        }

        private void applyTextAttributes(@Nullable PsiElement element, @NotNull TextAttributesKey attributes) {
            if (element == null) return;

            String message = ApplicationManager.getApplication().isUnitTestMode() ? attributes.getExternalName() : null;
            Annotation annotation =
                myHolder.createAnnotation(HighlightInfoType.SYMBOL_TYPE_SEVERITY, element.getTextRange(), message);
            annotation.setTextAttributes(attributes);
        }

        private void resetKeywordAttributes(@Nullable PsiElement element) {
            if (!(element instanceof GraphQLIdentifier)) return;

            // reset only for keywords used as fields, arguments, etc
            ASTNode node = element.getNode();
            if (node == null) return;
            node = TreeUtil.findFirstLeaf(node);
            IElementType elementType = PsiUtilCore.getElementType(node);
            if (!GraphQLExtendedElementTypes.KEYWORDS.contains(elementType)) return;

            Annotation annotation =
                myHolder.createAnnotation(HighlightInfoType.SYMBOL_TYPE_SEVERITY, element.getTextRange(), null);
            annotation.setEnforcedTextAttributes(TextAttributes.ERASE_MARKER);
        }
    }
}
