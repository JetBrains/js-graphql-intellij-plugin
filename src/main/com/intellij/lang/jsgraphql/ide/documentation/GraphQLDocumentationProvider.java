/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.documentation;

import com.intellij.lang.documentation.DocumentationProviderEx;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointDocumentationAware;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointFile;
import com.intellij.lang.jsgraphql.psi.GraphQLDirectiveDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLEnumValue;
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.psi.GraphQLIdentifier;
import com.intellij.lang.jsgraphql.psi.GraphQLInputObjectTypeDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLInputObjectTypeExtensionDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLInputValueDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLNamedElement;
import com.intellij.lang.jsgraphql.psi.GraphQLPsiUtil;
import com.intellij.lang.jsgraphql.psi.GraphQLType;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeNameDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeSystemDefinition;
import com.intellij.lang.jsgraphql.schema.GraphQLTypeDefinitionRegistryServiceImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLEnumValueDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.apache.commons.lang.StringEscapeUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class GraphQLDocumentationProvider extends DocumentationProviderEx {

    private final static String GRAPHQL_DOC_PREFIX = "GraphQL";
    private final static String GRAPHQL_DOC_TYPE = "Type";

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
        if (isDocumentationSupported(element)) {
            return createQuickNavigateDocumentation(element, true);
        }
        return null;
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

        final GraphQLTypeDefinitionRegistryServiceImpl typeRegistryService = GraphQLTypeDefinitionRegistryServiceImpl.getService(element.getProject());
        final GraphQLSchema schema = typeRegistryService.getSchema(element);

        if (element instanceof GraphQLNamedElement) {

            final PsiElement parent = element.getParent();

            if (parent instanceof GraphQLTypeNameDefinition) {
                return getTypeDocumentation(element, typeRegistryService, schema, (GraphQLTypeNameDefinition) parent);
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
            String doc = "";
            if (documentation != null) {
                doc += "<div style=\"margin-bottom: 4px\">" + StringEscapeUtils.escapeHtml(documentation) + "</div>";
            }
            doc += "<code>" + documentationAware.getDeclaration() + "</code>";
            return doc;
        }

        return null;
    }

    @Nullable
    private String getDirectiveDocumentation(GraphQLSchema schema, GraphQLDirectiveDefinition parent) {
        final GraphQLIdentifier directiveName = parent.getNameIdentifier();
        if (directiveName != null) {
            final GraphQLDirective schemaDirective = schema.getDirective(directiveName.getText());
            if (schemaDirective != null) {
                final String description = schemaDirective.getDescription();
                if (description != null) {
                    return GraphQLDocumentationMarkdownRenderer.getDescriptionAsHTML(description);
                }
            }
        }
        return null;
    }

    @Nullable
    private String getEnumValueDocumentation(GraphQLSchema schema, GraphQLEnumValue parent) {
        final String enumName = GraphQLPsiUtil.getTypeName(parent, null);
        if (enumName != null) {
            graphql.schema.GraphQLType schemaType = schema.getType(enumName);
            if (schemaType instanceof GraphQLEnumType) {
                final String enumValueName = parent.getName();
                for (GraphQLEnumValueDefinition enumValueDefinition : ((GraphQLEnumType) schemaType).getValues()) {
                    if (Objects.equals(enumValueDefinition.getName(), enumValueName)) {
                        final String description = enumValueDefinition.getDescription();
                        if (description != null) {
                            return GraphQLDocumentationMarkdownRenderer.getDescriptionAsHTML(description);
                        }
                    }
                }
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
                    final graphql.schema.GraphQLType schemaType = schema.getType(typeName);
                    List<graphql.schema.GraphQLFieldDefinition> fieldDefinitions;
                    if (schemaType instanceof GraphQLObjectType) {
                        fieldDefinitions = ((GraphQLObjectType) schemaType).getFieldDefinitions();
                    } else if (schemaType instanceof GraphQLInterfaceType) {
                        fieldDefinitions = ((GraphQLInterfaceType) schemaType).getFieldDefinitions();
                    } else {
                        return null;
                    }
                    final String fieldName = ((GraphQLFieldDefinition) definition).getName();
                    for (graphql.schema.GraphQLFieldDefinition fieldDefinition : fieldDefinitions) {
                        if (Objects.equals(fieldDefinition.getName(), fieldName)) {
                            for (GraphQLArgument argument : fieldDefinition.getArguments()) {
                                if (Objects.equals(argument.getName(), inputValueName)) {
                                    if (argument.getDescription() != null) {
                                        return GraphQLDocumentationMarkdownRenderer.getDescriptionAsHTML(argument.getDescription());
                                    }
                                }
                            }
                        }
                    }
                }

            } else if (definition instanceof GraphQLDirectiveDefinition) {

                final GraphQLIdentifier directiveName = ((GraphQLDirectiveDefinition) definition).getNameIdentifier();
                if (directiveName != null) {
                    final GraphQLDirective schemaDirective = schema.getDirective(directiveName.getText());
                    if (schemaDirective != null) {
                        for (GraphQLArgument argument : schemaDirective.getArguments()) {
                            if (inputValueName.equals(argument.getName())) {
                                final String description = argument.getDescription();
                                if (description != null) {
                                    return GraphQLDocumentationMarkdownRenderer.getDescriptionAsHTML(description);
                                }
                                break;
                            }
                        }
                    }
                }
            } else if (definition instanceof GraphQLInputObjectTypeDefinition || definition instanceof GraphQLInputObjectTypeExtensionDefinition) {

                final String inputTypeName = GraphQLPsiUtil.getTypeName(parent, null);
                final graphql.schema.GraphQLType schemaType = schema.getType(inputTypeName);
                if (schemaType instanceof GraphQLInputObjectType) {
                    for (GraphQLInputObjectField inputObjectField : ((GraphQLInputObjectType) schemaType).getFieldDefinitions()) {
                        if (inputValueName.equals(inputObjectField.getName())) {
                            final String description = inputObjectField.getDescription();
                            if (description != null) {
                                return GraphQLDocumentationMarkdownRenderer.getDescriptionAsHTML(description);
                            }
                            break;
                        }
                    }
                }
            }
        }

        return null;
    }

    @Nullable
    private String getFieldDocumentation(PsiElement element, GraphQLSchema schema, GraphQLFieldDefinition parent) {
        final GraphQLType psiFieldType = parent.getType();
        final GraphQLTypeSystemDefinition psiDefinition = PsiTreeUtil.getParentOfType(parent, GraphQLTypeSystemDefinition.class);
        final GraphQLNamedElement psiTypeName = PsiTreeUtil.findChildOfType(psiDefinition, GraphQLNamedElement.class);
        if (psiTypeName != null) {
            final graphql.schema.GraphQLType schemaType = schema.getType(psiTypeName.getText());
            if (schemaType != null) {
                final String fieldName = element.getText();
                final StringBuilder html = new StringBuilder().append("<header><code>");
                html.append(schemaType.getName()).append(" ");
                html.append(fieldName).append(": ").append(psiFieldType != null ? psiFieldType.getText() : "");
                html.append("</code></header>");
                List<graphql.schema.GraphQLFieldDefinition> fieldDefinitions = null;
                if (schemaType instanceof GraphQLObjectType) {
                    fieldDefinitions = ((GraphQLObjectType) schemaType).getFieldDefinitions();
                } else if (schemaType instanceof GraphQLInterfaceType) {
                    fieldDefinitions = ((GraphQLInterfaceType) schemaType).getFieldDefinitions();
                }
                if (fieldDefinitions != null) {
                    for (graphql.schema.GraphQLFieldDefinition fieldDefinition : fieldDefinitions) {
                        if (fieldName.equals(fieldDefinition.getName())) {
                            if (fieldDefinition.getDescription() != null) {
                                html.append("<section>");
                                html.append(GraphQLDocumentationMarkdownRenderer.getDescriptionAsHTML(fieldDefinition.getDescription()));
                                html.append("</section>");
                            }
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
    private String getTypeDocumentation(PsiElement element, GraphQLTypeDefinitionRegistryServiceImpl typeRegistryService, GraphQLSchema schema, GraphQLTypeNameDefinition parent) {
        graphql.schema.GraphQLType schemaType = schema.getType(((GraphQLNamedElement) element).getName());
        if (schemaType != null) {
            final String description = typeRegistryService.getTypeDescription(schemaType);
            if (description != null) {
                final StringBuilder html = new StringBuilder().append("<header><code>");
                PsiElement keyword = PsiTreeUtil.prevVisibleLeaf(parent);
                if (keyword != null) {
                    html.append(keyword.getText()).append(" ");
                }
                html.append(element.getText());
                html.append("</code></header><section>");
                html.append(GraphQLDocumentationMarkdownRenderer.getDescriptionAsHTML(description));
                html.append("</section>");
                return html.toString();
            }
        }
        return null;
    }


}

