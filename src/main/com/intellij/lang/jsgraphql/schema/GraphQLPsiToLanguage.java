package com.intellij.lang.jsgraphql.schema;


import com.intellij.lang.jsgraphql.ide.project.GraphQLInjectionSearchHelper;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.intellij.lang.jsgraphql.types.Assert.assertShouldNeverHappen;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.emptyList;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.mapNotNull;
import static com.intellij.lang.jsgraphql.types.parser.StringValueParsing.parseSingleQuotedString;
import static com.intellij.lang.jsgraphql.types.parser.StringValueParsing.parseTripleQuotedString;

@SuppressWarnings("rawtypes")
public class GraphQLPsiToLanguage {
    public static final GraphQLPsiToLanguage INSTANCE = new GraphQLPsiToLanguage();

    public @NotNull Document createDocument(@NotNull GraphQLFile file) {
        Document.Builder document = Document.newDocument();
        addCommonData(document, file);
        document.definitions(mapNotNull(file.getDefinitions(), this::createDefinition));
        return document.build();
    }

    protected @Nullable Definition createDefinition(@NotNull GraphQLDefinition definition) {
        if (definition instanceof GraphQLOperationDefinition) {
            return createOperationDefinition(((GraphQLOperationDefinition) definition));
        } else if (definition instanceof GraphQLFragmentDefinition) {
            return createFragmentDefinition(((GraphQLFragmentDefinition) definition));
        } else if (definition instanceof GraphQLTypeExtension) {
            return createTypeExtension(((GraphQLTypeExtension) definition));
        } /*else if (definition instanceof GraphQLSchemaExtension) {
            TODO: [intellij] schema extension
        } */ else if (definition instanceof GraphQLTypeSystemDefinition) {
            return createTypeSystemDefinition(((GraphQLTypeSystemDefinition) definition));
        } else if (definition instanceof GraphQLTemplateDefinition) {
            return null;
        } else {
            return assertShouldNeverHappen();
        }
    }

