package com.intellij.lang.jsgraphql.schema;


import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.jsgraphql.ide.injection.GraphQLInjectedLanguage;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryManager;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.intellij.lang.jsgraphql.types.Assert.assertShouldNeverHappen;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.emptyList;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.mapNotNull;
import static com.intellij.lang.jsgraphql.types.parser.StringValueParsing.parseSingleQuotedString;
import static com.intellij.lang.jsgraphql.types.parser.StringValueParsing.parseTripleQuotedString;

@SuppressWarnings("rawtypes")
public final class GraphQLPsiDocumentBuilder {
  private static final Logger LOG = Logger.getInstance(GraphQLPsiDocumentBuilder.class);

  private static final String IS_IN_LIBRARY_KEY = "is.in.library";
  private static final String TYPE_DEFINITIONS_COUNT = "type.definitions.count";

  private final GraphQLFile myFile;
  private final boolean myIsInLibrary;

  public static boolean isInLibrary(@NotNull Node<?> node) {
    return node.getAdditionalData().containsKey(IS_IN_LIBRARY_KEY);
  }

  public static int getTypeDefinitionsCount(@NotNull Document document) {
    String countAsString = document.getAdditionalData().get(TYPE_DEFINITIONS_COUNT);
    if (countAsString == null) {
      return document.getDefinitionsOfType(SDLDefinition.class).size();
    }

    try {
      return Integer.parseInt(countAsString);
    }
    catch (NumberFormatException e) {
      LOG.warn(e);
      return 0;
    }
  }

  public GraphQLPsiDocumentBuilder(@NotNull GraphQLFile file) {
    myFile = file;
    myIsInLibrary = GraphQLLibraryManager.getInstance(file.getProject()).isLibraryRoot(file.getVirtualFile());
  }

  public @NotNull Document createDocument() {
    Document.Builder document = Document.newDocument();
    addCommonData(document, myFile);

    Collection<GraphQLDefinition> psiDefinitions = myFile.getDefinitions();
    var definitions = new ArrayList<Definition>(psiDefinitions.size());
    var typeDefinitionsCount = 0;
    for (GraphQLDefinition psiDefinition : psiDefinitions) {
      Definition definition = createDefinition(psiDefinition);
      if (definition != null) {
        definitions.add(definition);
        if (definition instanceof SDLDefinition) {
          typeDefinitionsCount++;
        }
      }
    }
    document.definitions(definitions);
    document.additionalData(TYPE_DEFINITIONS_COUNT, String.valueOf(typeDefinitionsCount));

    return document.build();
  }

  public @Nullable Definition createDefinition(@NotNull GraphQLDefinition definition) {
    if (definition instanceof GraphQLOperationDefinition) {
      return createOperationDefinition(((GraphQLOperationDefinition)definition));
    }
    else if (definition instanceof GraphQLFragmentDefinition) {
      return createFragmentDefinition(((GraphQLFragmentDefinition)definition));
    }
    else if (definition instanceof GraphQLTypeExtension) {
      return createTypeExtension(((GraphQLTypeExtension)definition));
    }
    else if (definition instanceof GraphQLSchemaExtension) {
      return createSchemaExtension(((GraphQLSchemaExtension)definition));
    }
    else if (definition instanceof GraphQLTypeSystemDefinition) {
      return createTypeSystemDefinition(((GraphQLTypeSystemDefinition)definition));
    }
    else if (definition instanceof GraphQLTemplateDefinition) {
      return null;
    }
    else {
      return assertShouldNeverHappen();
    }
  }

  private @NotNull OperationDefinition createOperationDefinition(@NotNull GraphQLOperationDefinition definition) {
    OperationDefinition.Builder operationDefinition = OperationDefinition.newOperationDefinition();
    addCommonData(operationDefinition, definition);
    if (definition instanceof GraphQLSelectionSetOperationDefinition selectionSetOperation) {
      operationDefinition.operation(OperationDefinition.Operation.QUERY);
      operationDefinition.selectionSet(createSelectionSet(selectionSetOperation.getSelectionSet()));
    }
    else if (definition instanceof GraphQLTypedOperationDefinition typedOperation) {
      operationDefinition.operation(parseOperation(typedOperation));
      operationDefinition.name(typedOperation.getName());

      operationDefinition.variableDefinitions(createVariableDefinitions(typedOperation.getVariableDefinitions()));
      operationDefinition.directives(createDirectives(typedOperation.getDirectives()));
      operationDefinition.selectionSet(createSelectionSet(typedOperation.getSelectionSet()));
    }
    else {
      assertShouldNeverHappen();
    }

    return operationDefinition.build();
  }

