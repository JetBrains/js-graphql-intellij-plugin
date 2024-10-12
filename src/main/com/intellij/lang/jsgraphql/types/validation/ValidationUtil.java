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
package com.intellij.lang.jsgraphql.types.validation;


import com.google.common.collect.ImmutableSet;
import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.*;
import com.intellij.lang.jsgraphql.types.schema.visibility.DefaultGraphqlFieldVisibility;
import com.intellij.lang.jsgraphql.types.schema.visibility.GraphqlFieldVisibility;

import java.util.*;

import static com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil.*;

@Internal
public class ValidationUtil {

  public TypeName getUnmodifiedType(Type<?> type) {
    if (type instanceof ListType) {
      return getUnmodifiedType(((ListType)type).getType());
    }
    else if (type instanceof NonNullType) {
      return getUnmodifiedType(((NonNullType)type).getType());
    }
    else if (type instanceof TypeName) {
      return (TypeName)type;
    }
    return Assert.assertShouldNeverHappen();
  }

  protected void handleNullError(Value<?> value, GraphQLType type) {
  }

  protected void handleScalarError(Value<?> value, GraphQLScalarType type, GraphQLError invalid) {
  }

  protected void handleEnumError(Value<?> value, GraphQLEnumType type, GraphQLError invalid) {
  }

  protected void handleNotObjectError(Value<?> value, GraphQLInputObjectType type) {
  }

  protected void handleMissingFieldsError(Value<?> value, GraphQLInputObjectType type, Set<String> missingFields) {
  }

  protected void handleExtraFieldError(Value<?> value, GraphQLInputObjectType type, ObjectField objectField) {
  }

  protected void handleFieldNotValidError(ObjectField objectField, GraphQLInputObjectType type) {
  }

  protected void handleFieldNotValidError(Value<?> value, GraphQLType type, int index) {
  }

  public boolean isValidLiteralValue(Value<?> value, GraphQLType type, GraphQLSchema schema) {
    if (value == null || value instanceof NullValue) {
      boolean valid = !(isNonNull(type));
      if (!valid) {
        handleNullError(value, type);
      }
      return valid;
    }
    if (value instanceof VariableReference) {
      return true;
    }
    if (isNonNull(type)) {
      return isValidLiteralValue(value, unwrapOne(type), schema);
    }

    if (type instanceof GraphQLScalarType) {
      Optional<GraphQLError> invalid = parseLiteral(value, ((GraphQLScalarType)type).getCoercing());
      invalid.ifPresent(graphQLError -> handleScalarError(value, (GraphQLScalarType)type, graphQLError));
      return invalid.isEmpty();
    }
    if (type instanceof GraphQLEnumType) {
      Optional<GraphQLError> invalid = parseLiteralEnum(value, (GraphQLEnumType)type);
      invalid.ifPresent(graphQLError -> handleEnumError(value, (GraphQLEnumType)type, graphQLError));
      return invalid.isEmpty();
    }

    if (isList(type)) {
      return isValidLiteralValue(value, (GraphQLList)type, schema);
    }
    return type instanceof GraphQLInputObjectType && isValidLiteralValue(value, (GraphQLInputObjectType)type, schema);
  }

  private Optional<GraphQLError> parseLiteralEnum(Value<?> value, GraphQLEnumType graphQLEnumType) {
    try {
      graphQLEnumType.parseLiteral(value);
      return Optional.empty();
    }
    catch (CoercingParseLiteralException e) {
      return Optional.of(e);
    }
  }

  private Optional<GraphQLError> parseLiteral(Value<?> value, Coercing<?, ?> coercing) {
    try {
      coercing.parseLiteral(value);
      return Optional.empty();
    }
    catch (CoercingParseLiteralException e) {
      return Optional.of(e);
    }
  }

  private boolean isValidLiteralValue(Value<?> value, GraphQLInputObjectType type, GraphQLSchema schema) {
    if (!(value instanceof ObjectValue objectValue)) {
      handleNotObjectError(value, type);
      return false;
    }
    GraphqlFieldVisibility fieldVisibility = DefaultGraphqlFieldVisibility.DEFAULT_FIELD_VISIBILITY;
    Map<String, ObjectField> objectFieldMap = fieldMap(objectValue);

    Set<String> missingFields = getMissingFields(type, objectFieldMap, fieldVisibility);
    if (!missingFields.isEmpty()) {
      handleMissingFieldsError(value, type, missingFields);
      return false;
    }

    for (ObjectField objectField : objectValue.getObjectFields()) {

      GraphQLInputObjectField inputObjectField = fieldVisibility.getFieldDefinition(type, objectField.getName());
      if (inputObjectField == null) {
        handleExtraFieldError(value, type, objectField);
        return false;
      }
      if (!isValidLiteralValue(objectField.getValue(), inputObjectField.getType(), schema)) {
        handleFieldNotValidError(objectField, type);
        return false;
      }
    }
    return true;
  }

  private Set<String> getMissingFields(GraphQLInputObjectType type,
                                       Map<String, ObjectField> objectFieldMap,
                                       GraphqlFieldVisibility fieldVisibility) {
    return fieldVisibility.getFieldDefinitions(type).stream()
      .filter(field -> isNonNull(field.getType()))
      .filter(value -> (value.getDefaultValue() == null) && !objectFieldMap.containsKey(value.getName()))
      .map(GraphQLInputObjectField::getName)
      .collect(ImmutableSet.toImmutableSet());
  }

  private Map<String, ObjectField> fieldMap(ObjectValue objectValue) {
    Map<String, ObjectField> result = new LinkedHashMap<>();
    for (ObjectField objectField : objectValue.getObjectFields()) {
      result.put(objectField.getName(), objectField);
    }
    return result;
  }

  private boolean isValidLiteralValue(Value<?> value, GraphQLList type, GraphQLSchema schema) {
    GraphQLType wrappedType = type.getWrappedType();
    if (value instanceof ArrayValue) {
      List<Value> values = ((ArrayValue)value).getValues();
      for (int i = 0; i < values.size(); i++) {
        if (!isValidLiteralValue(values.get(i), wrappedType, schema)) {
          handleFieldNotValidError(values.get(i), wrappedType, i);
          return false;
        }
      }
      return true;
    }
    else {
      return isValidLiteralValue(value, wrappedType, schema);
    }
  }
}
