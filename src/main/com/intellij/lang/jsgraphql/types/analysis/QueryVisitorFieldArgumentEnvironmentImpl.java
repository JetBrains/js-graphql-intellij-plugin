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
import com.intellij.lang.jsgraphql.types.language.Argument;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.schema.GraphQLArgument;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;

import java.util.Map;

@Internal
public class QueryVisitorFieldArgumentEnvironmentImpl implements QueryVisitorFieldArgumentEnvironment {

  private final GraphQLFieldDefinition fieldDefinition;
  private final Argument argument;
  private final GraphQLArgument graphQLArgument;
  private final Object argumentValue;
  private final Map<String, Object> variables;
  private final QueryVisitorFieldEnvironment parentEnvironment;
  private final TraverserContext<Node> traverserContext;
  private final GraphQLSchema schema;

  public QueryVisitorFieldArgumentEnvironmentImpl(GraphQLFieldDefinition fieldDefinition,
                                                  Argument argument,
                                                  GraphQLArgument graphQLArgument,
                                                  Object argumentValue,
                                                  Map<String, Object> variables,
                                                  QueryVisitorFieldEnvironment parentEnvironment,
                                                  TraverserContext<Node> traverserContext,
                                                  GraphQLSchema schema) {
    this.fieldDefinition = fieldDefinition;
    this.argument = argument;
    this.graphQLArgument = graphQLArgument;
    this.argumentValue = argumentValue;
    this.variables = variables;
    this.parentEnvironment = parentEnvironment;
    this.traverserContext = traverserContext;
    this.schema = schema;
  }

  @Override
  public GraphQLSchema getSchema() {
    return schema;
  }

  @Override
  public Argument getArgument() {
    return argument;
  }

  public GraphQLFieldDefinition getFieldDefinition() {
    return fieldDefinition;
  }

  @Override
  public GraphQLArgument getGraphQLArgument() {
    return graphQLArgument;
  }

  @Override
  public Object getArgumentValue() {
    return argumentValue;
  }

  @Override
  public Map<String, Object> getVariables() {
    return variables;
  }

  @Override
  public QueryVisitorFieldEnvironment getParentEnvironment() {
    return parentEnvironment;
  }

  @Override
  public TraverserContext<Node> getTraverserContext() {
    return traverserContext;
  }

  @Override
  public String toString() {
    return "QueryVisitorFieldArgumentEnvironmentImpl{" +
           "fieldDefinition=" + fieldDefinition +
           ", argument=" + argument +
           ", graphQLArgument=" + graphQLArgument +
           ", argumentValue=" + argumentValue +
           ", variables=" + variables +
           ", traverserContext=" + traverserContext +
           ", schema=" + schema +
           '}';
  }
}