  private static @NotNull OperationDefinition.Operation parseOperation(@NotNull GraphQLTypedOperationDefinition operation) {
    return switch (operation.getOperationType().getText()) {
      case "query" -> OperationDefinition.Operation.QUERY;
      case "mutation" -> OperationDefinition.Operation.MUTATION;
      case "subscription" -> OperationDefinition.Operation.SUBSCRIPTION;
      default -> assertShouldNeverHappen("InternalError: unknown operationTypeContext=%s", operation.getText());
    };
  }

  private @Nullable FragmentSpread createFragmentSpread(@NotNull GraphQLFragmentSpread fragment) {
    FragmentSpread.Builder fragmentSpread = FragmentSpread.newFragmentSpread().name(fragment.getName());
    addCommonData(fragmentSpread, fragment);
    fragmentSpread.directives(createDirectives(fragment.getDirectives()));
    return checkNode(fragmentSpread.build());
  }

  private @NotNull List<VariableDefinition> createVariableDefinitions(@Nullable GraphQLVariableDefinitions definitions) {
    if (definitions == null) {
      return emptyList();
    }
    return mapNotNull(definitions.getVariableDefinitions(), this::createVariableDefinition);
  }

  private @Nullable VariableDefinition createVariableDefinition(@NotNull GraphQLVariableDefinition definition) {
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

  private @Nullable FragmentDefinition createFragmentDefinition(@NotNull GraphQLFragmentDefinition definition) {
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


  private @Nullable SelectionSet createSelectionSet(@Nullable GraphQLSelectionSet selectionSet) {
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


  private @Nullable Field createField(@NotNull GraphQLField field) {
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


  private @NotNull InlineFragment createInlineFragment(@NotNull GraphQLInlineFragment fragment) {
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

  private @Nullable SDLDefinition createTypeSystemDefinition(@NotNull GraphQLTypeSystemDefinition definition) {
    if (definition instanceof GraphQLSchemaDefinition) {
      return createSchemaDefinition((GraphQLSchemaDefinition)definition);
    }
    else if (definition instanceof GraphQLDirectiveDefinition) {
      return createDirectiveDefinition((GraphQLDirectiveDefinition)definition);
    }
    else if (definition instanceof GraphQLTypeDefinition) {
      return createTypeDefinition((GraphQLTypeDefinition)definition);
    }
    else {
      return assertShouldNeverHappen();
    }
  }

  private @Nullable TypeDefinition createTypeExtension(@NotNull GraphQLTypeExtension extension) {
    if (extension instanceof GraphQLEnumTypeExtensionDefinition) {
      return createEnumTypeExtensionDefinition(((GraphQLEnumTypeExtensionDefinition)extension));
    }
    else if (extension instanceof GraphQLObjectTypeExtensionDefinition) {
      return createObjectTypeExtensionDefinition(((GraphQLObjectTypeExtensionDefinition)extension));
    }
    else if (extension instanceof GraphQLInputObjectTypeExtensionDefinition) {
      return createInputObjectTypeExtensionDefinition(((GraphQLInputObjectTypeExtensionDefinition)extension));
    }
    else if (extension instanceof GraphQLInterfaceTypeExtensionDefinition) {
      return createInterfaceTypeExtensionDefinition(((GraphQLInterfaceTypeExtensionDefinition)extension));
    }
    else if (extension instanceof GraphQLScalarTypeExtensionDefinition) {
      return createScalarTypeExtensionDefinition(((GraphQLScalarTypeExtensionDefinition)extension));
    }
    else if (extension instanceof GraphQLUnionTypeExtensionDefinition) {
      return createUnionTypeExtensionDefinition(((GraphQLUnionTypeExtensionDefinition)extension));
    }
    else {
      return assertShouldNeverHappen();
    }
  }

  private @Nullable TypeDefinition createTypeDefinition(@NotNull GraphQLTypeDefinition definition) {
    if (definition instanceof GraphQLEnumTypeDefinition) {
      return createEnumTypeDefinition(((GraphQLEnumTypeDefinition)definition));
    }
    else if (definition instanceof GraphQLObjectTypeDefinition) {
      return createObjectTypeDefinition(((GraphQLObjectTypeDefinition)definition));
    }
    else if (definition instanceof GraphQLInputObjectTypeDefinition) {
      return createInputObjectTypeDefinition(((GraphQLInputObjectTypeDefinition)definition));
    }
    else if (definition instanceof GraphQLInterfaceTypeDefinition) {
      return createInterfaceTypeDefinition(((GraphQLInterfaceTypeDefinition)definition));
    }
    else if (definition instanceof GraphQLScalarTypeDefinition) {
      return createScalarTypeDefinition(((GraphQLScalarTypeDefinition)definition));
    }
    else if (definition instanceof GraphQLUnionTypeDefinition) {
      return createUnionTypeDefinition(((GraphQLUnionTypeDefinition)definition));
    }
    else {
      return assertShouldNeverHappen();
    }
  }

  private @Nullable Type createType(@Nullable GraphQLType type) {
    if (type == null) return null;

    if (type instanceof GraphQLTypeName) {
      return createTypeName(((GraphQLTypeName)type));
    }
    else if (type instanceof GraphQLNonNullType) {
      return createNonNullType(((GraphQLNonNullType)type));
    }
    else if (type instanceof GraphQLListType) {
      return createListType(((GraphQLListType)type));
    }
    else {
      return assertShouldNeverHappen();
    }
  }

  private @Nullable TypeName createTypeName(@Nullable GraphQLTypeName typeName) {
    if (typeName == null) return null;
    TypeName.Builder builder = TypeName.newTypeName();
    builder.name(typeName.getName());
    addCommonData(builder, typeName);
    return checkNode(builder.build());
  }

  private @Nullable NonNullType createNonNullType(@NotNull GraphQLNonNullType nonNullType) {
    NonNullType.Builder builder = NonNullType.newNonNullType();
    addCommonData(builder, nonNullType);
    GraphQLType type = nonNullType.getType();
    if (type instanceof GraphQLListType) {
      builder.type(createListType((GraphQLListType)type));
    }
    else if (type instanceof GraphQLTypeName) {
      builder.type(createTypeName((GraphQLTypeName)type));
    }
    else {
      return null;
    }
    return checkNode(builder.build());
  }

  private @NotNull ListType createListType(@NotNull GraphQLListType listType) {
    ListType.Builder builder = ListType.newListType();
    addCommonData(builder, listType);
    builder.type(createType(listType.getType()));
    return checkNode(builder.build());
  }

  private @Nullable Argument createArgument(@NotNull GraphQLArgument argument) {
    Argument.Builder builder = Argument.newArgument();
    addCommonData(builder, argument);
    builder.name(argument.getName());
    builder.value(createValue(argument.getValue()));
    return checkNode(builder.build());
  }

  private @NotNull List<Argument> createArguments(@Nullable GraphQLArguments arguments) {
    if (arguments == null) {
      return emptyList();
    }
    return mapNotNull(arguments.getArgumentList(), this::createArgument);
  }


  private @NotNull List<Directive> createDirectives(@NotNull List<GraphQLDirective> directives) {
    return mapNotNull(directives, this::createDirective);
  }

  private @Nullable Directive createDirective(@NotNull GraphQLDirective directive) {
    Directive.Builder builder = Directive.newDirective();
    builder.name(directive.getName());
    addCommonData(builder, directive);
    builder.arguments(createArguments(directive.getArguments()));
    return checkNode(builder.build());
  }

  private @NotNull SchemaDefinition createSchemaDefinition(@NotNull GraphQLSchemaDefinition schemaDefinition) {
    SchemaDefinition.Builder def = SchemaDefinition.newSchemaDefinition();
    addCommonData(def, schemaDefinition);
    def.directives(createDirectives(schemaDefinition.getDirectives()));
    def.description(newDescription(schemaDefinition.getDescription()));
    GraphQLOperationTypeDefinitions operationTypeDefinitions = schemaDefinition.getOperationTypeDefinitions();
    if (operationTypeDefinitions != null) {
      def.operationTypeDefinitions(mapNotNull(operationTypeDefinitions.getOperationTypeDefinitionList(),
                                              this::createOperationTypeDefinition));
    }
    return def.build();
  }

  private @NotNull SchemaExtensionDefinition createSchemaExtension(@NotNull GraphQLSchemaExtension extension) {
    SchemaExtensionDefinition.Builder def = SchemaExtensionDefinition.newSchemaExtensionDefinition();
    addCommonData(def, extension);
    def.directives(createDirectives(extension.getDirectives()));

    GraphQLOperationTypeDefinitions operationTypeDefinitions = extension.getOperationTypeDefinitions();
    if (operationTypeDefinitions != null) {
      def.operationTypeDefinitions(mapNotNull(operationTypeDefinitions.getOperationTypeDefinitionList(),
                                              this::createOperationTypeDefinition));
    }
    return def.build();
  }

  private @Nullable OperationTypeDefinition createOperationTypeDefinition(@NotNull GraphQLOperationTypeDefinition definition) {
    OperationTypeDefinition.Builder def = OperationTypeDefinition.newOperationTypeDefinition();
    GraphQLOperationType operationType = definition.getOperationType();
    if (operationType != null) {
      def.name(operationType.getText());
    }
    def.typeName(createTypeName(definition.getTypeName()));
    addCommonData(def, definition);
    return checkNode(def.build());
  }

  private @Nullable ScalarTypeDefinition createScalarTypeDefinition(@NotNull GraphQLScalarTypeDefinition typeDefinition) {
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

  private @Nullable ScalarTypeExtensionDefinition createScalarTypeExtensionDefinition(@NotNull GraphQLScalarTypeExtensionDefinition extensionDefinition) {
    ScalarTypeExtensionDefinition.Builder def = ScalarTypeExtensionDefinition.newScalarTypeExtensionDefinition();
    GraphQLTypeName typeName = extensionDefinition.getTypeName();
    if (typeName != null) {
      def.name(typeName.getName());
    }
    addCommonData(def, extensionDefinition);
    def.directives(createDirectives(extensionDefinition.getDirectives()));
    return checkNode(def.build());
  }

  private @Nullable ObjectTypeDefinition createObjectTypeDefinition(@NotNull GraphQLObjectTypeDefinition typeDefinition) {
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

  private @Nullable ObjectTypeExtensionDefinition createObjectTypeExtensionDefinition(@NotNull GraphQLObjectTypeExtensionDefinition extensionDefinition) {
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

  private @NotNull List<FieldDefinition> createFieldDefinitions(@Nullable GraphQLFieldsDefinition fieldsDefinition) {
    if (fieldsDefinition == null) {
      return emptyList();
    }
    return mapNotNull(fieldsDefinition.getFieldDefinitionList(), this::createFieldDefinition);
  }

  private @Nullable FieldDefinition createFieldDefinition(@NotNull GraphQLFieldDefinition fieldDefinition) {
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

  private @NotNull List<InputValueDefinition> createInputValueDefinitions(@NotNull List<GraphQLInputValueDefinition> defs) {
    return mapNotNull(defs, this::createInputValueDefinition);
  }

  private @Nullable InputValueDefinition createInputValueDefinition(@NotNull GraphQLInputValueDefinition valueDefinition) {
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

  private @Nullable InterfaceTypeDefinition createInterfaceTypeDefinition(@NotNull GraphQLInterfaceTypeDefinition typeDefinition) {
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

  private @Nullable InterfaceTypeExtensionDefinition createInterfaceTypeExtensionDefinition(@NotNull GraphQLInterfaceTypeExtensionDefinition extensionDefinition) {
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

  private @Nullable UnionTypeDefinition createUnionTypeDefinition(@NotNull GraphQLUnionTypeDefinition typeDefinition) {
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

  private @Nullable UnionTypeExtensionDefinition createUnionTypeExtensionDefinition(@NotNull GraphQLUnionTypeExtensionDefinition extensionDefinition) {
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

  private @Nullable EnumTypeDefinition createEnumTypeDefinition(@NotNull GraphQLEnumTypeDefinition enumTypeDefinition) {
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

  private @Nullable EnumTypeExtensionDefinition createEnumTypeExtensionDefinition(@NotNull GraphQLEnumTypeExtensionDefinition extensionDefinition) {
    EnumTypeExtensionDefinition.Builder def = EnumTypeExtensionDefinition.newEnumTypeExtensionDefinition();
    GraphQLTypeName typeName = extensionDefinition.getTypeName();
    if (typeName != null) {
      def.name(typeName.getName());
    }
    addCommonData(def, extensionDefinition);
    def.directives(createDirectives(extensionDefinition.getDirectives()));
    GraphQLEnumValueDefinitions enumValueDefinitions = extensionDefinition.getEnumValueDefinitions();
    if (enumValueDefinitions != null) {
      def.enumValueDefinitions(
        mapNotNull(enumValueDefinitions.getEnumValueDefinitionList(), this::createEnumValueDefinition));
    }
    return checkNode(def.build());
  }

  private @Nullable EnumValueDefinition createEnumValueDefinition(@NotNull GraphQLEnumValueDefinition valueDefinition) {
    EnumValueDefinition.Builder def = EnumValueDefinition.newEnumValueDefinition();
    def.name(valueDefinition.getEnumValue().getName());
    addCommonData(def, valueDefinition);
    def.description(newDescription(valueDefinition.getDescription()));
    def.directives(createDirectives(valueDefinition.getDirectives()));
    return checkNode(def.build());
  }

  private @Nullable InputObjectTypeDefinition createInputObjectTypeDefinition(@NotNull GraphQLInputObjectTypeDefinition typeDefinition) {
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

  private @Nullable InputObjectTypeExtensionDefinition createInputObjectTypeExtensionDefinition(@NotNull GraphQLInputObjectTypeExtensionDefinition extensionDefinition) {
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

  private @Nullable DirectiveDefinition createDirectiveDefinition(@NotNull GraphQLDirectiveDefinition directiveDefinition) {
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

  private @Nullable DirectiveLocation createDirectiveLocation(@NotNull GraphQLDirectiveLocation directiveLocation) {
    DirectiveLocation.Builder def = DirectiveLocation.newDirectiveLocation();
    def.name(directiveLocation.getText());
    addCommonData(def, directiveLocation);
    return checkNode(def.build());
  }

  private @Nullable Value createValue(@Nullable GraphQLValue value) {
    if (value == null) return null;

    if (value instanceof GraphQLIntValue) {
      try {
        IntValue.Builder intValue = IntValue.newIntValue().value(new BigInteger(value.getText()));
        addCommonData(intValue, value);
        return intValue.build();
      }
      catch (NumberFormatException e) {
        return null;
      }
    }
    else if (value instanceof GraphQLFloatValue) {
      try {
        FloatValue.Builder floatValue = FloatValue.newFloatValue().value(new BigDecimal(value.getText()));
        addCommonData(floatValue, value);
        return floatValue.build();
      }
      catch (NumberFormatException e) {
        return null;
      }
    }
    else if (value instanceof GraphQLBooleanValue) {
      BooleanValue.Builder booleanValue = BooleanValue.newBooleanValue()
        .value(Boolean.parseBoolean(value.getText()));
      addCommonData(booleanValue, value);
      return booleanValue.build();
    }
    else if (value instanceof GraphQLNullValue) {
      NullValue.Builder nullValue = NullValue.newNullValue();
      addCommonData(nullValue, value);
      return nullValue.build();
    }
    else if (value instanceof GraphQLStringValue) {
      StringValue.Builder stringValue = StringValue.newStringValue()
        .value(((GraphQLStringValue)value).getValueAsString());
      addCommonData(stringValue, value);
      return stringValue.build();
    }
    else if (value instanceof GraphQLEnumValue) {
      EnumValue.Builder enumValue = EnumValue.newEnumValue().name(((GraphQLEnumValue)value).getName());
      addCommonData(enumValue, value);
      return checkNode(enumValue.build());
    }
    else if (value instanceof GraphQLArrayValue) {
      ArrayValue.Builder arrayValue = ArrayValue.newArrayValue();
      addCommonData(arrayValue, value);
      List<Value> values = new ArrayList<>();
      for (GraphQLValue arrayValueItem : ((GraphQLArrayValue)value).getValueList()) {
        Value newValue = createValue(arrayValueItem);
        if (newValue != null) {
          values.add(newValue);
        }
      }
      return arrayValue.values(values).build();
    }
    else if (value instanceof GraphQLObjectValue) {
      ObjectValue.Builder objectValue = ObjectValue.newObjectValue();
      addCommonData(objectValue, value);
      List<ObjectField> objectFields = new ArrayList<>();
      for (GraphQLObjectField field : ((GraphQLObjectValue)value).getObjectFieldList()) {
        ObjectField objectField = checkNode(ObjectField.newObjectField()
                                              .name(field.getName())
                                              .value(createValue(field.getValue()))
                                              .build());
        if (objectField != null) {
          objectFields.add(objectField);
        }
      }
      return objectValue.objectFields(objectFields).build();
    }
    else if (value instanceof GraphQLVariable) {
      VariableReference.Builder variableReference = VariableReference.newVariableReference()
        .name(((GraphQLVariable)value).getName());
      addCommonData(variableReference, value);
      return checkNode(variableReference.build());
    }
    else if (value instanceof GraphQLTemplateVariable) {
      return null;
    }
    return assertShouldNeverHappen();
  }

  private void addCommonData(NodeBuilder nodeBuilder, @NotNull GraphQLElement element) {
    nodeBuilder.sourceLocation(GraphQLTypeDefinitionUtil.getSourceLocation(element));

    if (myIsInLibrary) {
      nodeBuilder.additionalData(IS_IN_LIBRARY_KEY, "");
    }
  }

  private static @Nullable Description newDescription(@Nullable GraphQLDescription description) {
    if (description == null) {
      return null;
    }

    String content = description.getText();

    PsiLanguageInjectionHost injectionHost =
      InjectedLanguageManager.getInstance(description.getProject()).getInjectionHost(description);
    GraphQLInjectedLanguage injectedLanguage = injectionHost != null ? GraphQLInjectedLanguage.forElement(injectionHost) : null;
    if (injectedLanguage != null) {
      String escaped = injectedLanguage.escapeHostElements(content);
      if (!StringUtil.isEmpty(escaped)) {
        content = escaped;
      }
    }

    SourceLocation sourceLocation = GraphQLTypeDefinitionUtil.getSourceLocation(description);
    boolean multiLine = content.startsWith("\"\"\"");
    if (multiLine) {
      content = parseTripleQuotedString(content);
    }
    else {
      content = parseSingleQuotedString(content);
    }
    return new Description(content, sourceLocation, multiLine);
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
      String name = ((NamedNode<?>)node).getName();
      if (StringUtil.isEmpty(name)) return null;
    }
    if (node instanceof ListType) {
      Type type = ((ListType)node).getType();
      if (type == null) return null;
    }
    if (node instanceof NonNullType) {
      Type type = ((NonNullType)node).getType();
      if (type == null) return null;
    }
    if (node instanceof OperationTypeDefinition) {
      TypeName typeName = ((OperationTypeDefinition)node).getTypeName();
      if (typeName == null) return null;
    }
    if (node instanceof VariableDefinition) {
      Type type = ((VariableDefinition)node).getType();
      if (type == null) return null;
    }
    if (node instanceof FragmentDefinition) {
      TypeName typeCondition = ((FragmentDefinition)node).getTypeCondition();
      if (typeCondition == null) return null;
    }
    if (node instanceof FieldDefinition) {
      Type type = ((FieldDefinition)node).getType();
      if (type == null) return null;
    }
    if (node instanceof InputValueDefinition) {
      Type type = ((InputValueDefinition)node).getType();
      if (type == null) return null;
    }
    return node;
  }
}

