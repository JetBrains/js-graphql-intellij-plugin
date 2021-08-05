/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.documentation;

import com.intellij.lang.documentation.DocumentationProviderEx;
import com.intellij.lang.jsgraphql.GraphQLConstants;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointDocumentationAware;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointFile;
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLInputValueDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLType;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.lang.jsgraphql.types.schema.GraphQLArgument;
import com.intellij.lang.jsgraphql.types.schema.GraphQLDirective;
import com.intellij.lang.jsgraphql.types.schema.GraphQLEnumValueDefinition;
import com.intellij.lang.jsgraphql.types.schema.*;
import org.apache.commons.lang.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static com.intellij.lang.documentation.DocumentationMarkup.*;

public class GraphQLDocumentationProvider extends DocumentationProviderEx {

    private final static String GRAPHQL_DOC_PREFIX = GraphQLConstants.GraphQL;

    @Nullable
    @Override
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        if (isDocumentationSupported(element)) {
            return createQuickNavigateDocumentation(element, false);
        }
        return null;
    }

    @Override
    public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        return createQuickNavigateDocumentation(element, true);
    }

    @Override
    public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
        if (link.startsWith(GRAPHQL_DOC_PREFIX)) {
            return new GraphQLDocumentationPsiElement(context, link);
        }
        return super.getDocumentationElementForLink(psiManager, link, context);
    }

    private boolean isDocumentationSupported(PsiElement element) {
        return element.getContainingFile() instanceof GraphQLFile || element.getContainingFile() instanceof JSGraphQLEndpointFile;
    }

    @Nullable
    private String createQuickNavigateDocumentation(PsiElement element, boolean fullDocumentation) {
        if (!isDocumentationSupported(element)) {
            return null;
        }

        final GraphQLSchemaProvider typeRegistryService = GraphQLSchemaProvider.getInstance(element.getProject());
        final GraphQLSchema schema = typeRegistryService.getSchemaInfo(element).getSchema();

        if (element instanceof GraphQLNamedElement) {

            final PsiElement parent = element.getParent();

            if (parent instanceof GraphQLTypeNameDefinition) {
                return getTypeDocumentation(element, schema, (GraphQLTypeNameDefinition) parent);
            }

            if (parent instanceof GraphQLFieldDefinition) {
                return getFieldDocumentation(element, schema, (GraphQLFieldDefinition) parent);
            }

            if (parent instanceof GraphQLInputValueDefinition) {
                return getArgumentDocumentation(schema, (GraphQLInputValueDefinition) parent);
            }

            if (parent instanceof GraphQLEnumValue) {
                return getEnumValueDocumentation(schema, (GraphQLEnumValue) parent);
            }

            if (parent instanceof GraphQLDirectiveDefinition) {
                return getDirectiveDocumentation(schema, (GraphQLDirectiveDefinition) parent);
            }

            return null;

        } else if (element instanceof JSGraphQLEndpointDocumentationAware) {
            final JSGraphQLEndpointDocumentationAware documentationAware = (JSGraphQLEndpointDocumentationAware) element;
            final String documentation = documentationAware.getDocumentation(fullDocumentation);
            String doc = DEFINITION_START + documentationAware.getDeclaration() + DEFINITION_END;
            if (documentation != null) {
                doc += CONTENT_START + StringEscapeUtils.escapeHtml(documentation) + CONTENT_END;
            }
            return doc;
        }

        return null;
    }

    @Nullable
    private String getDirectiveDocumentation(GraphQLSchema schema, GraphQLDirectiveDefinition parent) {
        final GraphQLIdentifier directiveName = parent.getNameIdentifier();
        if (directiveName == null) {
            return null;
        }
        final GraphQLDirective schemaDirective = schema.getFirstDirective(directiveName.getText());
        if (schemaDirective == null) {
            return null;
        }
        final StringBuilder result = new StringBuilder().append(DEFINITION_START);
        result.append("@").append(schemaDirective.getName());
        if (schemaDirective.isRepeatable()) {
            result.append(" ").append(GRAYED_START).append("(repeatable)").append(GRAYED_END);
        }
        result.append(DEFINITION_END);
        final String description = schemaDirective.getDescription();
        if (description != null) {
            result.append(CONTENT_START);
            result.append(GraphQLDocumentationMarkdownRenderer.getDescriptionAsHTML(description));
            result.append(CONTENT_END);
        }
        return result.toString();
    }

    @Nullable
    private String getEnumValueDocumentation(GraphQLSchema schema, GraphQLEnumValue parent) {
        final String enumName = GraphQLPsiUtil.getTypeName(parent, null);
        if (enumName != null) {
            com.intellij.lang.jsgraphql.types.schema.GraphQLType schemaType = schema.getType(enumName);
            if (schemaType instanceof GraphQLEnumType) {
                final String enumValueName = parent.getName();
                final StringBuilder result = new StringBuilder().append(DEFINITION_START);
                result.append(enumName).append(".").append(enumValueName);
                result.append(DEFINITION_END);
                for (GraphQLEnumValueDefinition enumValueDefinition : ((GraphQLEnumType) schemaType).getValues()) {
                    if (Objects.equals(enumValueDefinition.getName(), enumValueName)) {
                        final String description = enumValueDefinition.getDescription();
                        if (description != null) {
                            result.append(CONTENT_START);
                            result.append(GraphQLDocumentationMarkdownRenderer.getDescriptionAsHTML(description));
                            result.append(CONTENT_END);
                            return result.toString();
                        }
                    }
                }
                return result.toString();
            }
        }
        return null;
    }

    @Nullable
    private String getArgumentDocumentation(GraphQLSchema schema, GraphQLInputValueDefinition parent) {

        // input value definition defines an argument on a field or a directive, or a field on an input type

        final String inputValueName = parent.getName();
        if (inputValueName != null) {

            final PsiElement definition = PsiTreeUtil.getParentOfType(parent, GraphQLFieldDefinition.class, GraphQLDirectiveDefinition.class, GraphQLInputObjectTypeDefinition.class, GraphQLInputObjectTypeExtensionDefinition.class);
            if (definition instanceof GraphQLFieldDefinition) {

                final String typeName = GraphQLPsiUtil.getTypeName(parent, null);
                if (typeName != null) {
                    final com.intellij.lang.jsgraphql.types.schema.GraphQLType schemaType = schema.getType(typeName);
                    List<com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition> fieldDefinitions;
                    if (schemaType instanceof GraphQLObjectType) {
                        fieldDefinitions = ((GraphQLObjectType) schemaType).getFieldDefinitions();
                    } else if (schemaType instanceof GraphQLInterfaceType) {
                        fieldDefinitions = ((GraphQLInterfaceType) schemaType).getFieldDefinitions();
                    } else {
                        return null;
                    }
                    final String fieldName = ((GraphQLFieldDefinition) definition).getName();
                    for (com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition fieldDefinition : fieldDefinitions) {
                        if (Objects.equals(fieldDefinition.getName(), fieldName)) {
                            for (GraphQLArgument argument : fieldDefinition.getArguments()) {
                                if (Objects.equals(argument.getName(), inputValueName)) {
                                    return getArgumentDocumentation(inputValueName, argument);
                                }
                            }
                        }
                    }
                }

            } else if (definition instanceof GraphQLDirectiveDefinition) {

                final GraphQLIdentifier directiveName = ((GraphQLDirectiveDefinition) definition).getNameIdentifier();
                if (directiveName != null) {
                    final GraphQLDirective schemaDirective = schema.getFirstDirective(directiveName.getText());
                    if (schemaDirective != null) {
                        for (GraphQLArgument argument : schemaDirective.getArguments()) {
                            if (inputValueName.equals(argument.getName())) {
                                return getArgumentDocumentation(inputValueName, argument);
                            }
                        }
                    }
                }
            } else if (definition instanceof GraphQLInputObjectTypeDefinition || definition instanceof GraphQLInputObjectTypeExtensionDefinition) {

                final String inputTypeName = GraphQLPsiUtil.getTypeName(parent, null);
                final com.intellij.lang.jsgraphql.types.schema.GraphQLType schemaType = schema.getType(inputTypeName);
                if (schemaType instanceof GraphQLInputObjectType) {
                    for (GraphQLInputObjectField inputObjectField : ((GraphQLInputObjectType) schemaType).getFieldDefinitions()) {
                        if (inputValueName.equals(inputObjectField.getName())) {
                            GraphQLInputType type = inputObjectField.getType();
                            final StringBuilder result = new StringBuilder().append(DEFINITION_START);
                            result.append(GraphQLSchemaUtil.getTypeName(schemaType)).append(".");
                            result.append(inputValueName).append(type != null ? ": " : "").append(type != null ? GraphQLSchemaUtil.getTypeName(type) : "");
                            result.append(DEFINITION_END);

                            final String description = inputObjectField.getDescription();
                            appendDescription(result, description);
                            return result.toString();
                        }
                    }
                }
            }
        }

        return null;
    }

    @NotNull
    private String getArgumentDocumentation(String inputValueName, GraphQLArgument argument) {
        final StringBuilder html = new StringBuilder().append(DEFINITION_START);
        GraphQLInputType argumentType = argument.getType();
        html.append(inputValueName).append(argumentType != null ? ": " : " ").append(argumentType != null ? GraphQLSchemaUtil.getTypeName(argumentType) : "");
        html.append(DEFINITION_END);
        appendDescription(html, GraphQLDocumentationMarkdownRenderer.getDescriptionAsHTML(argument.getDescription()));
        return html.toString();
    }

    private void appendDescription(StringBuilder result, @Nullable String descriptionAsHTML) {
        if (descriptionAsHTML == null) return;
        result.append(CONTENT_START).append(descriptionAsHTML).append(CONTENT_END);
    }

    @Nullable
    private String getFieldDocumentation(PsiElement element, GraphQLSchema schema, GraphQLFieldDefinition parent) {
        final GraphQLType psiFieldType = parent.getType();
        final GraphQLTypeSystemDefinition psiDefinition = PsiTreeUtil.getParentOfType(parent, GraphQLTypeSystemDefinition.class);
        final GraphQLNamedElement psiTypeName = PsiTreeUtil.findChildOfType(psiDefinition, GraphQLNamedElement.class);
        if (psiTypeName != null) {
            final com.intellij.lang.jsgraphql.types.schema.GraphQLType schemaType = schema.getType(psiTypeName.getText());
            if (schemaType != null) {
                final String fieldName = element.getText();
                final StringBuilder html = new StringBuilder().append(DEFINITION_START);
                html.append(GraphQLSchemaUtil.getTypeName(schemaType)).append(".");
                html.append(fieldName).append(psiFieldType != null ? ": " : "").append(psiFieldType != null ? psiFieldType.getText() : "");
                html.append(DEFINITION_END);
                List<com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition> fieldDefinitions = null;
                if (schemaType instanceof GraphQLObjectType) {
                    fieldDefinitions = ((GraphQLObjectType) schemaType).getFieldDefinitions();
                } else if (schemaType instanceof GraphQLInterfaceType) {
                    fieldDefinitions = ((GraphQLInterfaceType) schemaType).getFieldDefinitions();
                }
                if (fieldDefinitions != null) {
                    for (com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition fieldDefinition : fieldDefinitions) {
                        if (fieldName.equals(fieldDefinition.getName())) {
                            appendDescription(html, GraphQLDocumentationMarkdownRenderer.getDescriptionAsHTML(fieldDefinition.getDescription()));
                            break;
                        }
                    }
                }
                return html.toString();

            }
        }
        return null;
    }

    @Nullable
    private String getTypeDocumentation(PsiElement element, GraphQLSchema schema, GraphQLTypeNameDefinition parent) {
        com.intellij.lang.jsgraphql.types.schema.GraphQLType schemaType = schema.getType(((GraphQLNamedElement) element).getName());
        if (schemaType != null) {
            final StringBuilder html = new StringBuilder().append(DEFINITION_START);
            PsiElement keyword = PsiTreeUtil.prevVisibleLeaf(parent);
            if (keyword != null) {
                html.append(keyword.getText()).append(" ");
            }
            html.append(element.getText());
            html.append(DEFINITION_END);
            final String description = GraphQLSchemaUtil.getTypeDescription(schemaType);
            if (description != null) {
                html.append(CONTENT_START);
                html.append(GraphQLDocumentationMarkdownRenderer.getDescriptionAsHTML(description));
                html.append(CONTENT_END);
            }
            return html.toString();
        }
        return null;
    }


}

