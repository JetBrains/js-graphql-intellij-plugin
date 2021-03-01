package com.intellij.lang.jsgraphql.schema;

import com.intellij.lang.jsgraphql.schema.builder.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ObjectUtils;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.GraphQLException;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry;
import com.intellij.lang.jsgraphql.types.schema.idl.errors.SchemaProblem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main purpose of this class is to avoid some possible errors during a schema build.
 * We should separate schema building process from its validation, because the user provided types aren't always valid
 * or could become valid after some processing like in graphql-modules library.
 */
class GraphQLRegistryTolerantBuilder implements GraphQLRegistryBuilder {
    private static final Logger LOG = Logger.getInstance(GraphQLRegistryTolerantBuilder.class);

    private final Map<String, GraphQLDefinitionBuilder<?>> myNamedDefinitionBuilders = new HashMap<>();
    private final GraphQLSchemaTypeDefinitionBuilder mySchemaDefinitionBuilder = new GraphQLSchemaTypeDefinitionBuilder();
    private final List<GraphQLException> myErrors = new ArrayList<>();

    @Override
    public void merge(@NotNull TypeDefinitionRegistry source) throws GraphQLException {
        if (source.schemaDefinition().isPresent()) {
            addTypeDefinition(source.schemaDefinition().get());
        }

        source.types().values().forEach(this::addTypeDefinition);
        source.getDirectiveDefinitions().values().forEach(this::addTypeDefinition);
        source.scalars().values().forEach(this::addTypeDefinition);

        source.getSchemaExtensionDefinitions().forEach(this::addExtensionDefinition);
        source.objectTypeExtensions().forEach((key, value) -> value.forEach(this::addExtensionDefinition));
        source.interfaceTypeExtensions().forEach((key, value) -> value.forEach(this::addExtensionDefinition));
        source.unionTypeExtensions().forEach((key, value) -> value.forEach(this::addExtensionDefinition));
        source.enumTypeExtensions().forEach((key, value) -> value.forEach(this::addExtensionDefinition));
        source.scalarTypeExtensions().forEach((key, value) -> value.forEach(this::addExtensionDefinition));
        source.inputObjectTypeExtensions().forEach((key, value) -> value.forEach(this::addExtensionDefinition));
    }

    @NotNull
    private static GraphQLDefinitionBuilder<?> createBuilder(@NotNull SDLDefinition<?> definition) {
        if (definition instanceof InputObjectTypeDefinition) {
            return new GraphQLInputObjectTypeDefinitionBuilder();
        } else if (definition instanceof ObjectTypeDefinition) {
            return new GraphQLObjectTypeDefinitionBuilder();
        } else if (definition instanceof InterfaceTypeDefinition) {
            return new GraphQLInterfaceTypeDefinitionBuilder();
        } else if (definition instanceof UnionTypeDefinition) {
            return new GraphQLUnionTypeDefinitionBuilder();
        } else if (definition instanceof EnumTypeDefinition) {
            return new GraphQLEnumTypeDefinitionBuilder();
        } else if (definition instanceof ScalarTypeDefinition) {
            return new GraphQLScalarTypeDefinitionBuilder();
        } else if (definition instanceof DirectiveDefinition) {
            return new GraphQLDirectiveTypeDefinitionBuilder();
        } else if (definition instanceof SchemaDefinition) {
            return new GraphQLSchemaTypeDefinitionBuilder();
        } else {
            throw new IllegalStateException("Unknown definition type: " + definition.getClass().getName());
        }
    }

    @Nullable
    private GraphQLDefinitionBuilder<?> getBuilder(@NotNull SDLDefinition<?> definition) {
        if (definition instanceof SchemaDefinition) {
            return mySchemaDefinitionBuilder;
        }

        if (!(definition instanceof NamedNode)) {
            return null;
        }

        return myNamedDefinitionBuilders.computeIfAbsent(
            ((NamedNode<?>) definition).getName(), name -> createBuilder(definition));
    }

