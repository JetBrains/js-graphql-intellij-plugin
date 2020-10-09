/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
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

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull final AnnotationHolder holder) {
        element.accept(new GraphQLVisitor() {
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
            public void visitIdentifier(@NotNull GraphQLIdentifier identifier) {
                // the annotator visitor skips fields, directives etc if they have errors (empty arguments list for instance)
                // so we do a pass here as well to highlight
                final PsiElement parent = identifier.getParent();
                if (parent instanceof GraphQLField) {
                    this.visitField((GraphQLField) parent);
                } else if (parent instanceof GraphQLFieldDefinition) {
                    this.visitFieldDefinition((GraphQLFieldDefinition) parent);
                } else if (parent instanceof GraphQLDirective) {
                    this.visitDirective((GraphQLDirective) parent);
                }
            }

            @Override
            public void visitField(@NotNull GraphQLField field) {
                if (field.isDeprecated()) {
                    applyTextAttributes(field.getNameIdentifier(), CodeInsightColors.DEPRECATED_ATTRIBUTES);
                }
                applyTextAttributes(field.getNameIdentifier(), FIELD_NAME);
                super.visitField(field);
            }

            @Override
            public void visitFieldDefinition(@NotNull GraphQLFieldDefinition fieldDefinition) {
                applyTextAttributes(fieldDefinition.getNameIdentifier(), FIELD_NAME);
            }

            @Override
            public void visitInputValueDefinition(@NotNull GraphQLInputValueDefinition element) {

                // first reset the bold font display from keywords such as input/type being used as field name
                Annotation annotation = holder.createInfoAnnotation(element.getNameIdentifier(), null);
                annotation.setEnforcedTextAttributes(TextAttributes.ERASE_MARKER);

                final GraphQLArgumentsDefinition arguments = PsiTreeUtil.getParentOfType(element, GraphQLArgumentsDefinition.class);
                if (arguments != null) {
                    // element is an argument so color as such
                    applyTextAttributes(element.getNameIdentifier(), ARGUMENT);
                } else {
                    // otherwise consider the "fields" in an in put
                    applyTextAttributes(element.getNameIdentifier(), FIELD_NAME);
                }
            }

            @Override
            public void visitArgument(@NotNull GraphQLArgument argument) {

                // first reset the bold font display from keywords such as input/type being used as argument name
                Annotation annotation = holder.createInfoAnnotation(argument.getNameIdentifier(), null);
                annotation.setEnforcedTextAttributes(TextAttributes.ERASE_MARKER);

                // then apply argument font style
                applyTextAttributes(argument.getNameIdentifier(), ARGUMENT);
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
                Annotation annotation = holder.createInfoAnnotation(objectField.getNameIdentifier(), null);
                annotation.setEnforcedTextAttributes(TextAttributes.ERASE_MARKER);

                // then apply argument font style
                applyTextAttributes(objectField.getNameIdentifier(), ARGUMENT);
            }

            private void applyTextAttributes(@Nullable PsiElement element, @NotNull TextAttributesKey attributes) {
                if (element == null) return;
                Annotation annotation = holder.createInfoAnnotation(element, null);
                annotation.setTextAttributes(attributes);
            }
        });
    }
}
