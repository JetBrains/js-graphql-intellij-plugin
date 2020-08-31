package com.intellij.lang.jsgraphql.ide.editor;

import com.google.common.collect.Lists;
import com.intellij.util.containers.ContainerUtil;
import graphql.PublicApi;
import graphql.language.*;
import graphql.schema.idl.ScalarInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static graphql.Assert.*;

@SuppressWarnings("unchecked")
@PublicApi
public class GraphQLIntrospectionResultToSchema {

    /**
     * Returns a IDL Document that represents the schema as defined by the introspection result map
     *
     * @param introspectionResult the result of an introspection query on a schema
     * @return a IDL Document of the schema
     */
    @SuppressWarnings("unchecked")
    public Document createSchemaDefinition(@NotNull Map<String, Object> introspectionResult) {
        assertTrue(introspectionResult.get("__schema") != null, () -> "__schema expected");
        Map<String, Object> schema = (Map<String, Object>) introspectionResult.get("__schema");


        Map<String, Object> queryType = (Map<String, Object>) schema.get("queryType");
        assertNotNull(queryType, () -> "queryType expected");
        TypeName query = TypeName.newTypeName().name((String) queryType.get("name")).build();
        boolean nonDefaultQueryName = !"Query".equals(query.getName());

        SchemaDefinition.Builder schemaDefinition = SchemaDefinition.newSchemaDefinition();
        schemaDefinition.operationTypeDefinition(OperationTypeDefinition.newOperationTypeDefinition().name("query").typeName(query).build());

        Map<String, Object> mutationType = (Map<String, Object>) schema.get("mutationType");
        boolean nonDefaultMutationName = false;
        if (mutationType != null) {
            TypeName mutation = TypeName.newTypeName().name((String) mutationType.get("name")).build();
            nonDefaultMutationName = !"Mutation".equals(mutation.getName());
            schemaDefinition.operationTypeDefinition(OperationTypeDefinition.newOperationTypeDefinition().name("mutation").typeName(mutation).build());
        }

        Map<String, Object> subscriptionType = (Map<String, Object>) schema.get("subscriptionType");
        boolean nonDefaultSubscriptionName = false;
        if (subscriptionType != null) {
            TypeName subscription = TypeName.newTypeName().name(((String) subscriptionType.get("name"))).build();
            nonDefaultSubscriptionName = !"Subscription".equals(subscription.getName());
            schemaDefinition.operationTypeDefinition(OperationTypeDefinition.newOperationTypeDefinition().name("subscription").typeName(subscription).build());
        }

        Document.Builder document = Document.newDocument();
        if (nonDefaultQueryName || nonDefaultMutationName || nonDefaultSubscriptionName) {
            document.definition(schemaDefinition.build());
        }

        List<Map<String, Object>> types = (List<Map<String, Object>>) schema.get("types");
        if (types != null) {
            for (Map<String, Object> type : types) {
                if (type == null) continue;
                TypeDefinition<?> typeDefinition = createTypeDefinition(type);
                if (typeDefinition == null) continue;
                document.definition(typeDefinition);
            }
        }

        return document.build();
    }

    @Nullable
    private TypeDefinition<?> createTypeDefinition(@NotNull Map<String, Object> type) {
        String kind = assertNotNull((String) type.get("kind"), () -> String.format("null object kind: %s", type.toString()));
        String name = (String) type.get("name");

        if (name.startsWith("__")) return null;
        switch (kind) {
            case "INTERFACE":
                return createInterface(type);
            case "OBJECT":
                return createObject(type);
            case "UNION":
                return createUnion(type);
            case "ENUM":
                return createEnum(type);
            case "INPUT_OBJECT":
                return createInputObject(type);
            case "SCALAR":
                return createScalar(type);
            default:
                return assertShouldNeverHappen("unexpected kind %s", kind);
        }
    }

