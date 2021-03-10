package com.intellij.lang.jsgraphql.schema;


import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlParser;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.lang.jsgraphql.types.Assert.assertShouldNeverHappen;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.emptyList;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.map;
import static com.intellij.lang.jsgraphql.types.parser.StringValueParsing.parseSingleQuotedString;
import static com.intellij.lang.jsgraphql.types.parser.StringValueParsing.parseTripleQuotedString;

public class GraphQLPsiToLanguage {

    public @NotNull Document createDocument(@NotNull GraphQLFile file) {
        Document.Builder document = Document.newDocument();
        addCommonData(document, file);
        document.definitions(map(file.getDefinitions(), this::createDefinition));
        return document.build();
    }

    protected @NotNull Definition createDefinition(@NotNull GraphQLDefinition definition) {
        if (definition instanceof GraphQLOperationDefinition) {
            return createOperationDefinition(((GraphQLOperationDefinition) definition));
        } else if (definition instanceof GraphQLFragmentDefinition) {
            return createFragmentDefinition(((GraphQLFragmentDefinition) definition));
        } else if (definition instanceof GraphQLTypeExtension) {
            return createTypeSystemExtension(((GraphQLTypeExtension) definition));
        } else if (definition instanceof GraphQLTypeSystemDefinition) {
            return createTypeSystemDefinition(((GraphQLTypeSystemDefinition) definition));
        } else {
            return assertShouldNeverHappen();
        }
    }

    protected @NotNull OperationDefinition createOperationDefinition(@NotNull GraphQLOperationDefinition definition) {
        OperationDefinition.Builder operationDefinition = OperationDefinition.newOperationDefinition();
        addCommonData(operationDefinition, definition);
        if (definition instanceof GraphQLSelectionSetOperationDefinition) {
            operationDefinition.operation(OperationDefinition.Operation.QUERY);
            final GraphQLSelectionSetOperationDefinition selectionSetOperation = (GraphQLSelectionSetOperationDefinition) definition;
            operationDefinition.selectionSet(createSelectionSet(selectionSetOperation.getSelectionSet()));
        } else if (definition instanceof GraphQLTypedOperationDefinition) {
            final GraphQLTypedOperationDefinition typedOperation = (GraphQLTypedOperationDefinition) definition;
            operationDefinition.operation(parseOperation(typedOperation));
            operationDefinition.name(typedOperation.getName());

            operationDefinition.variableDefinitions(createVariableDefinitions(typedOperation.getVariableDefinitions()));
            operationDefinition.directives(createDirectives(typedOperation.getDirectives()));
            operationDefinition.selectionSet(createSelectionSet(typedOperation.getSelectionSet()));
        } else {
            assertShouldNeverHappen();
        }

        return operationDefinition.build();
    }

    protected @NotNull OperationDefinition.Operation parseOperation(@NotNull GraphQLTypedOperationDefinition operation) {
        switch (operation.getOperationType().getText()) {
            case "query":
                return OperationDefinition.Operation.QUERY;
            case "mutation":
                return OperationDefinition.Operation.MUTATION;
            case "subscription":
                return OperationDefinition.Operation.SUBSCRIPTION;
            default:
                return assertShouldNeverHappen("InternalError: unknown operationTypeContext=%s", operation.getText());
        }
    }

    protected FragmentSpread createFragmentSpread(GraphqlParser.FragmentSpreadContext ctx) {
        FragmentSpread.Builder fragmentSpread = FragmentSpread.newFragmentSpread().name(ctx.fragmentName().getText());
        addCommonData(fragmentSpread, ctx);
        fragmentSpread.directives(createDirectives(ctx.directives()));
        return fragmentSpread.build();
    }

    protected @NotNull List<VariableDefinition> createVariableDefinitions(@Nullable GraphQLVariableDefinitions definitions) {
        if (definitions == null) {
            return emptyList();
        }
        return map(definitions.getVariableDefinitions(), this::createVariableDefinition);
    }

