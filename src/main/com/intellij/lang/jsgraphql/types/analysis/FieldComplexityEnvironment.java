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
package com.intellij.lang.jsgraphql.types.analysis;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.Field;
import com.intellij.lang.jsgraphql.types.schema.GraphQLCompositeType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;

import java.util.Map;
import java.util.Objects;

@PublicApi
public class FieldComplexityEnvironment {
  private final Field field;
  private final GraphQLFieldDefinition fieldDefinition;
  private final GraphQLCompositeType parentType;
  private final FieldComplexityEnvironment parentEnvironment;
  private final Map<String, Object> arguments;

  public FieldComplexityEnvironment(Field field,
                                    GraphQLFieldDefinition fieldDefinition,
                                    GraphQLCompositeType parentType,
                                    Map<String, Object> arguments,
                                    FieldComplexityEnvironment parentEnvironment) {
    this.field = field;
    this.fieldDefinition = fieldDefinition;
    this.parentType = parentType;
    this.arguments = arguments;
    this.parentEnvironment = parentEnvironment;
  }

  public Field getField() {
    return field;
  }

  public GraphQLFieldDefinition getFieldDefinition() {
    return fieldDefinition;
  }

  public GraphQLCompositeType getParentType() {
    return parentType;
  }

  public FieldComplexityEnvironment getParentEnvironment() {
    return parentEnvironment;
  }

  public Map<String, Object> getArguments() {
    return arguments;
  }

  @Override
  public String toString() {
    return "FieldComplexityEnvironment{" +
           "field=" + field +
           ", fieldDefinition=" + fieldDefinition +
           ", parentType=" + parentType +
           ", arguments=" + arguments +
           '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FieldComplexityEnvironment that = (FieldComplexityEnvironment)o;
    return Objects.equals(field, that.field)
           && Objects.equals(fieldDefinition, that.fieldDefinition)
           && Objects.equals(parentType, that.parentType)
           && Objects.equals(parentEnvironment, that.parentEnvironment)
           && Objects.equals(arguments, that.arguments);
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + Objects.hashCode(field);
    result = 31 * result + Objects.hashCode(fieldDefinition);
    result = 31 * result + Objects.hashCode(parentType);
    result = 31 * result + Objects.hashCode(parentEnvironment);
    result = 31 * result + Objects.hashCode(arguments);
    return result;
  }
}


