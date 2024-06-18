/*
    The MIT License (MIT)

    Copyright (c) 2015 Andreas Marek and Contributors

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
    (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
    publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
    so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
    OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
    CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.intellij.lang.jsgraphql.types.schema.idl;

import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.CoercingParseLiteralException;
import com.intellij.lang.jsgraphql.types.schema.GraphQLScalarType;
import com.intellij.lang.jsgraphql.types.schema.idl.errors.DirectiveIllegalArgumentTypeError;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.intellij.lang.jsgraphql.types.schema.idl.errors.DirectiveIllegalArgumentTypeError.*;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;

/**
 * Class to check whether a given directive argument value
 * matches a given directive definition.
 */
@Internal
class ArgValueOfAllowedTypeChecker {

  private static final Logger LOG = Logger.getInstance(ArgValueOfAllowedTypeChecker.class);

  private final Directive directive;
  private final Node<?> element;
  private final String elementName;
  private final Argument argument;
  private final TypeDefinitionRegistry typeRegistry;
  private final RuntimeWiring runtimeWiring;

  ArgValueOfAllowedTypeChecker(final Directive directive,
                               final Node<?> element,
                               final String elementName,
                               final Argument argument,
                               final TypeDefinitionRegistry typeRegistry,
                               final RuntimeWiring runtimeWiring) {
    this.directive = directive;
    this.element = element;
    this.elementName = elementName;
    this.argument = argument;
    this.typeRegistry = typeRegistry;
    this.runtimeWiring = runtimeWiring;
  }

  /**
   * Recursively inspects an argument value given an allowed type.
   * Given the (invalid) SDL below:
   * <p>
   * directive @myDirective(arg: [[String]] ) on FIELD_DEFINITION
   * <p>
   * query {
   * f: String @myDirective(arg: ["A String"])
   * }
   * <p>
   * it will first check that the `myDirective.arg` type is an array
   * and fail when finding "A String" as it expected a nested array ([[String]]).
   *
   * @param errors         validation error collector
   * @param instanceValue  directive argument value
   * @param allowedArgType directive definition argument allowed type
   */
  void checkArgValueMatchesAllowedType(List<GraphQLError> errors, Value<?> instanceValue, Type<?> allowedArgType) {
    if (allowedArgType instanceof TypeName) {
      checkArgValueMatchesAllowedTypeName(errors, instanceValue, allowedArgType);
    }
    else if (allowedArgType instanceof ListType) {
      checkArgValueMatchesAllowedListType(errors, instanceValue, (ListType)allowedArgType);
    }
    else if (allowedArgType instanceof NonNullType) {
      checkArgValueMatchesAllowedNonNullType(errors, instanceValue, (NonNullType)allowedArgType);
    }
    else {
      LOG.warn(String.format("Unsupported Type '%s' was added. ", allowedArgType));
    }
  }

  private void addValidationError(List<GraphQLError> errors, String message, Object... args) {
    errors.add(
      new DirectiveIllegalArgumentTypeError(element, elementName, directive.getName(), argument.getName(), String.format(message, args)));
  }

  private void checkArgValueMatchesAllowedTypeName(List<GraphQLError> errors, Value<?> instanceValue, Type<?> allowedArgType) {
    if (instanceValue instanceof NullValue) {
      return;
    }

    String allowedTypeName = ((TypeName)allowedArgType).getName();
    TypeDefinition<?> allowedTypeDefinition = typeRegistry.getType(allowedTypeName).orElse(null);
    if (allowedTypeDefinition == null) return;

    if (allowedTypeDefinition instanceof ScalarTypeDefinition) {
      checkArgValueMatchesAllowedScalar(errors, instanceValue, allowedTypeName);
    }
    else if (allowedTypeDefinition instanceof EnumTypeDefinition) {
      checkArgValueMatchesAllowedEnum(errors, instanceValue, (EnumTypeDefinition)allowedTypeDefinition);
    }
    else if (allowedTypeDefinition instanceof InputObjectTypeDefinition) {
      checkArgValueMatchesAllowedInputType(errors, instanceValue, (InputObjectTypeDefinition)allowedTypeDefinition);
    }
    else {
      LOG.warn(String.format("'%s' must be an input type. It is %s instead. ", allowedTypeName, allowedTypeDefinition.getClass()));
    }
  }