    protected @NotNull VariableDefinition createVariableDefinition(@NotNull GraphQLVariableDefinition definition) {
        VariableDefinition.Builder variableDefinition = VariableDefinition.newVariableDefinition();
        addCommonData(variableDefinition, definition);
        variableDefinition.name(definition.getVariable().getName());
        final GraphQLDefaultValue defaultValue = definition.getDefaultValue();
        if (defaultValue != null) {
            variableDefinition.defaultValue(createValue(defaultValue.getValue()));
        }
        variableDefinition.type(createType(definition.getType()));
        variableDefinition.directives(createDirectives(definition.directives()));
        return variableDefinition.build();

    }

    protected @NotNull FragmentDefinition createFragmentDefinition(@NotNull GraphQLFragmentDefinition definition) {
        FragmentDefinition.Builder fragmentDefinition = FragmentDefinition.newFragmentDefinition();
        addCommonData(fragmentDefinition, definition);
        fragmentDefinition.name(definition.fragmentName().getText());
        fragmentDefinition.typeCondition(TypeName.newTypeName().name(definition.typeCondition().typeName().getText()).build());
        fragmentDefinition.directives(createDirectives(definition.directives()));
        fragmentDefinition.selectionSet(createSelectionSet(definition.selectionSet()));
        return fragmentDefinition.build();
    }


    protected SelectionSet createSelectionSet(@Nullable GraphQLSelectionSet selectionSet) {
        if (selectionSet == null) {
            return null;
        }
        SelectionSet.Builder builder = SelectionSet.newSelectionSet();
        addCommonData(builder, selectionSet);
        List<Selection> selections = map(selectionSet.selection(), selectionContext -> {
            if (selectionContext.field() != null) {
                return createField(selectionContext.field());
            }
            if (selectionContext.fragmentSpread() != null) {
                return createFragmentSpread(selectionContext.fragmentSpread());
            }
            if (selectionContext.inlineFragment() != null) {
                return createInlineFragment(selectionContext.inlineFragment());
            }
            return (Selection) Assert.assertShouldNeverHappen();

        });
        builder.selections(selections);
        return builder.build();
    }


    protected Field createField(GraphqlParser.FieldContext ctx) {
        Field.Builder builder = Field.newField();
        addCommonData(builder, ctx);
        builder.name(ctx.name().getText());
        if (ctx.alias() != null) {
            builder.alias(ctx.alias().name().getText());
        }

        builder.directives(createDirectives(ctx.directives()));
        builder.arguments(createArguments(ctx.arguments()));
        builder.selectionSet(createSelectionSet(ctx.selectionSet()));
        return builder.build();
    }


    protected InlineFragment createInlineFragment(GraphqlParser.InlineFragmentContext ctx) {
        InlineFragment.Builder inlineFragment = InlineFragment.newInlineFragment();
        addCommonData(inlineFragment, ctx);
        if (ctx.typeCondition() != null) {
            inlineFragment.typeCondition(createTypeName(ctx.typeCondition().typeName()));
        }
        inlineFragment.directives(createDirectives(ctx.directives()));
        inlineFragment.selectionSet(createSelectionSet(ctx.selectionSet()));
        return inlineFragment.build();
    }

    protected SDLDefinition createTypeSystemDefinition(GraphQLTypeSystemDefinition definition) {
        if (definition.schemaDefinition() != null) {
            return createSchemaDefinition(definition.schemaDefinition());
        } else if (definition.directiveDefinition() != null) {
            return createDirectiveDefinition(definition.directiveDefinition());
        } else if (definition.typeDefinition() != null) {
            return createTypeDefinition(definition.typeDefinition());
        } else {
            return assertShouldNeverHappen();
        }
    }

    protected SDLDefinition createTypeSystemExtension(GraphQLTypeExtension extension) {
        if (extension.typeExtension() != null) {
            return createTypeExtension(extension.typeExtension());
        } else if (extension.schemaExtension() != null) {
            return creationSchemaExtension(extension.schemaExtension());
        } else {
            return assertShouldNeverHappen();
        }
    }

