package com.intellij.lang.jsgraphql.ide.editor;

import com.google.common.collect.Lists;
import graphql.PublicApi;
import graphql.language.Argument;
import graphql.language.AstValueHelper;
import graphql.language.Description;
import graphql.language.Directive;
import graphql.language.Document;
import graphql.language.EnumTypeDefinition;
import graphql.language.EnumValueDefinition;
import graphql.language.FieldDefinition;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.InterfaceTypeDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.OperationTypeDefinition;
import graphql.language.ScalarTypeDefinition;
import graphql.language.SchemaDefinition;
import graphql.language.SourceLocation;
import graphql.language.StringValue;
import graphql.language.Type;
import graphql.language.TypeDefinition;
import graphql.language.TypeName;
import graphql.language.UnionTypeDefinition;
import graphql.language.Value;
import graphql.schema.idl.ScalarInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static graphql.Assert.assertNotNull;
import static graphql.Assert.assertShouldNeverHappen;
import static graphql.Assert.assertTrue;

@SuppressWarnings("unchecked")
@PublicApi
public class GraphQLIntrospectionResultToSchema {

    /**
     * Returns a IDL Document that represents the schema as defined by the introspection result map
     *
     * @param introspectionResult the result of an introspection query on a schema
     *
     * @return a IDL Document of the schema
     */
    @SuppressWarnings("unchecked")
    public Document createSchemaDefinition(Map<String, Object> introspectionResult) {
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
        for (Map<String, Object> type : types) {
            TypeDefinition typeDefinition = createTypeDefinition(type);
            if (typeDefinition == null) continue;
            document.definition(typeDefinition);
        }

        return document.build();
    }

    private TypeDefinition createTypeDefinition(Map<String, Object> type) {
        String kind = (String) type.get("kind");
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

    private TypeDefinition createScalar(Map<String, Object> input) {
        String name = (String) input.get("name");
        if (ScalarInfo.isGraphqlSpecifiedScalar(name)) {
            return null;
        }
        return ScalarTypeDefinition.newScalarTypeDefinition().name(name).description(getDescription(input)).build();
    }


    @SuppressWarnings("unchecked")
    UnionTypeDefinition createUnion(Map<String, Object> input) {
        assertTrue(input.get("kind").equals("UNION"), () -> "wrong input");

        final List<Map<String, Object>> possibleTypes = (List<Map<String, Object>>) input.get("possibleTypes");
        final List<Type> memberTypes = Lists.newArrayList();
        for (Map<String, Object> possibleType : possibleTypes) {
            TypeName typeName = new TypeName((String) possibleType.get("name"));
            memberTypes.add(typeName);
        }

        return UnionTypeDefinition.newUnionTypeDefinition()
                .name((String) input.get("name"))
                .description(getDescription(input))
                .memberTypes(memberTypes)
                .build();
    }

    @SuppressWarnings("unchecked")
    EnumTypeDefinition createEnum(Map<String, Object> input) {
        assertTrue(input.get("kind").equals("ENUM"), () -> "wrong input");

        final List<Map<String, Object>> enumValues = (List<Map<String, Object>>) input.get("enumValues");
        final List<EnumValueDefinition> enumValueDefinitions = Lists.newArrayList();
        for (Map<String, Object> enumValue : enumValues) {

            EnumValueDefinition enumValueDefinition = EnumValueDefinition.newEnumValueDefinition()
                    .name((String) enumValue.get("name"))
                    .description(getDescription(enumValue))
                    .directives(createDeprecatedDirective(enumValue))
                    .build();

            enumValueDefinitions.add(enumValueDefinition);
        }

        return EnumTypeDefinition.newEnumTypeDefinition()
                .name((String) input.get("name"))
                .description(getDescription(input))
                .enumValueDefinitions(enumValueDefinitions)
                .build();
    }

    @SuppressWarnings("unchecked")
    InterfaceTypeDefinition createInterface(Map<String, Object> input) {
        assertTrue(input.get("kind").equals("INTERFACE"), () -> "wrong input");

        final List<Map<String, Object>> fields = (List<Map<String, Object>>) input.get("fields");
        final InterfaceTypeDefinition interfaceTypeDefinition = InterfaceTypeDefinition.newInterfaceTypeDefinition()
                .name((String) input.get("name"))
                .description(getDescription(input))
                .definitions(createFields(fields))
                .build();

        return interfaceTypeDefinition;

    }

    @SuppressWarnings("unchecked")
    InputObjectTypeDefinition createInputObject(Map<String, Object> input) {
        assertTrue(input.get("kind").equals("INPUT_OBJECT"), () -> "wrong input");
        final List<Map<String, Object>> fields = (List<Map<String, Object>>) input.get("inputFields");
        final InputObjectTypeDefinition inputObjectTypeDefinition = InputObjectTypeDefinition.newInputObjectDefinition()
                .name((String) input.get("name"))
                .description(getDescription(input))
                .inputValueDefinitions(createInputValueDefinitions(fields))
                .build();

        return inputObjectTypeDefinition;
    }

    @SuppressWarnings("unchecked")
    ObjectTypeDefinition createObject(Map<String, Object> input) {
        assertTrue(input.get("kind").equals("OBJECT"), () -> "wrong input");

        ObjectTypeDefinition.Builder builder = ObjectTypeDefinition.newObjectTypeDefinition()
                .name((String) input.get("name"))
                .description(getDescription(input));
        if (input.containsKey("interfaces")) {
            builder.implementz(
                    ((List<Map<String, Object>>) input.get("interfaces")).stream()
                            .map(this::createTypeIndirection)
                            .collect(Collectors.toList())
            );
        }
        List<Map<String, Object>> fields = (List<Map<String, Object>>) input.get("fields");

        builder.fieldDefinitions(createFields(fields));

        return builder.build();
    }

    private List<FieldDefinition> createFields(List<Map<String, Object>> fields) {
        List<FieldDefinition> result = new ArrayList<>();
        for (Map<String, Object> field : fields) {
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

    private List<Directive> createDeprecatedDirective(Map<String, Object> field) {
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
    private List<InputValueDefinition> createInputValueDefinitions(List<Map<String, Object>> args) {
        List<InputValueDefinition> result = new ArrayList<>();
        for (Map<String, Object> arg : args) {
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
    private Type createTypeIndirection(Map<String, Object> type) {
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
                return NonNullType.newNonNullType().type(createTypeIndirection((Map<String, Object>) type.get("ofType"))).build();
            case "LIST":
                return ListType.newListType().type(createTypeIndirection((Map<String, Object>) type.get("ofType"))).build();
            default:
                return assertShouldNeverHappen("Unknown kind %s", kind);
        }
    }

    private Description getDescription(Map<String, Object> descriptionAware) {
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
