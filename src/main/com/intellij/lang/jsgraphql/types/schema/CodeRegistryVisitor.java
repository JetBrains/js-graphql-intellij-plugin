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

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.util.TraversalControl;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;

import static com.intellij.lang.jsgraphql.types.Assert.assertTrue;
import static com.intellij.lang.jsgraphql.types.schema.FieldCoordinates.coordinates;
import static com.intellij.lang.jsgraphql.types.util.TraversalControl.CONTINUE;

/**
 * This ensure that all fields have data fetchers and that unions and interfaces have type resolvers
 */
@Internal
class CodeRegistryVisitor extends GraphQLTypeVisitorStub {
  private final GraphQLCodeRegistry.Builder codeRegistry;

  CodeRegistryVisitor(GraphQLCodeRegistry.Builder codeRegistry) {
    this.codeRegistry = codeRegistry;
  }

  @Override
  public TraversalControl visitGraphQLFieldDefinition(GraphQLFieldDefinition node, TraverserContext<GraphQLSchemaElement> context) {
    GraphQLFieldsContainer parentContainerType = (GraphQLFieldsContainer)context.getParentContext().thisNode();
    DataFetcher<?> dataFetcher = node.getDataFetcher();
    if (dataFetcher != null) {
      FieldCoordinates coordinates = coordinates(parentContainerType, node);
      codeRegistry.dataFetcherIfAbsent(coordinates, dataFetcher);
    }

    return CONTINUE;
  }

  @Override
  public TraversalControl visitGraphQLInterfaceType(GraphQLInterfaceType node, TraverserContext<GraphQLSchemaElement> context) {
    TypeResolver typeResolver = node.getTypeResolver();
    if (typeResolver != null) {
      codeRegistry.typeResolverIfAbsent(node, typeResolver);
    }
    assertTrue(codeRegistry.getTypeResolver(node) != null,
               () -> String.format("You MUST provide a type resolver for the interface type '%s'", node.getName()));
    return CONTINUE;
  }

  @Override
  public TraversalControl visitGraphQLUnionType(GraphQLUnionType node, TraverserContext<GraphQLSchemaElement> context) {
    TypeResolver typeResolver = node.getTypeResolver();
    if (typeResolver != null) {
      codeRegistry.typeResolverIfAbsent(node, typeResolver);
    }
    assertTrue(codeRegistry.getTypeResolver(node) != null,
               () -> String.format("You MUST provide a type resolver for the union type '%s'", node.getName()));
    return CONTINUE;
  }
}