    protected TypeDefinition createTypeExtension(GraphqlParser.TypeExtensionContext ctx) {
        if (ctx.enumTypeExtensionDefinition() != null) {
            return createEnumTypeExtensionDefinition(ctx.enumTypeExtensionDefinition());

        } else if (ctx.objectTypeExtensionDefinition() != null) {
            return createObjectTypeExtensionDefinition(ctx.objectTypeExtensionDefinition());

        } else if (ctx.inputObjectTypeExtensionDefinition() != null) {
            return createInputObjectTypeExtensionDefinition(ctx.inputObjectTypeExtensionDefinition());

        } else if (ctx.interfaceTypeExtensionDefinition() != null) {
            return createInterfaceTypeExtensionDefinition(ctx.interfaceTypeExtensionDefinition());

        } else if (ctx.scalarTypeExtensionDefinition() != null) {
            return createScalarTypeExtensionDefinition(ctx.scalarTypeExtensionDefinition());

        } else if (ctx.unionTypeExtensionDefinition() != null) {
            return createUnionTypeExtensionDefinition(ctx.unionTypeExtensionDefinition());
        } else {
            return assertShouldNeverHappen();
        }
    }

    protected TypeDefinition createTypeDefinition(GraphqlParser.TypeDefinitionContext ctx) {
        if (ctx.enumTypeDefinition() != null) {
            return createEnumTypeDefinition(ctx.enumTypeDefinition());

        } else if (ctx.objectTypeDefinition() != null) {
            return createObjectTypeDefinition(ctx.objectTypeDefinition());

        } else if (ctx.inputObjectTypeDefinition() != null) {
            return createInputObjectTypeDefinition(ctx.inputObjectTypeDefinition());

        } else if (ctx.interfaceTypeDefinition() != null) {
            return createInterfaceTypeDefinition(ctx.interfaceTypeDefinition());

        } else if (ctx.scalarTypeDefinition() != null) {
            return createScalarTypeDefinition(ctx.scalarTypeDefinition());

        } else if (ctx.unionTypeDefinition() != null) {
            return createUnionTypeDefinition(ctx.unionTypeDefinition());

        } else {
            return assertShouldNeverHappen();
        }
    }


    protected @NotNull Type createType(@NotNull GraphQLType type) {
        if (type instanceof GraphQLTypeName) {
            return createTypeName(((GraphQLTypeName) type));
        } else if (type instanceof GraphQLNonNullType) {
            return createNonNullType(((GraphQLNonNullType) type));
        } else if (type.listType() != null) {
            return createListType(type.listType());
        } else {
            return assertShouldNeverHappen();
        }
    }

    protected @NotNull TypeName createTypeName(@NotNull GraphQLTypeName typeName) {
        TypeName.Builder builder = TypeName.newTypeName();
        builder.name(typeName.getName());
        addCommonData(builder, typeName);
        return builder.build();
    }

    protected @NotNull NonNullType createNonNullType(@NotNull GraphQLNonNullType nonNullType) {
        NonNullType.Builder builder = NonNullType.newNonNullType();
        addCommonData(builder, nonNullType);
        if (nonNullType.listType() != null) {
            builder.type(createListType(nonNullType.listType()));
        } else if (nonNullType.typeName() != null) {
            builder.type(createTypeName(nonNullType.typeName()));
        } else {
            return assertShouldNeverHappen();
        }
        return builder.build();
    }

    protected ListType createListType(GraphqlParser.ListTypeContext ctx) {
        ListType.Builder builder = ListType.newListType();
        addCommonData(builder, ctx);
        builder.type(createType(ctx.type()));
        return builder.build();
    }

    protected @NotNull Argument createArgument(@NotNull GraphQLArgument argument) {
        Argument.Builder builder = Argument.newArgument();
        addCommonData(builder, argument);
        builder.name(argument.getName());
        builder.value(createValue(argument.getValue()));
        return builder.build();
    }

    protected @NotNull List<Argument> createArguments(@Nullable GraphQLArguments arguments) {
        if (arguments == null) {
            return emptyList();
        }
        return map(arguments.getArgumentList(), this::createArgument);
    }


    protected List<Directive> createDirectives(@NotNull List<GraphQLDirective> directives) {
        return map(directives, this::createDirective);
    }

    protected @NotNull Directive createDirective(@NotNull GraphQLDirective directive) {
        Directive.Builder builder = Directive.newDirective();
        builder.name(directive.getName());
        addCommonData(builder, directive);
        builder.arguments(createArguments(directive.getArguments()));
        return builder.build();
    }

