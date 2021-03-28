/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.*;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Stack;

import static com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil.*;

public class GraphQLSchemaUtil {

    /**
     * Provides the IDL string version of a type including handling of types wrapped in non-null/list-types
     */
    public static String typeString(GraphQLType rawType) {

        final StringBuilder sb = new StringBuilder();
        final Stack<String> stack = new Stack<>();

        GraphQLType type = rawType;
        while (true) {
            if (type instanceof GraphQLNonNull) {
                type = ((GraphQLNonNull) type).getWrappedType();
                stack.push("!");
            } else if (type instanceof GraphQLList) {
                type = ((GraphQLList) type).getWrappedType();
                sb.append("[");
                stack.push("]");
            } else if (type instanceof GraphQLUnmodifiedType) {
                sb.append(((GraphQLUnmodifiedType) type).getName());
                break;
            } else {
                sb.append(type.toString());
                break;
            }
        }
        while (!stack.isEmpty()) {
            sb.append(stack.pop());
        }

        return sb.toString();

    }

    @Nullable
    public static Description getTypeDefinitionDescription(@NotNull TypeDefinition<?> typeDefinition) {
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

    @Nullable
    public static String getTypeDescription(@NotNull GraphQLType graphQLType) {
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

    public static boolean isExtension(@Nullable SDLDefinition<?> definition) {
        return definition instanceof SchemaExtensionDefinition ||
            definition instanceof InputObjectTypeExtensionDefinition ||
            definition instanceof ObjectTypeExtensionDefinition ||
            definition instanceof InterfaceTypeExtensionDefinition ||
            definition instanceof ScalarTypeExtensionDefinition ||
            definition instanceof UnionTypeExtensionDefinition ||
            definition instanceof EnumTypeExtensionDefinition;
    }

    public static @NotNull String getTypeName(@Nullable GraphQLType type) {
        String typeName = null;
        if (type instanceof GraphQLNamedSchemaElement) {
            typeName = ((GraphQLNamedSchemaElement) type).getName();
        }
        return StringUtil.notNullize(typeName);
    }

    /**
     * Gets the raw named type that sits within a non-null/list modifier type, or the type as-is if no unwrapping is needed
     *
     * @param graphQLType the type to unwrap
     * @return the raw type as-is, or the type wrapped inside a non-null/list modifier type
     */
    public static GraphQLUnmodifiedType getUnmodifiedType(GraphQLType graphQLType) {
        if (graphQLType instanceof GraphQLModifiedType) {
            return getUnmodifiedType(((GraphQLModifiedType) graphQLType).getWrappedType());
        }
        return (GraphQLUnmodifiedType) graphQLType;
    }

    public static GraphQLType unwrapListType(GraphQLType type) {
        while (isWrapped(type)) {
            if (isList(type)) {
                return unwrapOne(type);
            }
            type = unwrapOne(type);
        }
        return type;
    }
}