  private void checkArgValueMatchesAllowedInputType(List<GraphQLError> errors,
                                                    @Nullable Value<?> instanceValue,
                                                    InputObjectTypeDefinition allowedTypeDefinition) {
    if (!(instanceValue instanceof ObjectValue objectValue)) {
      if (instanceValue != null) {
        addValidationError(errors, EXPECTED_OBJECT_MESSAGE, instanceValue.getClass().getSimpleName());
      }
      return;
    }

    // duck typing validation, if it looks like the definition
    // then it must be the same type as the definition

    List<ObjectField> fields = objectValue.getObjectFields();
    List<InputObjectTypeExtensionDefinition> inputObjExt =
      typeRegistry.inputObjectTypeExtensions().getOrDefault(allowedTypeDefinition.getName(), emptyList());
    Stream<InputValueDefinition> inputObjExtValues = inputObjExt.stream().flatMap(inputObj -> inputObj.getInputValueDefinitions().stream());
    List<InputValueDefinition> inputValueDefinitions =
      Stream.concat(allowedTypeDefinition.getInputValueDefinitions().stream(), inputObjExtValues).toList();

    // check for duplicated fields
    Map<String, Long> fieldsToOccurrenceMap = fields.stream().map(ObjectField::getName)
      .collect(groupingBy(Function.identity(), counting()));

    if (ContainerUtil.exists(fieldsToOccurrenceMap.values(), count -> count > 1)) {
      addValidationError(errors, DUPLICATED_KEYS_MESSAGE, fieldsToOccurrenceMap.entrySet().stream()
        .filter(entry -> entry.getValue() > 1)
        .map(Map.Entry::getKey)
        .collect(joining(",")));
      return;
    }

    // check for unknown fields
    Map<String, InputValueDefinition> nameToInputValueDefMap = inputValueDefinitions.stream()
      .collect(toMap(InputValueDefinition::getName, inputValueDef -> inputValueDef));

    List<ObjectField> unknownFields = ContainerUtil.filter(fields, field -> !nameToInputValueDefMap.containsKey(field.getName()));

    if (!unknownFields.isEmpty()) {
      addValidationError(errors, UNKNOWN_FIELDS_MESSAGE,
                         unknownFields.stream()
                           .map(ObjectField::getName)
                           .collect(joining(",")),
                         allowedTypeDefinition.getName());
      return;
    }

    // fields to map for easy access
    Map<String, ObjectField> nameToFieldsMap = fields.stream()
      .collect(toMap(ObjectField::getName, objectField -> objectField));
    // check each single field with its definition
    inputValueDefinitions.forEach(allowedValueDef -> {
      ObjectField objectField = nameToFieldsMap.get(allowedValueDef.getName());
      checkArgInputObjectValueFieldMatchesAllowedDefinition(errors, objectField, allowedValueDef);
    });
  }

  private void checkArgValueMatchesAllowedEnum(List<GraphQLError> errors,
                                               @Nullable Value<?> instanceValue,
                                               EnumTypeDefinition allowedTypeDefinition) {
    if (!(instanceValue instanceof EnumValue enumValue)) {
      if (instanceValue != null) {
        addValidationError(errors, EXPECTED_ENUM_MESSAGE, instanceValue.getClass().getSimpleName());
      }
      return;
    }

    List<EnumTypeExtensionDefinition> enumExtensions =
      typeRegistry.enumTypeExtensions().getOrDefault(allowedTypeDefinition.getName(), emptyList());
    Stream<EnumValueDefinition> enumExtStream = enumExtensions.stream().flatMap(enumExt -> enumExt.getEnumValueDefinitions().stream());
    List<EnumValueDefinition> enumValueDefinitions =
      Stream.concat(allowedTypeDefinition.getEnumValueDefinitions().stream(), enumExtStream).toList();

    boolean noneMatchAllowedEnumValue =
      !ContainerUtil.exists(enumValueDefinitions, enumAllowedValue -> enumAllowedValue.getName().equals(enumValue.getName()));

    if (noneMatchAllowedEnumValue) {
      addValidationError(errors, MUST_BE_VALID_ENUM_VALUE_MESSAGE, enumValue.getName(), enumValueDefinitions.stream()
        .map(EnumValueDefinition::getName)
        .collect(joining(",")));
    }
  }