    protected SchemaDefinition createSchemaDefinition(GraphqlParser.SchemaDefinitionContext ctx) {
        SchemaDefinition.Builder def = SchemaDefinition.newSchemaDefinition();
        addCommonData(def, ctx);
        def.directives(createDirectives(ctx.directives()));
        def.description(newDescription(ctx.description()));
        def.operationTypeDefinitions(map(ctx.operationTypeDefinition(), this::createOperationTypeDefinition));
        return def.build();
    }

    private SDLDefinition creationSchemaExtension(GraphqlParser.SchemaExtensionContext ctx) {
        SchemaExtensionDefinition.Builder def = SchemaExtensionDefinition.newSchemaExtensionDefinition();
        addCommonData(def, ctx);

        List<Directive> directives = new ArrayList<>();
        List<GraphqlParser.DirectivesContext> directivesCtx = ctx.directives();
        for (GraphqlParser.DirectivesContext directiveCtx : directivesCtx) {
            directives.addAll(createDirectives(directiveCtx));
        }
        def.directives(directives);

        List<OperationTypeDefinition> operationTypeDefs = map(ctx.operationTypeDefinition(), this::createOperationTypeDefinition);
        def.operationTypeDefinitions(operationTypeDefs);
        return def.build();
    }


    protected OperationTypeDefinition createOperationTypeDefinition(GraphqlParser.OperationTypeDefinitionContext ctx) {
        OperationTypeDefinition.Builder def = OperationTypeDefinition.newOperationTypeDefinition();
        def.name(ctx.operationType().getText());
        def.typeName(createTypeName(ctx.typeName()));
        addCommonData(def, ctx);
        return def.build();
    }

    protected ScalarTypeDefinition createScalarTypeDefinition(GraphqlParser.ScalarTypeDefinitionContext ctx) {
        ScalarTypeDefinition.Builder def = ScalarTypeDefinition.newScalarTypeDefinition();
        def.name(ctx.name().getText());
        addCommonData(def, ctx);
        def.description(newDescription(ctx.description()));
        def.directives(createDirectives(ctx.directives()));
        return def.build();
    }

    protected ScalarTypeExtensionDefinition createScalarTypeExtensionDefinition(GraphqlParser.ScalarTypeExtensionDefinitionContext ctx) {
        ScalarTypeExtensionDefinition.Builder def = ScalarTypeExtensionDefinition.newScalarTypeExtensionDefinition();
        def.name(ctx.name().getText());
        addCommonData(def, ctx);
        def.directives(createDirectives(ctx.directives()));
        return def.build();
    }

    protected ObjectTypeDefinition createObjectTypeDefinition(GraphqlParser.ObjectTypeDefinitionContext ctx) {
        ObjectTypeDefinition.Builder def = ObjectTypeDefinition.newObjectTypeDefinition();
        def.name(ctx.name().getText());
        addCommonData(def, ctx);
        def.description(newDescription(ctx.description()));
        def.directives(createDirectives(ctx.directives()));
        GraphqlParser.ImplementsInterfacesContext implementsInterfacesContext = ctx.implementsInterfaces();
        List<Type> implementz = getImplementz(implementsInterfacesContext);
        def.implementz(implementz);
        if (ctx.fieldsDefinition() != null) {
            def.fieldDefinitions(createFieldDefinitions(ctx.fieldsDefinition()));
        }
        return def.build();
    }

    protected ObjectTypeExtensionDefinition createObjectTypeExtensionDefinition(GraphqlParser.ObjectTypeExtensionDefinitionContext ctx) {
        ObjectTypeExtensionDefinition.Builder def = ObjectTypeExtensionDefinition.newObjectTypeExtensionDefinition();
        def.name(ctx.name().getText());
        addCommonData(def, ctx);
        def.directives(createDirectives(ctx.directives()));
        GraphqlParser.ImplementsInterfacesContext implementsInterfacesContext = ctx.implementsInterfaces();
        List<Type> implementz = getImplementz(implementsInterfacesContext);
        def.implementz(implementz);
        if (ctx.extensionFieldsDefinition() != null) {
            def.fieldDefinitions(createFieldDefinitions(ctx.extensionFieldsDefinition()));
        }
        return def.build();
    }

    protected List<FieldDefinition> createFieldDefinitions(GraphqlParser.FieldsDefinitionContext ctx) {
        if (ctx == null) {
            return emptyList();
        }
        return map(ctx.fieldDefinition(), this::createFieldDefinition);
    }

