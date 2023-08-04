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

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Field;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.language.SelectionSetContainer;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldsContainer;
import com.intellij.lang.jsgraphql.types.schema.GraphQLOutputType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;

import java.util.Map;
import java.util.Objects;

@Internal
public class QueryVisitorFieldEnvironmentImpl implements QueryVisitorFieldEnvironment {

  private final boolean typeNameIntrospectionField;
  private final Field field;
  private final GraphQLFieldDefinition fieldDefinition;
  private final GraphQLOutputType parentType;
  private final GraphQLFieldsContainer unmodifiedParentType;
  private final Map<String, Object> arguments;
  private final QueryVisitorFieldEnvironment parentEnvironment;
  private final SelectionSetContainer selectionSetContainer;
  private final TraverserContext<Node> traverserContext;
  private final GraphQLSchema schema;

  public QueryVisitorFieldEnvironmentImpl(boolean typeNameIntrospectionField,
                                          Field field,
                                          GraphQLFieldDefinition fieldDefinition,
                                          GraphQLOutputType parentType,
                                          GraphQLFieldsContainer unmodifiedParentType,
                                          QueryVisitorFieldEnvironment parentEnvironment,
                                          Map<String, Object> arguments,
                                          SelectionSetContainer selectionSetContainer,
                                          TraverserContext<Node> traverserContext,
                                          GraphQLSchema schema) {
    this.typeNameIntrospectionField = typeNameIntrospectionField;
    this.field = field;
    this.fieldDefinition = fieldDefinition;
    this.parentType = parentType;
    this.unmodifiedParentType = unmodifiedParentType;
    this.parentEnvironment = parentEnvironment;
    this.arguments = arguments;
    this.selectionSetContainer = selectionSetContainer;
    this.traverserContext = traverserContext;
    this.schema = schema;
  }

  @Override
  public GraphQLSchema getSchema() {
    return schema;
  }

  @Override
  public Field getField() {
    return field;
  }

  @Override
  public GraphQLFieldDefinition getFieldDefinition() {
    return fieldDefinition;
  }

  @Override
  public GraphQLOutputType getParentType() {
    return parentType;
  }

  @Override
  public QueryVisitorFieldEnvironment getParentEnvironment() {
    return parentEnvironment;
  }

  @Override
  public Map<String, Object> getArguments() {
    return arguments;
  }

  @Override
  public SelectionSetContainer getSelectionSetContainer() {
    return selectionSetContainer;
  }

  @Override
  public GraphQLFieldsContainer getFieldsContainer() {
    if (isTypeNameIntrospectionField()) {
      throw new IllegalStateException("introspection field __typename doesn't have a fields container");
    }
    return unmodifiedParentType;
  }

  @Override
  public boolean isTypeNameIntrospectionField() {
    return typeNameIntrospectionField;
  }

  @Override
  public TraverserContext<Node> getTraverserContext() {
    return traverserContext;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    QueryVisitorFieldEnvironmentImpl that = (QueryVisitorFieldEnvironmentImpl)o;
    return typeNameIntrospectionField == that.typeNameIntrospectionField &&
           Objects.equals(field, that.field) &&
           Objects.equals(fieldDefinition, that.fieldDefinition) &&
           Objects.equals(parentType, that.parentType) &&
           Objects.equals(unmodifiedParentType, that.unmodifiedParentType) &&
           Objects.equals(arguments, that.arguments) &&
           Objects.equals(parentEnvironment, that.parentEnvironment) &&
           Objects.equals(selectionSetContainer, that.selectionSetContainer);
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + Objects.hashCode(typeNameIntrospectionField);
    result = 31 * result + Objects.hashCode(field);
    result = 31 * result + Objects.hashCode(fieldDefinition);
    result = 31 * result + Objects.hashCode(parentType);
    result = 31 * result + Objects.hashCode(unmodifiedParentType);
    result = 31 * result + Objects.hashCode(arguments);
    result = 31 * result + Objects.hashCode(parentEnvironment);
    result = 31 * result + Objects.hashCode(selectionSetContainer);
    return result;
  }

  @Override
  public String toString() {
    return "QueryVisitorFieldEnvironmentImpl{" +
           "field=" + field +
           ", fieldDefinition=" + fieldDefinition +
           ", parentType=" + parentType +
           ", arguments=" + arguments +
           '}';
  }
}
