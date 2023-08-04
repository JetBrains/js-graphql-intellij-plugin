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
package com.intellij.lang.jsgraphql.types.schema;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.util.TraversalControl;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;

import static com.intellij.lang.jsgraphql.types.util.TraversalControl.CONTINUE;

/**
 * Base implementation of {@link GraphQLTypeVisitor} for convenience.
 * Overwrite only required methods and/or {@link #visitGraphQLType(GraphQLSchemaElement, TraverserContext)} as default fallback.
 */
@PublicApi
public class GraphQLTypeVisitorStub implements GraphQLTypeVisitor {
  @Override
  public TraversalControl visitGraphQLArgument(GraphQLArgument node, TraverserContext<GraphQLSchemaElement> context) {
    return visitGraphQLType(node, context);
  }

  @Override
  public TraversalControl visitGraphQLInterfaceType(GraphQLInterfaceType node, TraverserContext<GraphQLSchemaElement> context) {
    return visitGraphQLType(node, context);
  }

  @Override
  public TraversalControl visitGraphQLEnumType(GraphQLEnumType node, TraverserContext<GraphQLSchemaElement> context) {
    return visitGraphQLType(node, context);
  }

  @Override
  public TraversalControl visitGraphQLEnumValueDefinition(GraphQLEnumValueDefinition node, TraverserContext<GraphQLSchemaElement> context) {
    return visitGraphQLType(node, context);
  }

  @Override
  public TraversalControl visitGraphQLFieldDefinition(GraphQLFieldDefinition node, TraverserContext<GraphQLSchemaElement> context) {
    return visitGraphQLType(node, context);
  }

  @Override
  public TraversalControl visitGraphQLDirective(GraphQLDirective node, TraverserContext<GraphQLSchemaElement> context) {
    return visitGraphQLType(node, context);
  }

  @Override
  public TraversalControl visitGraphQLInputObjectField(GraphQLInputObjectField node, TraverserContext<GraphQLSchemaElement> context) {
    return visitGraphQLType(node, context);
  }

  @Override
  public TraversalControl visitGraphQLInputObjectType(GraphQLInputObjectType node, TraverserContext<GraphQLSchemaElement> context) {
    return visitGraphQLType(node, context);
  }

  @Override
  public TraversalControl visitGraphQLList(GraphQLList node, TraverserContext<GraphQLSchemaElement> context) {
    return visitGraphQLType(node, context);
  }

  @Override
  public TraversalControl visitGraphQLNonNull(GraphQLNonNull node, TraverserContext<GraphQLSchemaElement> context) {
    return visitGraphQLType(node, context);
  }

  @Override
  public TraversalControl visitGraphQLObjectType(GraphQLObjectType node, TraverserContext<GraphQLSchemaElement> context) {
    return visitGraphQLType(node, context);
  }

  @Override
  public TraversalControl visitGraphQLScalarType(GraphQLScalarType node, TraverserContext<GraphQLSchemaElement> context) {
    return visitGraphQLType(node, context);
  }

  @Override
  public TraversalControl visitGraphQLTypeReference(GraphQLTypeReference node, TraverserContext<GraphQLSchemaElement> context) {
    return visitGraphQLType(node, context);
  }

  @Override
  public TraversalControl visitGraphQLUnionType(GraphQLUnionType node, TraverserContext<GraphQLSchemaElement> context) {
    return visitGraphQLType(node, context);
  }

  protected TraversalControl visitGraphQLType(GraphQLSchemaElement node, TraverserContext<GraphQLSchemaElement> context) {
    return CONTINUE;
  }
}