    protected List<FieldDefinition> createFieldDefinitions(GraphqlParser.ExtensionFieldsDefinitionContext ctx) {
        if (ctx == null) {
            return emptyList();
        }
        return map(ctx.fieldDefinition(), this::createFieldDefinition);
    }


    protected FieldDefinition createFieldDefinition(GraphqlParser.FieldDefinitionContext ctx) {
        FieldDefinition.Builder def = FieldDefinition.newFieldDefinition();
        def.name(ctx.name().getText());
        def.type(createType(ctx.type()));
        addCommonData(def, ctx);
        def.description(newDescription(ctx.description()));
        def.directives(createDirectives(ctx.directives()));
        if (ctx.argumentsDefinition() != null) {
            def.inputValueDefinitions(createInputValueDefinitions(ctx.argumentsDefinition().inputValueDefinition()));
        }
        return def.build();
    }

    protected List<InputValueDefinition> createInputValueDefinitions(List<GraphqlParser.InputValueDefinitionContext> defs) {
        return map(defs, this::createInputValueDefinition);
    }

    protected InputValueDefinition createInputValueDefinition(GraphqlParser.InputValueDefinitionContext ctx) {
        InputValueDefinition.Builder def = InputValueDefinition.newInputValueDefinition();
        def.name(ctx.name().getText());
        def.type(createType(ctx.type()));
        addCommonData(def, ctx);
        def.description(newDescription(ctx.description()));
        if (ctx.defaultValue() != null) {
            def.defaultValue(createValue(ctx.defaultValue().value()));
        }
        def.directives(createDirectives(ctx.directives()));
        return def.build();
    }

    protected InterfaceTypeDefinition createInterfaceTypeDefinition(GraphqlParser.InterfaceTypeDefinitionContext ctx) {
        InterfaceTypeDefinition.Builder def = InterfaceTypeDefinition.newInterfaceTypeDefinition();
        def.name(ctx.name().getText());
        addCommonData(def, ctx);
        def.description(newDescription(ctx.description()));
        def.directives(createDirectives(ctx.directives()));
        GraphqlParser.ImplementsInterfacesContext implementsInterfacesContext = ctx.implementsInterfaces();
        List<Type> implementz = getImplementz(implementsInterfacesContext);
        def.implementz(implementz);
        def.definitions(createFieldDefinitions(ctx.fieldsDefinition()));
        return def.build();
    }

    protected InterfaceTypeExtensionDefinition createInterfaceTypeExtensionDefinition(GraphqlParser.InterfaceTypeExtensionDefinitionContext ctx) {
        InterfaceTypeExtensionDefinition.Builder def = InterfaceTypeExtensionDefinition.newInterfaceTypeExtensionDefinition();
        def.name(ctx.name().getText());
        addCommonData(def, ctx);
        def.directives(createDirectives(ctx.directives()));
        GraphqlParser.ImplementsInterfacesContext implementsInterfacesContext = ctx.implementsInterfaces();
        List<Type> implementz = getImplementz(implementsInterfacesContext);
        def.implementz(implementz);
        def.definitions(createFieldDefinitions(ctx.extensionFieldsDefinition()));
        return def.build();
    }

    protected UnionTypeDefinition createUnionTypeDefinition(GraphqlParser.UnionTypeDefinitionContext ctx) {
        UnionTypeDefinition.Builder def = UnionTypeDefinition.newUnionTypeDefinition();
        def.name(ctx.name().getText());
        addCommonData(def, ctx);
        def.description(newDescription(ctx.description()));
        def.directives(createDirectives(ctx.directives()));
        List<Type> members = new ArrayList<>();
        GraphqlParser.UnionMembershipContext unionMembership = ctx.unionMembership();
        if (unionMembership != null) {
            GraphqlParser.UnionMembersContext unionMembersContext = unionMembership.unionMembers();
            while (unionMembersContext != null) {
                members.add(0, createTypeName(unionMembersContext.typeName()));
                unionMembersContext = unionMembersContext.unionMembers();
            }
        }
        def.memberTypes(members);
        return def.build();
    }