    @Nullable
    private static TypeDefinition<?> createScalar(@NotNull Map<String, Object> input) {
        String name = (String) input.get("name");
        if (ScalarInfo.isGraphqlSpecifiedScalar(name)) {
            return null;
        }
        return ScalarTypeDefinition.newScalarTypeDefinition().name(name).description(getDescription(input)).build();
    }


    @SuppressWarnings("unchecked")
    @NotNull
    static UnionTypeDefinition createUnion(@NotNull Map<String, Object> input) {
        assertTrue(Objects.equals(input.get("kind"), "UNION"), () -> "wrong input");

        final List<Map<String, Object>> possibleTypes = (List<Map<String, Object>>) input.get("possibleTypes");
        final List<Type> memberTypes = Lists.newArrayList();
        if (possibleTypes != null) {
            for (Map<String, Object> possibleType : possibleTypes) {
                if (possibleType == null) continue;
                TypeName typeName = new TypeName((String) possibleType.get("name"));
                memberTypes.add(typeName);
            }
        }

        return UnionTypeDefinition.newUnionTypeDefinition()
            .name((String) input.get("name"))
            .description(getDescription(input))
            .memberTypes(memberTypes)
            .build();
    }

    @SuppressWarnings("unchecked")
    @NotNull
    static EnumTypeDefinition createEnum(@NotNull Map<String, Object> input) {
        assertTrue(Objects.equals(input.get("kind"), "ENUM"), () -> "wrong input");

        final List<Map<String, Object>> enumValues = (List<Map<String, Object>>) input.get("enumValues");
        final List<EnumValueDefinition> enumValueDefinitions = Lists.newArrayList();
        if (enumValues != null) {
            for (Map<String, Object> enumValue : enumValues) {
                if (enumValue == null) continue;

                EnumValueDefinition enumValueDefinition = EnumValueDefinition.newEnumValueDefinition()
                    .name((String) enumValue.get("name"))
                    .description(getDescription(enumValue))
                    .directives(createDeprecatedDirective(enumValue))
                    .build();

                enumValueDefinitions.add(enumValueDefinition);
            }
        }

        return EnumTypeDefinition.newEnumTypeDefinition()
            .name((String) input.get("name"))
            .description(getDescription(input))
            .enumValueDefinitions(enumValueDefinitions)
            .build();
    }

    @SuppressWarnings("unchecked")
    @NotNull
    InterfaceTypeDefinition createInterface(@NotNull Map<String, Object> input) {
        assertTrue(Objects.equals(input.get("kind"), "INTERFACE"), () -> "wrong input");

        final List<Map<String, Object>> fields = (List<Map<String, Object>>) input.get("fields");

        return InterfaceTypeDefinition.newInterfaceTypeDefinition()
            .name((String) input.get("name"))
            .description(getDescription(input))
            .definitions(createFields(fields))
            .build();

    }

    @SuppressWarnings("unchecked")
    @NotNull
    InputObjectTypeDefinition createInputObject(@NotNull Map<String, Object> input) {
        assertTrue(Objects.equals(input.get("kind"), "INPUT_OBJECT"), () -> "wrong input");
        final List<Map<String, Object>> fields = (List<Map<String, Object>>) input.get("inputFields");

        return InputObjectTypeDefinition.newInputObjectDefinition()
            .name((String) input.get("name"))
            .description(getDescription(input))
            .inputValueDefinitions(createInputValueDefinitions(fields))
            .build();
    }

    @SuppressWarnings("unchecked")
    @NotNull
    ObjectTypeDefinition createObject(@NotNull Map<String, Object> input) {
        assertTrue(Objects.equals(input.get("kind"), "OBJECT"), () -> "wrong input");

        ObjectTypeDefinition.Builder builder = ObjectTypeDefinition.newObjectTypeDefinition()
            .name((String) input.get("name"))
            .description(getDescription(input));
        if (input.containsKey("interfaces")) {
            builder.implementz(
                ((List<Map<String, Object>>) input.get("interfaces")).stream()
                    .map(GraphQLIntrospectionResultToSchema::createTypeIndirection)
                    .collect(Collectors.toList())
            );
        }
        List<Map<String, Object>> fields = (List<Map<String, Object>>) input.get("fields");

        builder.fieldDefinitions(createFields(fields));

        return builder.build();
    }