    protected @NotNull OperationDefinition createOperationDefinition(@NotNull GraphQLOperationDefinition definition) {
        OperationDefinition.Builder operationDefinition = OperationDefinition.newOperationDefinition();
        addCommonData(operationDefinition, definition);
        if (definition instanceof GraphQLSelectionSetOperationDefinition) {
            operationDefinition.operation(OperationDefinition.Operation.QUERY);
            GraphQLSelectionSetOperationDefinition selectionSetOperation = (GraphQLSelectionSetOperationDefinition) definition;
            operationDefinition.selectionSet(createSelectionSet(selectionSetOperation.getSelectionSet()));
        } else if (definition instanceof GraphQLTypedOperationDefinition) {
            GraphQLTypedOperationDefinition typedOperation = (GraphQLTypedOperationDefinition) definition;
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

    protected @Nullable FragmentSpread createFragmentSpread(@NotNull GraphQLFragmentSpread fragment) {
        FragmentSpread.Builder fragmentSpread = FragmentSpread.newFragmentSpread().name(fragment.getName());
        addCommonData(fragmentSpread, fragment);
        fragmentSpread.directives(createDirectives(fragment.getDirectives()));
        return checkNode(fragmentSpread.build());
    }

    protected @NotNull List<VariableDefinition> createVariableDefinitions(@Nullable GraphQLVariableDefinitions definitions) {
        if (definitions == null) {
            return emptyList();
        }
        return mapNotNull(definitions.getVariableDefinitions(), this::createVariableDefinition);
    }

    protected @Nullable VariableDefinition createVariableDefinition(@NotNull GraphQLVariableDefinition definition) {
        VariableDefinition.Builder variableDefinition = VariableDefinition.newVariableDefinition();
        addCommonData(variableDefinition, definition);
        variableDefinition.name(definition.getVariable().getName());
        GraphQLDefaultValue defaultValue = definition.getDefaultValue();
        if (defaultValue != null) {
            variableDefinition.defaultValue(createValue(defaultValue.getValue()));
        }
        variableDefinition.type(createType(definition.getType()));
        variableDefinition.directives(createDirectives(definition.getDirectives()));
        return checkNode(variableDefinition.build());

    }

    protected @Nullable FragmentDefinition createFragmentDefinition(@NotNull GraphQLFragmentDefinition definition) {
        FragmentDefinition.Builder fragmentDefinition = FragmentDefinition.newFragmentDefinition();
        addCommonData(fragmentDefinition, definition);
        fragmentDefinition.name(definition.getName());

        GraphQLTypeCondition typeCondition = definition.getTypeCondition();
        if (typeCondition != null) {
            GraphQLTypeName typeName = typeCondition.getTypeName();
            if (typeName != null) {
                fragmentDefinition.typeCondition(checkNode(
                    TypeName.newTypeName().name(typeName.getName()).build()));
            }
        }

        fragmentDefinition.directives(createDirectives(definition.getDirectives()));
        fragmentDefinition.selectionSet(createSelectionSet(definition.getSelectionSet()));
        return checkNode(fragmentDefinition.build());
    }


    protected @Nullable SelectionSet createSelectionSet(@Nullable GraphQLSelectionSet selectionSet) {
        if (selectionSet == null) {
            return null;
        }
        SelectionSet.Builder builder = SelectionSet.newSelectionSet();
        addCommonData(builder, selectionSet);
        List<Selection> selections = mapNotNull(selectionSet.getSelectionList(), selection -> {
            GraphQLTemplateSelection templateSelection = selection.getTemplateSelection();
            // ignore templates
            if (templateSelection != null) return null;

            GraphQLField field = selection.getField();
            if (field != null) {
                return createField(field);
            }

            GraphQLFragmentSelection fragmentSelection = selection.getFragmentSelection();
            if (fragmentSelection != null) {
                GraphQLFragmentSpread fragmentSpread = fragmentSelection.getFragmentSpread();
                if (fragmentSpread != null) {
                    return createFragmentSpread(fragmentSpread);
                }

                GraphQLInlineFragment inlineFragment = fragmentSelection.getInlineFragment();
                if (inlineFragment != null) {
                    return createInlineFragment(inlineFragment);
                }
            }
            return assertShouldNeverHappen();

        });
        builder.selections(selections);
        return builder.build();
    }


    protected @Nullable Field createField(@NotNull GraphQLField field) {
        Field.Builder builder = Field.newField();
        addCommonData(builder, field);
        builder.name(field.getName());
        GraphQLAlias alias = field.getAlias();
        if (alias != null) {
            builder.alias(alias.getIdentifier().getText());
        }

        builder.directives(createDirectives(field.getDirectives()));
        builder.arguments(createArguments(field.getArguments()));
        builder.selectionSet(createSelectionSet(field.getSelectionSet()));
        return checkNode(builder.build());
    }


    protected @NotNull InlineFragment createInlineFragment(@NotNull GraphQLInlineFragment fragment) {
        InlineFragment.Builder inlineFragment = InlineFragment.newInlineFragment();
        addCommonData(inlineFragment, fragment);
        GraphQLTypeCondition typeCondition = fragment.getTypeCondition();
        if (typeCondition != null) {
            GraphQLTypeName typeName = typeCondition.getTypeName();
            if (typeName != null) {
                inlineFragment.typeCondition(createTypeName(typeName));
            }
        }
        inlineFragment.directives(createDirectives(fragment.getDirectives()));
        inlineFragment.selectionSet(createSelectionSet(fragment.getSelectionSet()));
        return inlineFragment.build();
    }

    protected @Nullable SDLDefinition createTypeSystemDefinition(@NotNull GraphQLTypeSystemDefinition definition) {
        if (definition instanceof GraphQLSchemaDefinition) {
            return createSchemaDefinition((GraphQLSchemaDefinition) definition);
        } else if (definition instanceof GraphQLDirectiveDefinition) {
            return createDirectiveDefinition((GraphQLDirectiveDefinition) definition);
        } else if (definition instanceof GraphQLTypeDefinition) {
            return createTypeDefinition((GraphQLTypeDefinition) definition);
        } else {
            return assertShouldNeverHappen();
        }
    }

    protected @Nullable TypeDefinition createTypeExtension(@NotNull GraphQLTypeExtension extension) {
        if (extension instanceof GraphQLEnumTypeExtensionDefinition) {
            return createEnumTypeExtensionDefinition(((GraphQLEnumTypeExtensionDefinition) extension));
        } else if (extension instanceof GraphQLObjectTypeExtensionDefinition) {
            return createObjectTypeExtensionDefinition(((GraphQLObjectTypeExtensionDefinition) extension));
        } else if (extension instanceof GraphQLInputObjectTypeExtensionDefinition) {
            return createInputObjectTypeExtensionDefinition(((GraphQLInputObjectTypeExtensionDefinition) extension));
        } else if (extension instanceof GraphQLInterfaceTypeExtensionDefinition) {
            return createInterfaceTypeExtensionDefinition(((GraphQLInterfaceTypeExtensionDefinition) extension));
        } else if (extension instanceof GraphQLScalarTypeExtensionDefinition) {
            return createScalarTypeExtensionDefinition(((GraphQLScalarTypeExtensionDefinition) extension));
        } else if (extension instanceof GraphQLUnionTypeExtensionDefinition) {
            return createUnionTypeExtensionDefinition(((GraphQLUnionTypeExtensionDefinition) extension));
        } else {
            return assertShouldNeverHappen();
        }
    }

    protected @Nullable TypeDefinition createTypeDefinition(@NotNull GraphQLTypeDefinition definition) {
        if (definition instanceof GraphQLEnumTypeDefinition) {
            return createEnumTypeDefinition(((GraphQLEnumTypeDefinition) definition));

        } else if (definition instanceof GraphQLObjectTypeDefinition) {
            return createObjectTypeDefinition(((GraphQLObjectTypeDefinition) definition));

        } else if (definition instanceof GraphQLInputObjectTypeDefinition) {
            return createInputObjectTypeDefinition(((GraphQLInputObjectTypeDefinition) definition));

        } else if (definition instanceof GraphQLInterfaceTypeDefinition) {
            return createInterfaceTypeDefinition(((GraphQLInterfaceTypeDefinition) definition));

        } else if (definition instanceof GraphQLScalarTypeDefinition) {
            return createScalarTypeDefinition(((GraphQLScalarTypeDefinition) definition));

        } else if (definition instanceof GraphQLUnionTypeDefinition) {
            return createUnionTypeDefinition(((GraphQLUnionTypeDefinition) definition));

        } else {
            return assertShouldNeverHappen();
        }
    }


    protected @Nullable Type createType(@Nullable GraphQLType type) {
        if (type == null) return null;

        if (type instanceof GraphQLTypeName) {
            return createTypeName(((GraphQLTypeName) type));
        } else if (type instanceof GraphQLNonNullType) {
            return createNonNullType(((GraphQLNonNullType) type));
        } else if (type instanceof GraphQLListType) {
            return createListType(((GraphQLListType) type));
        } else {
            return assertShouldNeverHappen();
        }
    }

    protected @Nullable TypeName createTypeName(@Nullable GraphQLTypeName typeName) {
        if (typeName == null) return null;
        TypeName.Builder builder = TypeName.newTypeName();
        builder.name(typeName.getName());
        addCommonData(builder, typeName);
        return checkNode(builder.build());
    }

    protected @Nullable NonNullType createNonNullType(@NotNull GraphQLNonNullType nonNullType) {
        NonNullType.Builder builder = NonNullType.newNonNullType();
        addCommonData(builder, nonNullType);
        GraphQLType type = nonNullType.getType();
        if (type instanceof GraphQLListType) {
            builder.type(createListType((GraphQLListType) type));
        } else if (type instanceof GraphQLTypeName) {
            builder.type(createTypeName((GraphQLTypeName) type));
        } else {
            return null;
        }
        return checkNode(builder.build());
    }

    protected @NotNull ListType createListType(@NotNull GraphQLListType listType) {
        ListType.Builder builder = ListType.newListType();
        addCommonData(builder, listType);
        builder.type(createType(listType.getType()));
        return checkNode(builder.build());
    }

    protected @Nullable Argument createArgument(@NotNull GraphQLArgument argument) {
        Argument.Builder builder = Argument.newArgument();
        addCommonData(builder, argument);
        builder.name(argument.getName());
        builder.value(createValue(argument.getValue()));
        return checkNode(builder.build());
    }

    protected @NotNull List<Argument> createArguments(@Nullable GraphQLArguments arguments) {
        if (arguments == null) {
            return emptyList();
        }
        return mapNotNull(arguments.getArgumentList(), this::createArgument);
    }


    protected @NotNull List<Directive> createDirectives(@NotNull List<GraphQLDirective> directives) {
        return mapNotNull(directives, this::createDirective);
    }

    protected @Nullable Directive createDirective(@NotNull GraphQLDirective directive) {
        Directive.Builder builder = Directive.newDirective();
        builder.name(directive.getName());
        addCommonData(builder, directive);
        builder.arguments(createArguments(directive.getArguments()));
        return checkNode(builder.build());
    }

    protected @NotNull SchemaDefinition createSchemaDefinition(@NotNull GraphQLSchemaDefinition schemaDefinition) {
        SchemaDefinition.Builder def = SchemaDefinition.newSchemaDefinition();
        addCommonData(def, schemaDefinition);
        def.directives(createDirectives(schemaDefinition.getDirectives()));
        // TODO: [intellij] schema description
//        def.description(newDescription(schemaDefinition.description()));
        GraphQLOperationTypeDefinitions operationTypeDefinitions = schemaDefinition.getOperationTypeDefinitions();
        if (operationTypeDefinitions != null) {
            def.operationTypeDefinitions(mapNotNull(operationTypeDefinitions.getOperationTypeDefinitionList(), this::createOperationTypeDefinition));
        }
        return def.build();
    }

    // TODO: [intellij] schema extension
//    private SDLDefinition creationSchemaExtension(GraphqlParser.SchemaExtensionContext ctx) {
//        SchemaExtensionDefinition.Builder def = SchemaExtensionDefinition.newSchemaExtensionDefinition();
//        addCommonData(def, ctx);
//
//        List<Directive> directives = new ArrayList<>();
//        List<GraphqlParser.DirectivesContext> directivesCtx = ctx.directives();
//        for (GraphqlParser.DirectivesContext directiveCtx : directivesCtx) {
//            directives.addAll(createDirectives(directiveCtx));
//        }
//        def.directives(directives);
//
//        List<OperationTypeDefinition> operationTypeDefs = mapNotNull(ctx.operationTypeDefinition(), this::createOperationTypeDefinition);
//        def.operationTypeDefinitions(operationTypeDefs);
//        return def.build();
//    }


    protected @Nullable OperationTypeDefinition createOperationTypeDefinition(@NotNull GraphQLOperationTypeDefinition definition) {
        OperationTypeDefinition.Builder def = OperationTypeDefinition.newOperationTypeDefinition();
        GraphQLOperationType operationType = definition.getOperationType();
        if (operationType != null) {
            def.name(operationType.getText());
        }
        def.typeName(createTypeName(definition.getTypeName()));
        addCommonData(def, definition);
        return checkNode(def.build());
    }

    protected @Nullable ScalarTypeDefinition createScalarTypeDefinition(@NotNull GraphQLScalarTypeDefinition typeDefinition) {
        ScalarTypeDefinition.Builder def = ScalarTypeDefinition.newScalarTypeDefinition();
        GraphQLTypeNameDefinition typeNameDefinition = typeDefinition.getTypeNameDefinition();
        if (typeNameDefinition != null) {
            def.name(typeNameDefinition.getName());
        }
        addCommonData(def, typeDefinition);
        def.description(newDescription(typeDefinition.getDescription()));
        def.directives(createDirectives(typeDefinition.getDirectives()));
        return checkNode(def.build());
    }

    protected @Nullable ScalarTypeExtensionDefinition createScalarTypeExtensionDefinition(@NotNull GraphQLScalarTypeExtensionDefinition extensionDefinition) {
        ScalarTypeExtensionDefinition.Builder def = ScalarTypeExtensionDefinition.newScalarTypeExtensionDefinition();
        GraphQLTypeName typeName = extensionDefinition.getTypeName();
        if (typeName != null) {
            def.name(typeName.getName());
        }
        addCommonData(def, extensionDefinition);
        def.directives(createDirectives(extensionDefinition.getDirectives()));
        return checkNode(def.build());
    }

    protected @Nullable ObjectTypeDefinition createObjectTypeDefinition(@NotNull GraphQLObjectTypeDefinition typeDefinition) {
        ObjectTypeDefinition.Builder def = ObjectTypeDefinition.newObjectTypeDefinition();
        GraphQLTypeNameDefinition typeNameDefinition = typeDefinition.getTypeNameDefinition();
        if (typeNameDefinition != null) {
            def.name(typeNameDefinition.getName());
        }
        addCommonData(def, typeDefinition);
        def.description(newDescription(typeDefinition.getDescription()));
        def.directives(createDirectives(typeDefinition.getDirectives()));
        def.implementz(getImplements(typeDefinition.getImplementsInterfaces()));
        def.fieldDefinitions(createFieldDefinitions(typeDefinition.getFieldsDefinition()));
        return checkNode(def.build());
    }

    protected @Nullable ObjectTypeExtensionDefinition createObjectTypeExtensionDefinition(@NotNull GraphQLObjectTypeExtensionDefinition extensionDefinition) {
        ObjectTypeExtensionDefinition.Builder def = ObjectTypeExtensionDefinition.newObjectTypeExtensionDefinition();
        GraphQLTypeName typeName = extensionDefinition.getTypeName();
        if (typeName != null) {
            def.name(typeName.getName());
        }
        addCommonData(def, extensionDefinition);
        def.directives(createDirectives(extensionDefinition.getDirectives()));
        GraphQLImplementsInterfaces implementsInterfacesContext = extensionDefinition.getImplementsInterfaces();
        def.implementz(getImplements(implementsInterfacesContext));
        GraphQLFieldsDefinition fieldsDefinition = extensionDefinition.getFieldsDefinition();
        if (fieldsDefinition != null) {
            def.fieldDefinitions(createFieldDefinitions(fieldsDefinition));
        }
        return checkNode(def.build());
    }

    protected @NotNull List<FieldDefinition> createFieldDefinitions(@Nullable GraphQLFieldsDefinition fieldsDefinition) {
        if (fieldsDefinition == null) {
            return emptyList();
        }
        return mapNotNull(fieldsDefinition.getFieldDefinitionList(), this::createFieldDefinition);
    }

    protected @Nullable FieldDefinition createFieldDefinition(@NotNull GraphQLFieldDefinition fieldDefinition) {
        FieldDefinition.Builder def = FieldDefinition.newFieldDefinition();
        def.name(fieldDefinition.getName());
        def.type(createType(fieldDefinition.getType()));
        addCommonData(def, fieldDefinition);
        def.description(newDescription(fieldDefinition.getDescription()));
        def.directives(createDirectives(fieldDefinition.getDirectives()));
        GraphQLArgumentsDefinition argumentsDefinition = fieldDefinition.getArgumentsDefinition();
        if (argumentsDefinition != null) {
            def.inputValueDefinitions(createInputValueDefinitions(argumentsDefinition.getInputValueDefinitionList()));
        }
        return checkNode(def.build());
    }

    protected @NotNull List<InputValueDefinition> createInputValueDefinitions(@NotNull List<GraphQLInputValueDefinition> defs) {
        return mapNotNull(defs, this::createInputValueDefinition);
    }

    protected @Nullable InputValueDefinition createInputValueDefinition(@NotNull GraphQLInputValueDefinition valueDefinition) {
        InputValueDefinition.Builder def = InputValueDefinition.newInputValueDefinition();
        def.name(valueDefinition.getName());
        def.type(createType(valueDefinition.getType()));
        addCommonData(def, valueDefinition);
        def.description(newDescription(valueDefinition.getDescription()));
        GraphQLDefaultValue defaultValue = valueDefinition.getDefaultValue();
        if (defaultValue != null) {
            def.defaultValue(createValue(defaultValue.getValue()));
        }
        def.directives(createDirectives(valueDefinition.getDirectives()));
        return checkNode(def.build());
    }

    protected @Nullable InterfaceTypeDefinition createInterfaceTypeDefinition(@NotNull GraphQLInterfaceTypeDefinition typeDefinition) {
        InterfaceTypeDefinition.Builder def = InterfaceTypeDefinition.newInterfaceTypeDefinition();
        GraphQLTypeNameDefinition typeNameDefinition = typeDefinition.getTypeNameDefinition();
        if (typeNameDefinition != null) {
            def.name(typeNameDefinition.getName());
        }
        addCommonData(def, typeDefinition);
        def.description(newDescription(typeDefinition.getDescription()));
        def.directives(createDirectives(typeDefinition.getDirectives()));
        GraphQLImplementsInterfaces implementsInterfacesContext = typeDefinition.getImplementsInterfaces();
        def.implementz(getImplements(implementsInterfacesContext));
        def.definitions(createFieldDefinitions(typeDefinition.getFieldsDefinition()));
        return checkNode(def.build());
    }

    protected @Nullable InterfaceTypeExtensionDefinition createInterfaceTypeExtensionDefinition(@NotNull GraphQLInterfaceTypeExtensionDefinition extensionDefinition) {
        InterfaceTypeExtensionDefinition.Builder def = InterfaceTypeExtensionDefinition.newInterfaceTypeExtensionDefinition();
        GraphQLTypeName typeName = extensionDefinition.getTypeName();
        if (typeName != null) {
            def.name(typeName.getName());
        }
        addCommonData(def, extensionDefinition);
        def.directives(createDirectives(extensionDefinition.getDirectives()));
        GraphQLImplementsInterfaces implementsInterfacesContext = extensionDefinition.getImplementsInterfaces();
        def.implementz(getImplements(implementsInterfacesContext));
        def.definitions(createFieldDefinitions(extensionDefinition.getFieldsDefinition()));
        return checkNode(def.build());
    }

    protected @Nullable UnionTypeDefinition createUnionTypeDefinition(@NotNull GraphQLUnionTypeDefinition typeDefinition) {
        UnionTypeDefinition.Builder def = UnionTypeDefinition.newUnionTypeDefinition();
        GraphQLTypeNameDefinition typeNameDefinition = typeDefinition.getTypeNameDefinition();
        if (typeNameDefinition != null) {
            def.name(typeNameDefinition.getName());
        }
        addCommonData(def, typeDefinition);
        def.description(newDescription(typeDefinition.getDescription()));
        def.directives(createDirectives(typeDefinition.getDirectives()));
        List<Type> members = new ArrayList<>();
        GraphQLUnionMembership unionMembership = typeDefinition.getUnionMembership();
        if (unionMembership != null) {
            GraphQLUnionMembers unionMembers = unionMembership.getUnionMembers();
            if (unionMembers != null) {
                for (GraphQLTypeName typeName : unionMembers.getTypeNameList()) {
                    TypeName newTypeName = createTypeName(typeName);
                    if (newTypeName != null) {
                        members.add(newTypeName);
                    }
                }
            }
        }
        def.memberTypes(members);
        return checkNode(def.build());
    }

    protected @Nullable UnionTypeExtensionDefinition createUnionTypeExtensionDefinition(@NotNull GraphQLUnionTypeExtensionDefinition extensionDefinition) {
        UnionTypeExtensionDefinition.Builder def = UnionTypeExtensionDefinition.newUnionTypeExtensionDefinition();
        GraphQLTypeName typeName = extensionDefinition.getTypeName();
        if (typeName != null) {
            def.name(typeName.getName());
        }
        addCommonData(def, extensionDefinition);
        def.directives(createDirectives(extensionDefinition.getDirectives()));
        List<Type> members = new ArrayList<>();
        GraphQLUnionMembership unionMembership = extensionDefinition.getUnionMembership();
        if (unionMembership != null) {
            GraphQLUnionMembers unionMembers = unionMembership.getUnionMembers();
            if (unionMembers != null) {
                for (GraphQLTypeName name : unionMembers.getTypeNameList()) {
                    TypeName newTypeName = createTypeName(name);
                    if (newTypeName != null) {
                        members.add(newTypeName);
                    }
                }
            }
            def.memberTypes(members);
        }
        return checkNode(def.build());
    }

    protected @Nullable EnumTypeDefinition createEnumTypeDefinition(@NotNull GraphQLEnumTypeDefinition enumTypeDefinition) {
        EnumTypeDefinition.Builder def = EnumTypeDefinition.newEnumTypeDefinition();
        GraphQLTypeNameDefinition typeNameDefinition = enumTypeDefinition.getTypeNameDefinition();
        if (typeNameDefinition != null) {
            def.name(typeNameDefinition.getName());
        }
        addCommonData(def, enumTypeDefinition);
        def.description(newDescription(enumTypeDefinition.getDescription()));
        def.directives(createDirectives(enumTypeDefinition.getDirectives()));
        GraphQLEnumValueDefinitions enumValueDefinitions = enumTypeDefinition.getEnumValueDefinitions();
        if (enumValueDefinitions != null) {
            def.enumValueDefinitions(
                mapNotNull(enumValueDefinitions.getEnumValueDefinitionList(), this::createEnumValueDefinition));
        }
        return checkNode(def.build());
    }

    protected @Nullable EnumTypeExtensionDefinition createEnumTypeExtensionDefinition(@NotNull GraphQLEnumTypeExtensionDefinition extensionDefinition) {
        EnumTypeExtensionDefinition.Builder def = EnumTypeExtensionDefinition.newEnumTypeExtensionDefinition();
        GraphQLTypeName typeName = extensionDefinition.getTypeName();
        if (typeName != null) {
            def.name(typeName.getName());
        }
        addCommonData(def, extensionDefinition);
        def.directives(createDirectives(extensionDefinition.getDirectives()));
        GraphQLEnumValueDefinitions enumValueDefinitions = extensionDefinition.getEnumValueDefinitions();
        if (enumValueDefinitions != null) {
            def.enumValueDefinitions(mapNotNull(enumValueDefinitions.getEnumValueDefinitionList(), this::createEnumValueDefinition));
        }
        return checkNode(def.build());
    }

    protected @Nullable EnumValueDefinition createEnumValueDefinition(@NotNull GraphQLEnumValueDefinition valueDefinition) {
        EnumValueDefinition.Builder def = EnumValueDefinition.newEnumValueDefinition();
        def.name(valueDefinition.getEnumValue().getName());
        addCommonData(def, valueDefinition);
        def.description(newDescription(valueDefinition.getDescription()));
        def.directives(createDirectives(valueDefinition.getDirectives()));
        return checkNode(def.build());
    }

    protected @Nullable InputObjectTypeDefinition createInputObjectTypeDefinition(@NotNull GraphQLInputObjectTypeDefinition typeDefinition) {
        InputObjectTypeDefinition.Builder def = InputObjectTypeDefinition.newInputObjectDefinition();
        GraphQLTypeNameDefinition typeNameDefinition = typeDefinition.getTypeNameDefinition();
        if (typeNameDefinition != null) {
            def.name(typeNameDefinition.getName());
        }
        addCommonData(def, typeDefinition);
        def.description(newDescription(typeDefinition.getDescription()));
        def.directives(createDirectives(typeDefinition.getDirectives()));
        GraphQLInputObjectValueDefinitions valueDefinitions = typeDefinition.getInputObjectValueDefinitions();
        if (valueDefinitions != null) {
            def.inputValueDefinitions(createInputValueDefinitions(valueDefinitions.getInputValueDefinitionList()));
        }
        return checkNode(def.build());
    }

    protected @Nullable InputObjectTypeExtensionDefinition createInputObjectTypeExtensionDefinition(@NotNull GraphQLInputObjectTypeExtensionDefinition extensionDefinition) {
        InputObjectTypeExtensionDefinition.Builder def = InputObjectTypeExtensionDefinition.newInputObjectTypeExtensionDefinition();
        GraphQLTypeName typeName = extensionDefinition.getTypeName();
        if (typeName != null) {
            def.name(typeName.getName());
        }
        addCommonData(def, extensionDefinition);
        def.directives(createDirectives(extensionDefinition.getDirectives()));
        GraphQLInputObjectValueDefinitions valueDefinitions = extensionDefinition.getInputObjectValueDefinitions();
        if (valueDefinitions != null) {
            def.inputValueDefinitions(createInputValueDefinitions(valueDefinitions.getInputValueDefinitionList()));
        }
        return checkNode(def.build());
    }

    protected @Nullable DirectiveDefinition createDirectiveDefinition(@NotNull GraphQLDirectiveDefinition directiveDefinition) {
        DirectiveDefinition.Builder def = DirectiveDefinition.newDirectiveDefinition();
        GraphQLIdentifier nameIdentifier = directiveDefinition.getNameIdentifier();
        if (nameIdentifier != null) {
            def.name(nameIdentifier.getText());
        }
        addCommonData(def, directiveDefinition);
        def.description(newDescription(directiveDefinition.getDescription()));
        def.repeatable(directiveDefinition.getRepeatable() != null);

        GraphQLDirectiveLocations directiveLocationsContext = directiveDefinition.getDirectiveLocations();
        List<DirectiveLocation> directiveLocations = new ArrayList<>();
        if (directiveLocationsContext != null) {
            List<GraphQLDirectiveLocation> directiveLocationList = directiveLocationsContext.getDirectiveLocationList();
            for (GraphQLDirectiveLocation directiveLocation : directiveLocationList) {
                DirectiveLocation location = createDirectiveLocation(directiveLocation);
                if (location != null) {
                    directiveLocations.add(location);
                }
            }
        }
        def.directiveLocations(directiveLocations);

        GraphQLArgumentsDefinition argumentsDefinition = directiveDefinition.getArgumentsDefinition();
        if (argumentsDefinition != null) {
            def.inputValueDefinitions(createInputValueDefinitions(argumentsDefinition.getInputValueDefinitionList()));
        }
        return checkNode(def.build());
    }

    protected @Nullable DirectiveLocation createDirectiveLocation(@NotNull GraphQLDirectiveLocation directiveLocation) {
        DirectiveLocation.Builder def = DirectiveLocation.newDirectiveLocation();
        def.name(directiveLocation.getText());
        addCommonData(def, directiveLocation);
        return checkNode(def.build());
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
            return checkNode(enumValue.build());
        } else if (value instanceof GraphQLArrayValue) {
            ArrayValue.Builder arrayValue = ArrayValue.newArrayValue();
            addCommonData(arrayValue, value);
            List<Value> values = new ArrayList<>();
            for (GraphQLValue arrayValueItem : ((GraphQLArrayValue) value).getValueList()) {
                Value newValue = createValue(arrayValueItem);
                if (newValue != null) {
                    values.add(newValue);
                }
            }
            return arrayValue.values(values).build();
        } else if (value instanceof GraphQLObjectValue) {
            ObjectValue.Builder objectValue = ObjectValue.newObjectValue();
            addCommonData(objectValue, value);
            List<ObjectField> objectFields = new ArrayList<>();
            for (GraphQLObjectField field : ((GraphQLObjectValue) value).getObjectFieldList()) {
                ObjectField objectField = checkNode(ObjectField.newObjectField()
                    .name(field.getName())
                    .value(createValue(field.getValue()))
                    .build());
                if (objectField != null) {
                    objectFields.add(objectField);
                }
            }
            return objectValue.objectFields(objectFields).build();
        } else if (value instanceof GraphQLVariable) {
            VariableReference.Builder variableReference = VariableReference.newVariableReference().name(((GraphQLVariable) value).getName());
            addCommonData(variableReference, value);
            return checkNode(variableReference.build());
        } else if (value instanceof GraphQLTemplateVariable) {
            return null;
        }
        return assertShouldNeverHappen();
    }

    static @NotNull String quotedString(@NotNull GraphQLQuotedString quotedString) {
        String text = quotedString.getText();
        boolean multiLine = text.startsWith("\"\"\"");
        if (multiLine) {
            return parseTripleQuotedString(text);
        } else {
            return parseSingleQuotedString(text);
        }
    }

    protected void addCommonData(NodeBuilder nodeBuilder, @NotNull PsiElement element) {
        nodeBuilder.sourceLocation(getSourceLocation(element));
        nodeBuilder.element(element);
    }

    protected @Nullable Description newDescription(@Nullable GraphQLQuotedString description) {
        if (description == null) {
            return null;
        }

        String content = description.getText();
        GraphQLInjectionSearchHelper injectionSearchHelper = GraphQLInjectionSearchHelper.getInstance();
        if (injectionSearchHelper != null) {
            content = injectionSearchHelper.applyInjectionDelimitingQuotesEscape(content);
        }

        boolean multiLine = content.startsWith("\"\"\"");
        if (multiLine) {
            content = parseTripleQuotedString(content);
        } else {
            content = parseSingleQuotedString(content);
        }
        SourceLocation sourceLocation = getSourceLocation(description);
        return new Description(content, sourceLocation, multiLine, description);
    }

    protected @NotNull SourceLocation getSourceLocation(@NotNull PsiElement element) {
        return new SourceLocation(element);
    }

    private @NotNull List<Type> getImplements(@Nullable GraphQLImplementsInterfaces implementsInterfaces) {
        if (implementsInterfaces == null) return Collections.emptyList();

        List<Type> implementz = new ArrayList<>();
        for (GraphQLTypeName typeName : implementsInterfaces.getTypeNameList()) {
            TypeName newTypeName = createTypeName(typeName);
            if (newTypeName != null) {
                implementz.add(newTypeName);
            }
        }
        return implementz;
    }

    private static <T extends Node> T checkNode(@Nullable T node) {
        if (node == null) return null;
        if (node instanceof NamedNode) {
            String name = ((NamedNode<?>) node).getName();
            if (StringUtil.isEmpty(name)) return null;
        }
        if (node instanceof ListType) {
            Type type = ((ListType) node).getType();
            if (type == null) return null;
        }
        if (node instanceof NonNullType) {
            Type type = ((NonNullType) node).getType();
            if (type == null) return null;
        }
        if (node instanceof OperationTypeDefinition) {
            TypeName typeName = ((OperationTypeDefinition) node).getTypeName();
            if (typeName == null) return null;
        }
        if (node instanceof VariableDefinition) {
            Type type = ((VariableDefinition) node).getType();
            if (type == null) return null;
        }
        if (node instanceof FragmentDefinition) {
            TypeName typeCondition = ((FragmentDefinition) node).getTypeCondition();
            if (typeCondition == null) return null;
        }
        if (node instanceof FieldDefinition) {
            Type type = ((FieldDefinition) node).getType();
            if (type == null) return null;
        }
        if (node instanceof InputValueDefinition) {
            Type type = ((InputValueDefinition) node).getType();
            if (type == null) return null;
        }
        return node;
    }
}