    protected UnionTypeExtensionDefinition createUnionTypeExtensionDefinition(GraphqlParser.UnionTypeExtensionDefinitionContext ctx) {
        UnionTypeExtensionDefinition.Builder def = UnionTypeExtensionDefinition.newUnionTypeExtensionDefinition();
        def.name(ctx.name().getText());
        addCommonData(def, ctx);
        def.directives(createDirectives(ctx.directives()));
        List<Type> members = new ArrayList<>();
        if (ctx.unionMembership() != null) {
            GraphqlParser.UnionMembersContext unionMembersContext = ctx.unionMembership().unionMembers();
            while (unionMembersContext != null) {
                members.add(0, createTypeName(unionMembersContext.typeName()));
                unionMembersContext = unionMembersContext.unionMembers();
            }
            def.memberTypes(members);
        }
        return def.build();
    }

    protected EnumTypeDefinition createEnumTypeDefinition(GraphqlParser.EnumTypeDefinitionContext ctx) {
        EnumTypeDefinition.Builder def = EnumTypeDefinition.newEnumTypeDefinition();
        def.name(ctx.name().getText());
        addCommonData(def, ctx);
        def.description(newDescription(ctx.description()));
        def.directives(createDirectives(ctx.directives()));
        if (ctx.enumValueDefinitions() != null) {
            def.enumValueDefinitions(
                map(ctx.enumValueDefinitions().enumValueDefinition(), this::createEnumValueDefinition));
        }
        return def.build();
    }

    protected EnumTypeExtensionDefinition createEnumTypeExtensionDefinition(GraphqlParser.EnumTypeExtensionDefinitionContext ctx) {
        EnumTypeExtensionDefinition.Builder def = EnumTypeExtensionDefinition.newEnumTypeExtensionDefinition();
        def.name(ctx.name().getText());
        addCommonData(def, ctx);
        def.directives(createDirectives(ctx.directives()));
        if (ctx.extensionEnumValueDefinitions() != null) {
            def.enumValueDefinitions(
                map(ctx.extensionEnumValueDefinitions().enumValueDefinition(), this::createEnumValueDefinition));
        }
        return def.build();
    }

    protected EnumValueDefinition createEnumValueDefinition(GraphqlParser.EnumValueDefinitionContext ctx) {
        EnumValueDefinition.Builder def = EnumValueDefinition.newEnumValueDefinition();
        def.name(ctx.enumValue().getText());
        addCommonData(def, ctx);
        def.description(newDescription(ctx.description()));
        def.directives(createDirectives(ctx.directives()));
        return def.build();
    }

    protected InputObjectTypeDefinition createInputObjectTypeDefinition(GraphqlParser.InputObjectTypeDefinitionContext ctx) {
        InputObjectTypeDefinition.Builder def = InputObjectTypeDefinition.newInputObjectDefinition();
        def.name(ctx.name().getText());
        addCommonData(def, ctx);
        def.description(newDescription(ctx.description()));
        def.directives(createDirectives(ctx.directives()));
        if (ctx.inputObjectValueDefinitions() != null) {
            def.inputValueDefinitions(createInputValueDefinitions(ctx.inputObjectValueDefinitions().inputValueDefinition()));
        }
        return def.build();
    }

    protected InputObjectTypeExtensionDefinition createInputObjectTypeExtensionDefinition(GraphqlParser.InputObjectTypeExtensionDefinitionContext ctx) {
        InputObjectTypeExtensionDefinition.Builder def = InputObjectTypeExtensionDefinition.newInputObjectTypeExtensionDefinition();
        def.name(ctx.name().getText());
        addCommonData(def, ctx);
        def.directives(createDirectives(ctx.directives()));
        if (ctx.extensionInputObjectValueDefinitions() != null) {
            def.inputValueDefinitions(createInputValueDefinitions(ctx.extensionInputObjectValueDefinitions().inputValueDefinition()));
        }
        return def.build();
    }