    @NotNull
    private List<FieldDefinition> createFields(@Nullable List<Map<String, Object>> fields) {
        if (fields == null) return ContainerUtil.emptyList();

        List<FieldDefinition> result = new ArrayList<>();
        for (Map<String, Object> field : fields) {
            if (field == null) continue;

            List<Map<String, Object>> args = (List<Map<String, Object>>) field.get("args");
            List<InputValueDefinition> inputValueDefinitions = createInputValueDefinitions(args);
            FieldDefinition fieldDefinition = FieldDefinition.newFieldDefinition()
                .name((String) field.get("name"))
                .description(getDescription(field))
                .type(createTypeIndirection((Map<String, Object>) field.get("type")))
                .inputValueDefinitions(inputValueDefinitions)
                .directives(createDeprecatedDirective(field))
                .build();
            result.add(fieldDefinition);
        }
        return result;
    }

    @NotNull
    private static List<Directive> createDeprecatedDirective(@NotNull Map<String, Object> field) {
        if ((Boolean) field.get("isDeprecated")) {
            String reason = (String) field.get("deprecationReason");
            if (reason == null) {
                reason = "No longer supported"; // default according to spec
            }
            Argument reasonArg = new Argument("reason", new StringValue(reason));
            return Collections.singletonList(new Directive("deprecated", Collections.singletonList(reasonArg)));
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private static List<InputValueDefinition> createInputValueDefinitions(@Nullable List<Map<String, Object>> args) {
        if (args == null) return ContainerUtil.emptyList();

        List<InputValueDefinition> result = new ArrayList<>();
        for (Map<String, Object> arg : args) {
            if (arg == null) continue;

            Type argType = createTypeIndirection((Map<String, Object>) arg.get("type"));
            String valueLiteral = (String) arg.get("defaultValue");
            Value defaultValue = valueLiteral != null ? AstValueHelper.valueFromAst(valueLiteral) : null;
            InputValueDefinition inputValueDefinition = InputValueDefinition.newInputValueDefinition()
                .name((String) arg.get("name"))
                .type(argType)
                .description(getDescription(arg))
                .defaultValue(defaultValue)
                .build();
            result.add(inputValueDefinition);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static Type createTypeIndirection(@Nullable Map<String, Object> type) {
        if (type == null) return null;

        String kind = (String) type.get("kind");
        switch (kind) {
            case "INTERFACE":
            case "OBJECT":
            case "UNION":
            case "ENUM":
            case "INPUT_OBJECT":
            case "SCALAR":
                return TypeName.newTypeName().name((String) type.get("name")).build();
            case "NON_NULL":
                Type ofType = createTypeIndirection((Map<String, Object>) type.get("ofType"));
                if (ofType == null) return null;
                return NonNullType.newNonNullType().type(ofType).build();
            case "LIST":
                return ListType.newListType().type(createTypeIndirection((Map<String, Object>) type.get("ofType"))).build();
            default:
                return assertShouldNeverHappen("Unknown kind %s", kind);
        }
    }

    @Nullable
    private static Description getDescription(@Nullable Map<String, Object> descriptionAware) {
        if (descriptionAware == null) return null;

        final Object rawDescription = descriptionAware.get("description");
        if (rawDescription instanceof String) {
            String description = (String) rawDescription;
            if (!description.trim().isEmpty()) {
                final boolean multiLine = description.contains("\n");
                if (multiLine) {
                    // ensures the description stands on separate lines from the triple quotes
                    description = "\n" + description.trim() + "\n";
                }
                return new Description(description, new SourceLocation(1, 1), multiLine);
            }
        }
        return null;
    }

}
