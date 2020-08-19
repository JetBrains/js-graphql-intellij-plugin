/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.lang.jsgraphql.psi.GraphQLPsiUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import graphql.Directives;
import graphql.GraphQLException;
import graphql.language.*;
import graphql.schema.*;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class GraphQLTypeDefinitionRegistryServiceImpl implements GraphQLTypeDefinitionRegistryService {

    public static final GraphQLSchema EMPTY_SCHEMA = GraphQLSchema.newSchema().query(GraphQLObjectType.newObject().name("Query").build()).build();

    private Project project;

    private final Map<String, TypeDefinitionRegistryWithErrors> fileNameToRegistry = Maps.newConcurrentMap();
    private final Map<String, GraphQLSchemaWithErrors> fileNameToSchema = Maps.newConcurrentMap();

    public static GraphQLTypeDefinitionRegistryServiceImpl getService(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLTypeDefinitionRegistryServiceImpl.class);
    }

    public GraphQLTypeDefinitionRegistryServiceImpl(Project project) {
        this.project = project;
        project.getMessageBus().connect().subscribe(GraphQLSchemaChangeListener.TOPIC, new GraphQLSchemaEventListener() {
            @Override
            public void onGraphQLSchemaChanged(Integer schemaVersion) {
                // clear the cache on each PSI change
                fileNameToRegistry.clear();
                fileNameToSchema.clear();
            }
        });

    }

    @Override
    public TypeDefinitionRegistryWithErrors getRegistryWithErrors(PsiElement psiElement) { ;
        return fileNameToRegistry.computeIfAbsent(GraphQLPsiUtil.getFileName(psiElement.getContainingFile()), fileName -> {
            return SchemaIDLTypeDefinitionRegistry.getService(project).getRegistryWithErrors(psiElement);
        });
    }

    @Override
    public TypeDefinitionRegistry getRegistry(PsiElement psiElement) {
        return getRegistryWithErrors(psiElement).getRegistry();
    }

    @Override
    public GraphQLSchemaWithErrors getSchemaWithErrors(PsiElement psiElement) {
        return fileNameToSchema.computeIfAbsent(GraphQLPsiUtil.getFileName(psiElement.getContainingFile()), fileName -> {
            final TypeDefinitionRegistryWithErrors registryWithErrors = getRegistryWithErrors(psiElement);
            try {
                final GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(registryWithErrors.getRegistry());
                for (GraphQLDirective directive : schema.getDirectives()) {
                    if (Directives.DeferDirective.getName().equals(directive.getName()) && directive != Directives.DeferDirective) {
                        // more than one @defer (one was declared in addition to the built-in one from graphql-java)
                        removeBuiltInDirective(schema, Directives.DeferDirective);
                        break;
                    }
                }
                return new GraphQLSchemaWithErrors(schema, Collections.emptyList(), registryWithErrors);
            } catch (GraphQLException e) {
                return new GraphQLSchemaWithErrors(EMPTY_SCHEMA, Lists.newArrayList(e), registryWithErrors);
            } catch (Exception e) {
                return new GraphQLSchemaWithErrors(EMPTY_SCHEMA, Lists.newArrayList(new GraphQLException(e)), registryWithErrors);
            }
        });
    }

    @Override
    public GraphQLSchema getSchema(PsiElement psiElement) {
        return getSchemaWithErrors(psiElement).getSchema();
    }

    public Description getTypeDefinitionDescription(TypeDefinition typeDefinition) {
        Description description = null;
        if (typeDefinition instanceof ObjectTypeDefinition) {
            description = ((ObjectTypeDefinition) typeDefinition).getDescription();
        } else if (typeDefinition instanceof InterfaceTypeDefinition) {
            description = ((InterfaceTypeDefinition) typeDefinition).getDescription();
        } else if (typeDefinition instanceof EnumTypeDefinition) {
            description = ((EnumTypeDefinition) typeDefinition).getDescription();
        } else if (typeDefinition instanceof ScalarTypeDefinition) {
            description = ((ScalarTypeDefinition) typeDefinition).getDescription();
        } else if (typeDefinition instanceof InputObjectTypeDefinition) {
            description = ((InputObjectTypeDefinition) typeDefinition).getDescription();
        } else if (typeDefinition instanceof UnionTypeDefinition) {
            description = ((UnionTypeDefinition) typeDefinition).getDescription();
        }
        return description;

    }

    public String getTypeDescription(GraphQLType graphQLType) {

        String description = null;
        if (graphQLType instanceof GraphQLObjectType) {
            description = ((GraphQLObjectType) graphQLType).getDescription();
        } else if (graphQLType instanceof GraphQLInterfaceType) {
            description = ((GraphQLInterfaceType) graphQLType).getDescription();
        } else if (graphQLType instanceof GraphQLEnumType) {
            description = ((GraphQLEnumType) graphQLType).getDescription();
        } else if (graphQLType instanceof GraphQLScalarType) {
            description = ((GraphQLScalarType) graphQLType).getDescription();
        } else if (graphQLType instanceof GraphQLInputObjectType) {
            description = ((GraphQLInputObjectType) graphQLType).getDescription();
        } else if (graphQLType instanceof GraphQLUnionType) {
            description = ((GraphQLUnionType) graphQLType).getDescription();
        }
        return description;


    }

    // ---- https://github.com/graphql-java/graphql-java/issues/1399 ----
    // Remove graphql-java built-in @defer since it declares valid locations that can conflict with frameworks such as Relay Modern.
    // Unfortunately no API to do this without reflection right now.
    private static Field directives = getDirectivesField();
    private static Field getDirectivesField() {
        try {
            Field directives = GraphQLSchema.class.getDeclaredField("directives");
            directives.setAccessible(true);
            return directives;
        } catch (NoSuchFieldException ignored) {}
        return null;
    }

    private static void removeBuiltInDirective(GraphQLSchema graphQLSchema, GraphQLDirective directiveToRemove) {
        if (directives != null) {
            try {
                final Object knownDirectives = directives.get(graphQLSchema);
                if(knownDirectives instanceof Collection) {
                    ((Collection) knownDirectives).remove(directiveToRemove);
                }
            } catch (IllegalAccessException ignored) {}
        }
    }

    // ----
}
