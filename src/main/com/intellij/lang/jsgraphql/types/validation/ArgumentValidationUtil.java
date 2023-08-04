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

import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Argument;
import com.intellij.lang.jsgraphql.types.language.ObjectField;
import com.intellij.lang.jsgraphql.types.language.Value;
import com.intellij.lang.jsgraphql.types.schema.GraphQLEnumType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLInputObjectType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLScalarType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Internal
public class ArgumentValidationUtil extends ValidationUtil {

  private final List<String> argumentNames = new ArrayList<>();
  private Value<?> argumentValue;
  private String errorMessage;
  private final List<Object> arguments = new ArrayList<>();
  private Map<String, Object> errorExtensions;

  private final String argumentName;

  public ArgumentValidationUtil(Argument argument) {
    argumentName = argument.getName();
    argumentValue = argument.getValue();
  }

  @Override
  protected void handleNullError(Value<?> value, GraphQLType type) {
    errorMessage = "must not be null";
    argumentValue = value;
  }

  @Override
  protected void handleScalarError(Value<?> value, GraphQLScalarType type, GraphQLError invalid) {
    errorMessage = "is not a valid '%s' - %s";
    arguments.add(type.getName());
    arguments.add(invalid.getMessage());
    argumentValue = value;
    errorExtensions = invalid.getExtensions();
  }

  @Override
  protected void handleEnumError(Value<?> value, GraphQLEnumType type, GraphQLError invalid) {
    errorMessage = "is not a valid '%s' - %s";
    arguments.add(type.getName());
    arguments.add(invalid.getMessage());
    argumentValue = value;
  }

  @Override
  protected void handleNotObjectError(Value<?> value, GraphQLInputObjectType type) {
    errorMessage = "must be an object type";
  }

  @Override
  protected void handleMissingFieldsError(Value<?> value, GraphQLInputObjectType type, Set<String> missingFields) {
    errorMessage = "is missing required fields '%s'";
    arguments.add(missingFields);
  }

  @Override
  protected void handleExtraFieldError(Value<?> value, GraphQLInputObjectType type, ObjectField objectField) {
    errorMessage = "contains a field not in '%s': '%s'";
    arguments.add(type.getName());
    arguments.add(objectField.getName());
  }

  @Override
  protected void handleFieldNotValidError(ObjectField objectField, GraphQLInputObjectType type) {
    argumentNames.add(0, objectField.getName());
  }

  @Override
  protected void handleFieldNotValidError(Value<?> value, GraphQLType type, int index) {
    argumentNames.add(0, String.format("[%s]", index));
  }

  public String getMessage() {
    StringBuilder argument = new StringBuilder(argumentName);
    for (String name : argumentNames) {
      if (name.startsWith("[")) {
        argument.append(name);
      }
      else {
        argument.append(".").append(name);
      }
    }
    arguments.add(0, argument.toString());

    String message = "Argument '%s'" + " " + errorMessage;

    return String.format(message, arguments.toArray());
  }

  public Map<String, Object> getErrorExtensions() {
    return errorExtensions;
  }
}
