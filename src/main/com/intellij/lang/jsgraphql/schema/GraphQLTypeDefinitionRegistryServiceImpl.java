/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.google.common.collect.Lists;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import graphql.GraphQLException;
import graphql.language.*;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLUnionType;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class GraphQLTypeDefinitionRegistryServiceImpl implements GraphQLTypeDefinitionRegistryService {

    public static final GraphQLSchema EMPTY_SCHEMA = GraphQLSchema.newSchema().query(GraphQLObjectType.newObject().name("Query").build()).build();

    private Project project;

    public static GraphQLTypeDefinitionRegistryServiceImpl getService(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLTypeDefinitionRegistryServiceImpl.class);
    }

    public GraphQLTypeDefinitionRegistryServiceImpl(Project project) {
        this.project = project;
    }

    @Override
    public TypeDefinitionRegistry getRegistry(PsiElement psiElement) {

        // TODO JKM can cache schema as long as not a modification to type system definitions
        // or a GraphQL file is deleted/added

        return SchemaIDLTypeDefinitionRegistry.getService(project).getRegistry(psiElement);

    }

    @Override
    public GraphQLSchemaWithErrors getSchemaWithErrors(PsiElement psiElement) {
        final TypeDefinitionRegistryWithErrors registryWithErrors = SchemaIDLTypeDefinitionRegistry.getService(project).getRegistryWithErrors(psiElement);
        try {
            final GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(registryWithErrors.getRegistry());
            return new GraphQLSchemaWithErrors(schema, Collections.emptyList(), registryWithErrors);
        } catch (GraphQLException e) {
            return new GraphQLSchemaWithErrors(EMPTY_SCHEMA, Lists.newArrayList(e), registryWithErrors);
        }
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
}
