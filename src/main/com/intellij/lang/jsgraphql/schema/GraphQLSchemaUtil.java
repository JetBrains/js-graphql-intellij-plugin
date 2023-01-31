/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider;
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeCondition;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.*;
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil.*;

public class GraphQLSchemaUtil {

    /**
     * Provides the IDL string version of a type including handling of types wrapped in non-null/list-types
     */
    @NotNull
    public static String typeString(@Nullable GraphQLType rawType) {
        if (rawType == null) {
            return "";
        }

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

    public static @NotNull String getValueTypeName(@NotNull Object value) {
        if (value instanceof IntValue) {
            return "Int";
        } else if (value instanceof FloatValue) {
            return "Float";
        } else if (value instanceof StringValue) {
            return "String";
        } else if (value instanceof EnumValue) {
            return "Enum";
        } else if (value instanceof BooleanValue) {
            return "Boolean";
        } else if (value instanceof NullValue) {
            return "null";
        } else if (value instanceof ArrayValue) {
            return "Array";
        } else if (value instanceof ObjectValue) {
            return "Object";
        } else if (value instanceof VariableReference) {
            return "Reference";
        }
        return value.getClass().getSimpleName();
    }

    @NotNull
    public static Set<String> getSchemaOperationTypeNames(@NotNull GraphQLSchema schema) {
        HashSet<String> types = new HashSet<>();

        GraphQLObjectType queryType = schema.getQueryType();
        types.add(queryType != null && queryType.getName() != null
            ? queryType.getName() : GraphQLKnownTypes.QUERY_TYPE);

        GraphQLObjectType mutationType = schema.getMutationType();
        types.add(mutationType != null && mutationType.getName() != null
            ? mutationType.getName() : GraphQLKnownTypes.MUTATION_TYPE);

        GraphQLObjectType subscriptionType = schema.getSubscriptionType();
        types.add(subscriptionType != null && subscriptionType.getName() != null
            ? subscriptionType.getName() : GraphQLKnownTypes.SUBSCRIPTION_TYPE);

        return types;
    }

    public static boolean hasRequiredArgs(@Nullable GraphQLFieldDefinition field) {
        if (field == null) {
            return false;
        }

        boolean hasRequiredArgs = false;
        for (GraphQLArgument fieldArgument : field.getArguments()) {
            if (fieldArgument.getType() instanceof GraphQLNonNull) {
                hasRequiredArgs = true;
                break;
            }
        }
        return hasRequiredArgs;
    }

    /**
     * Gets whether the specified fragment candidate is valid to spread inside the specified required type scope
     *
     * @param typeDefinitionRegistry registry with available schema types, used to resolve union members and interface implementations
     * @param fragmentCandidate      the fragment to check for being able to validly spread under the required type scope
     * @param requiredTypeScope      the type scope in which the fragment is a candidate to spread
     * @return true if the fragment candidate is valid to be spread inside the type scope
     */
    public static boolean isFragmentApplicableInTypeScope(@NotNull TypeDefinitionRegistry typeDefinitionRegistry,
                                                          @NotNull GraphQLFragmentDefinition fragmentCandidate,
                                                          @NotNull GraphQLType requiredTypeScope) {

        // unwrap non-nullable and list types
        requiredTypeScope = getUnmodifiedType(requiredTypeScope);

        final GraphQLTypeCondition typeCondition = fragmentCandidate.getTypeCondition();
        if (typeCondition == null || typeCondition.getTypeName() == null) {
            return false;
        }

        final String fragmentTypeName = Optional.ofNullable(typeCondition.getTypeName().getName()).orElse("");
        if (fragmentTypeName.equals(getTypeName(requiredTypeScope))) {
            // direct match, e.g. User scope, fragment on User
            return true;
        }

        // check whether compatible based on interfaces and unions
        return isCompatibleFragment(typeDefinitionRegistry, requiredTypeScope, fragmentTypeName);

    }

    /**
     * Gets whether a fragment type condition name is compatible with the required type scope
     *
     * @param typeDefinitionRegistry registry with available schema types, used to resolve union members and interface implementations
     * @param rawRequiredTypeScope   the type scope in which the fragment is a candidate to spread
     * @param fragmentTypeName       the name of the type that a candidate fragment applies to
     * @return true if the candidate type condtion name is compatible inside the required type scope
     */
    private static boolean isCompatibleFragment(TypeDefinitionRegistry typeDefinitionRegistry,
                                                GraphQLType rawRequiredTypeScope,
                                                String fragmentTypeName) {

        // unwrap non-nullable and list types
        GraphQLUnmodifiedType requiredTypeScope = getUnmodifiedType(rawRequiredTypeScope);

        if (requiredTypeScope instanceof GraphQLInterfaceType) {
            // also include fragments on types implementing the interface scope
            final TypeDefinition typeScopeDefinition = typeDefinitionRegistry.types().get(requiredTypeScope.getName());
            if (typeScopeDefinition != null) {
                final List<ObjectTypeDefinition> implementations = typeDefinitionRegistry.getImplementationsOf(
                    (InterfaceTypeDefinition) typeScopeDefinition);
                for (ObjectTypeDefinition implementation : implementations) {
                    if (implementation.getName().equals(fragmentTypeName)) {
                        return true;
                    }
                }
            }
        } else if (requiredTypeScope instanceof GraphQLObjectType) {
            // include fragments on the interfaces implemented by the object type
            for (GraphQLNamedOutputType graphQLOutputType : ((GraphQLObjectType) requiredTypeScope).getInterfaces()) {
                if (graphQLOutputType.getName().equals(fragmentTypeName)) {
                    return true;
                }
            }
        } else if (requiredTypeScope instanceof GraphQLUnionType) {
            for (GraphQLNamedOutputType graphQLOutputType : ((GraphQLUnionType) requiredTypeScope).getTypes()) {
                // check each type in the union for compatibility
                if (graphQLOutputType.getName().equals(fragmentTypeName) ||
                    isCompatibleFragment(typeDefinitionRegistry, graphQLOutputType, fragmentTypeName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Collection<? extends ModificationTracker> getSchemaDependencies(@NotNull Project project) {
        return Arrays.asList(
            GraphQLSchemaContentTracker.getInstance(project),
            GraphQLConfigProvider.getInstance(project),
            VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS
        );
    }

    public static ModificationTracker getSchemaModificationTracker(@NotNull Project project) {
        return () -> GraphQLSchemaContentTracker.getInstance(project).getModificationCount() +
            GraphQLConfigProvider.getInstance(project).getModificationCount() +
            VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS.getModificationCount();
    }
}