    protected DirectiveDefinition createDirectiveDefinition(GraphqlParser.DirectiveDefinitionContext ctx) {
        DirectiveDefinition.Builder def = DirectiveDefinition.newDirectiveDefinition();
        def.name(ctx.name().getText());
        addCommonData(def, ctx);
        def.description(newDescription(ctx.description()));

        def.repeatable(ctx.REPEATABLE() != null);

        GraphqlParser.DirectiveLocationsContext directiveLocationsContext = ctx.directiveLocations();
        List<DirectiveLocation> directiveLocations = new ArrayList<>();
        while (directiveLocationsContext != null) {
            directiveLocations.add(0, createDirectiveLocation(directiveLocationsContext.directiveLocation()));
            directiveLocationsContext = directiveLocationsContext.directiveLocations();
        }
        def.directiveLocations(directiveLocations);
        if (ctx.argumentsDefinition() != null) {
            def.inputValueDefinitions(createInputValueDefinitions(ctx.argumentsDefinition().inputValueDefinition()));
        }
        return def.build();
    }

    protected DirectiveLocation createDirectiveLocation(GraphqlParser.DirectiveLocationContext ctx) {
        DirectiveLocation.Builder def = DirectiveLocation.newDirectiveLocation();
        def.name(ctx.name().getText());
        addCommonData(def, ctx);
        return def.build();
    }

    protected @Nullable Value createValue(@Nullable GraphQLValue value) {
        if (value == null) return null;

        if (value instanceof GraphQLIntValue) {
            IntValue.Builder intValue = IntValue.newIntValue().value(new BigInteger(value.getText()));
            addCommonData(intValue, value);
            return intValue.build();
        } else if (value instanceof GraphQLFloatValue) {
            FloatValue.Builder floatValue = FloatValue.newFloatValue().value(new BigDecimal(value.getText()));
            addCommonData(floatValue, value);
            return floatValue.build();
        } else if (value instanceof GraphQLBooleanValue) {
            BooleanValue.Builder booleanValue = BooleanValue.newBooleanValue().value(Boolean.parseBoolean(value.getText()));
            addCommonData(booleanValue, value);
            return booleanValue.build();
        } else if (value instanceof GraphQLNullValue) {
            NullValue.Builder nullValue = NullValue.newNullValue();
            addCommonData(nullValue, value);
            return nullValue.build();
        } else if (value instanceof GraphQLStringValue) {
            StringValue.Builder stringValue = StringValue.newStringValue().value(quotedString(((GraphQLStringValue) value).getQuotedString()));
            addCommonData(stringValue, value);
            return stringValue.build();
        } else if (value instanceof GraphQLEnumValue) {
            EnumValue.Builder enumValue = EnumValue.newEnumValue().name(((GraphQLEnumValue) value).getName());
            addCommonData(enumValue, value);
            return enumValue.build();
        } else if (value instanceof GraphQLArrayValue) {
            ArrayValue.Builder arrayValue = ArrayValue.newArrayValue();
            addCommonData(arrayValue, value);
            List<Value> values = new ArrayList<>();
            for (GraphQLValue arrayValueItem : ((GraphQLArrayValue) value).getValueList()) {
                values.add(createValue(arrayValueItem));
            }
            return arrayValue.values(values).build();
        } else if (value instanceof GraphQLObjectValue) {
            ObjectValue.Builder objectValue = ObjectValue.newObjectValue();
            addCommonData(objectValue, value);
            List<ObjectField> objectFields = new ArrayList<>();
            for (GraphQLObjectField field : ((GraphQLObjectValue) value).getObjectFieldList()) {
                ObjectField objectField = ObjectField.newObjectField()
                    .name(field.getName())
                    .value(createValue(field.getValue()))
                    .build();
                objectFields.add(objectField);
            }
            return objectValue.objectFields(objectFields).build();
        } else if (value instanceof GraphQLVariable) {
            VariableReference.Builder variableReference = VariableReference.newVariableReference().name(((GraphQLVariable) value).getName());
            addCommonData(variableReference, value);
            return variableReference.build();
        }
        return assertShouldNeverHappen();
    }