  private void checkArgValueMatchesAllowedScalar(List<GraphQLError> errors, Value<?> instanceValue, String allowedTypeName) {
    if (instanceValue instanceof ArrayValue
        || instanceValue instanceof EnumValue
        || instanceValue instanceof ObjectValue) {
      addValidationError(errors, EXPECTED_SCALAR_MESSAGE, instanceValue.getClass().getSimpleName());
      return;
    }

    GraphQLScalarType scalarType = runtimeWiring.getScalars().get(allowedTypeName);
    // scalarType will always be present as
    // scalar implementation validation has been performed earlier
    if (!isArgumentValueScalarLiteral(scalarType, instanceValue)) {
      addValidationError(errors, NOT_A_VALID_SCALAR_LITERAL_MESSAGE, allowedTypeName);
    }
  }

  private void checkArgInputObjectValueFieldMatchesAllowedDefinition(List<GraphQLError> errors,
                                                                     ObjectField objectField,
                                                                     InputValueDefinition allowedValueDef) {

    if (objectField != null) {
      checkArgValueMatchesAllowedType(errors, objectField.getValue(), allowedValueDef.getType());
      return;
    }

    // check if field definition is required and has no default value
    if (allowedValueDef.getType() instanceof NonNullType && allowedValueDef.getDefaultValue() == null) {
      addValidationError(errors, MISSING_REQUIRED_FIELD_MESSAGE, allowedValueDef.getName());
    }

    // other cases are
    // - field definition is marked as non-null but has a default value, so the default value can be used
    // - field definition is nullable hence null can be used
  }

  private void checkArgValueMatchesAllowedNonNullType(List<GraphQLError> errors, Value<?> instanceValue, NonNullType allowedArgType) {
    if (instanceValue instanceof NullValue) {
      addValidationError(errors, EXPECTED_NON_NULL_MESSAGE);
      return;
    }

    Type<?> unwrappedAllowedType = allowedArgType.getType();
    checkArgValueMatchesAllowedType(errors, instanceValue, unwrappedAllowedType);
  }

  private void checkArgValueMatchesAllowedListType(List<GraphQLError> errors, Value<?> instanceValue, ListType allowedArgType) {
    if (instanceValue instanceof NullValue) {
      return;
    }

    Type<?> unwrappedAllowedType = allowedArgType.getType();
    if (!(instanceValue instanceof ArrayValue arrayValue)) {
      checkArgValueMatchesAllowedType(errors, instanceValue, unwrappedAllowedType);
      return;
    }

    boolean isUnwrappedList = unwrappedAllowedType instanceof ListType;

    // validate each instance value in the list, all instances must match for the list to match
    arrayValue.getValues().forEach(value -> {
      // restrictive check for sub-arrays
      if (isUnwrappedList && !(value instanceof ArrayValue)) {
        addValidationError(errors, EXPECTED_LIST_MESSAGE, value.getClass().getSimpleName());
      }
      checkArgValueMatchesAllowedType(errors, value, unwrappedAllowedType);
    });
  }

  private static boolean isArgumentValueScalarLiteral(GraphQLScalarType scalarType, Value<?> instanceValue) {
    if (instanceValue == null) return false;

    try {
      scalarType.getCoercing().parseLiteral(instanceValue);
      return true;
    }
    catch (CoercingParseLiteralException ex) {
      return false;
    }
  }
}