    private void addTypeDefinition(@NotNull SDLDefinition<?> definition) {
        GraphQLDefinitionBuilder<?> builder = getBuilder(definition);

        if (builder == null) {
            LOG.warn("No suitable builder for " + definition.getClass().getName());
            return;
        }

        if (builder instanceof GraphQLDirectiveTypeDefinitionBuilder) {
            ((GraphQLDirectiveTypeDefinitionBuilder) builder).addDefinition(ObjectUtils.tryCast(definition, DirectiveDefinition.class));
        } else if (builder instanceof GraphQLEnumTypeDefinitionBuilder) {
            ((GraphQLEnumTypeDefinitionBuilder) builder).addDefinition(ObjectUtils.tryCast(definition, EnumTypeDefinition.class));
        } else if (builder instanceof GraphQLInputObjectTypeDefinitionBuilder) {
            ((GraphQLInputObjectTypeDefinitionBuilder) builder).addDefinition(ObjectUtils.tryCast(definition, InputObjectTypeDefinition.class));
        } else if (builder instanceof GraphQLInterfaceTypeDefinitionBuilder) {
            ((GraphQLInterfaceTypeDefinitionBuilder) builder).addDefinition(ObjectUtils.tryCast(definition, InterfaceTypeDefinition.class));
        } else if (builder instanceof GraphQLObjectTypeDefinitionBuilder) {
            ((GraphQLObjectTypeDefinitionBuilder) builder).addDefinition(ObjectUtils.tryCast(definition, ObjectTypeDefinition.class));
        } else if (builder instanceof GraphQLScalarTypeDefinitionBuilder) {
            ((GraphQLScalarTypeDefinitionBuilder) builder).addDefinition(ObjectUtils.tryCast(definition, ScalarTypeDefinition.class));
        } else if (builder instanceof GraphQLSchemaTypeDefinitionBuilder) {
            ((GraphQLSchemaTypeDefinitionBuilder) builder).addDefinition(ObjectUtils.tryCast(definition, SchemaDefinition.class));
        } else if (builder instanceof GraphQLUnionTypeDefinitionBuilder) {
            ((GraphQLUnionTypeDefinitionBuilder) builder).addDefinition(ObjectUtils.tryCast(definition, UnionTypeDefinition.class));
        } else {
            LOG.error("Unknown builder type: " + builder.getClass().getName());
        }
    }

    private void addExtensionDefinition(@NotNull SDLDefinition<?> definition) {
        GraphQLDefinitionBuilder<?> builder = getBuilder(definition);

        if (builder == null) {
            LOG.warn("No suitable builder for extension definition " + definition.getClass().getName());
            return;
        }

        if (!(builder instanceof GraphQLExtendableDefinitionBuilder)) {
            return;
        }

        if (builder instanceof GraphQLEnumTypeDefinitionBuilder) {
            ((GraphQLEnumTypeDefinitionBuilder) builder).addExtension(ObjectUtils.tryCast(definition, EnumTypeExtensionDefinition.class));
        } else if (builder instanceof GraphQLInputObjectTypeDefinitionBuilder) {
            ((GraphQLInputObjectTypeDefinitionBuilder) builder).addExtension(ObjectUtils.tryCast(definition, InputObjectTypeExtensionDefinition.class));
        } else if (builder instanceof GraphQLInterfaceTypeDefinitionBuilder) {
            ((GraphQLInterfaceTypeDefinitionBuilder) builder).addExtension(ObjectUtils.tryCast(definition, InterfaceTypeExtensionDefinition.class));
        } else if (builder instanceof GraphQLObjectTypeDefinitionBuilder) {
            ((GraphQLObjectTypeDefinitionBuilder) builder).addExtension(ObjectUtils.tryCast(definition, ObjectTypeExtensionDefinition.class));
        } else if (builder instanceof GraphQLScalarTypeDefinitionBuilder) {
            ((GraphQLScalarTypeDefinitionBuilder) builder).addExtension(ObjectUtils.tryCast(definition, ScalarTypeExtensionDefinition.class));
        } else if (builder instanceof GraphQLSchemaTypeDefinitionBuilder) {
            ((GraphQLSchemaTypeDefinitionBuilder) builder).addExtension(ObjectUtils.tryCast(definition, SchemaExtensionDefinition.class));
        } else if (builder instanceof GraphQLUnionTypeDefinitionBuilder) {
            ((GraphQLUnionTypeDefinitionBuilder) builder).addExtension(ObjectUtils.tryCast(definition, UnionTypeExtensionDefinition.class));
        } else {
            LOG.error("Unknown extension builder type: " + builder.getClass().getName());
        }
    }


    @NotNull
    @Override
    public TypeDefinitionRegistry build() {
        List<GraphQLError> errors = new ArrayList<>();
        TypeDefinitionRegistry registry = new TypeDefinitionRegistry();

        SchemaDefinition schemaDefinition = mySchemaDefinitionBuilder.buildDefinition();
        if (schemaDefinition != null) {
            registry.add(schemaDefinition).ifPresent(errors::add);
        }
        mySchemaDefinitionBuilder.buildExtensions().forEach(extension -> registry.add(extension).ifPresent(errors::add));

        myNamedDefinitionBuilders.values().forEach(builder -> {
            SDLDefinition<?> definition = builder.buildDefinition();
            if (definition != null) {
                registry.add(definition).ifPresent(errors::add);
            }

            if (builder instanceof GraphQLExtendableDefinitionBuilder) {
                ((GraphQLExtendableDefinitionBuilder<?, ?>) builder).buildExtensions()
                    .forEach(extension -> registry.add(extension).ifPresent(errors::add));
            }
        });

        if (!errors.isEmpty()) {
            myErrors.add(new SchemaProblem(errors));
        }
        return registry;
    }

    @NotNull
    @Override
    public List<GraphQLException> getErrors() {
        return myErrors;
    }

}