    protected Value createValue(GraphqlParser.ValueContext ctx) {
        if (ctx.IntValue() != null) {
            IntValue.Builder intValue = IntValue.newIntValue().value(new BigInteger(ctx.IntValue().getText()));
            addCommonData(intValue, ctx);
            return intValue.build();
        } else if (ctx.FloatValue() != null) {
            FloatValue.Builder floatValue = FloatValue.newFloatValue().value(new BigDecimal(ctx.FloatValue().getText()));
            addCommonData(floatValue, ctx);
            return floatValue.build();
        } else if (ctx.BooleanValue() != null) {
            BooleanValue.Builder booleanValue = BooleanValue.newBooleanValue().value(Boolean.parseBoolean(ctx.BooleanValue().getText()));
            addCommonData(booleanValue, ctx);
            return booleanValue.build();
        } else if (ctx.NullValue() != null) {
            NullValue.Builder nullValue = NullValue.newNullValue();
            addCommonData(nullValue, ctx);
            return nullValue.build();
        } else if (ctx.StringValue() != null) {
            StringValue.Builder stringValue = StringValue.newStringValue().value(quotedString(ctx.StringValue()));
            addCommonData(stringValue, ctx);
            return stringValue.build();
        } else if (ctx.enumValue() != null) {
            EnumValue.Builder enumValue = EnumValue.newEnumValue().name(ctx.enumValue().getText());
            addCommonData(enumValue, ctx);
            return enumValue.build();
        } else if (ctx.arrayValue() != null) {
            ArrayValue.Builder arrayValue = ArrayValue.newArrayValue();
            addCommonData(arrayValue, ctx);
            List<Value> values = new ArrayList<>();
            for (GraphqlParser.ValueContext valueContext : ctx.arrayValue().value()) {
                values.add(createValue(valueContext));
            }
            return arrayValue.values(values).build();
        } else if (ctx.objectValue() != null) {
            ObjectValue.Builder objectValue = ObjectValue.newObjectValue();
            addCommonData(objectValue, ctx);
            List<ObjectField> objectFields = new ArrayList<>();
            for (GraphqlParser.ObjectFieldContext objectFieldContext :
                ctx.objectValue().objectField()) {
                ObjectField objectField = ObjectField.newObjectField()
                    .name(objectFieldContext.name().getText())
                    .value(createValue(objectFieldContext.value()))
                    .build();
                objectFields.add(objectField);
            }
            return objectValue.objectFields(objectFields).build();
        }
        return assertShouldNeverHappen();
    }

    static @NotNull String quotedString(@NotNull GraphQLQuotedString quotedString) {
        final String text = quotedString.getText();
        boolean multiLine = text.startsWith("\"\"\"");
        if (multiLine) {
            return parseTripleQuotedString(text);
        } else {
            return parseSingleQuotedString(text);
        }
    }

    protected void addCommonData(NodeBuilder nodeBuilder, PsiElement element) {
        nodeBuilder.sourceLocation(getSourceLocation(element));
    }

    protected Description newDescription(GraphqlParser.DescriptionContext descriptionCtx) {
        if (descriptionCtx == null) {
            return null;
        }
        TerminalNode terminalNode = descriptionCtx.StringValue();
        if (terminalNode == null) {
            return null;
        }
        String content = terminalNode.getText();
        boolean multiLine = content.startsWith("\"\"\"");
        if (multiLine) {
            content = parseTripleQuotedString(content);
        } else {
            content = parseSingleQuotedString(content);
        }
        SourceLocation sourceLocation = getSourceLocation(descriptionCtx);
        return new Description(content, sourceLocation, multiLine);
    }

    protected SourceLocation getSourceLocation(PsiElement element) {
        final InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(element.getProject());
        PsiFile file = injectedLanguageManager.getTopLevelFile(element);
        if (file == null) return new SourceLocation(1, 1, "Unknown");
        com.intellij.openapi.editor.Document document = PsiDocumentManager.getInstance(element.getProject()).getDocument(file);
        if (document == null) return new SourceLocation(1, 1, "Unknown");
        int offset = injectedLanguageManager.injectedToHost(element, element.getTextOffset());
        int lineNumber = document.getLineNumber(offset);
        int column = offset - document.getLineStartOffset(lineNumber);
        return new SourceLocation(lineNumber + 1, column + 1);
    }

    private List<Type> getImplementz(GraphqlParser.ImplementsInterfacesContext implementsInterfacesContext) {
        List<Type> implementz = new ArrayList<>();
        while (implementsInterfacesContext != null) {
            List<TypeName> typeNames = map(implementsInterfacesContext.typeName(), this::createTypeName);

            implementz.addAll(0, typeNames);
            implementsInterfacesContext = implementsInterfacesContext.implementsInterfaces();
        }
        return implementz;
    }
}

