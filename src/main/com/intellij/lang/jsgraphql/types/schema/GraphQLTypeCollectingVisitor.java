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

import com.intellij.lang.jsgraphql.types.AssertException;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.util.TraversalControl;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;
import com.intellij.openapi.diagnostic.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.String.format;

@Internal
public class GraphQLTypeCollectingVisitor extends GraphQLTypeVisitorStub {

  private static final Logger LOG = Logger.getInstance(GraphQLTypeCollectingVisitor.class);

  private final Map<String, GraphQLNamedType> result = new LinkedHashMap<>();

  public GraphQLTypeCollectingVisitor() {
  }

  @Override
  public TraversalControl visitGraphQLEnumType(GraphQLEnumType node, TraverserContext<GraphQLSchemaElement> context) {
    assertTypeUniqueness(node, result);
    save(node.getName(), node);
    return super.visitGraphQLEnumType(node, context);
  }

  @Override
  public TraversalControl visitGraphQLScalarType(GraphQLScalarType node, TraverserContext<GraphQLSchemaElement> context) {
    assertTypeUniqueness(node, result);
    save(node.getName(), node);
    return super.visitGraphQLScalarType(node, context);
  }

  @Override
  public TraversalControl visitGraphQLObjectType(GraphQLObjectType node, TraverserContext<GraphQLSchemaElement> context) {
    if (isNotTypeReference(node.getName())) {
      assertTypeUniqueness(node, result);
    }
    else {
      save(node.getName(), node);
    }
    return super.visitGraphQLObjectType(node, context);
  }

  @Override
  public TraversalControl visitGraphQLInputObjectType(GraphQLInputObjectType node, TraverserContext<GraphQLSchemaElement> context) {
    if (isNotTypeReference(node.getName())) {
      assertTypeUniqueness(node, result);
    }
    else {
      save(node.getName(), node);
    }
    return super.visitGraphQLInputObjectType(node, context);
  }

  @Override
  public TraversalControl visitGraphQLInterfaceType(GraphQLInterfaceType node, TraverserContext<GraphQLSchemaElement> context) {
    if (isNotTypeReference(node.getName())) {
      assertTypeUniqueness(node, result);
    }
    else {
      save(node.getName(), node);
    }

    return super.visitGraphQLInterfaceType(node, context);
  }

  @Override
  public TraversalControl visitGraphQLUnionType(GraphQLUnionType node, TraverserContext<GraphQLSchemaElement> context) {
    assertTypeUniqueness(node, result);
    save(node.getName(), node);
    return super.visitGraphQLUnionType(node, context);
  }

  private boolean isNotTypeReference(String name) {
    return result.containsKey(name) && !(result.get(name) instanceof GraphQLTypeReference);
  }

  private void save(String name, GraphQLNamedType type) {
    result.put(name, type);
  }


  /*
      From http://facebook.github.io/graphql/#sec-Type-System

         All types within a GraphQL schema must have unique names. No two provided types may have the same name.
         No provided type may have a name which conflicts with any built in types (including Scalar and Introspection types).

      Enforcing this helps avoid problems later down the track fo example https://github.com/graphql-java/graphql-java/issues/373
  */
  private void assertTypeUniqueness(GraphQLNamedType type, Map<String, GraphQLNamedType> result) {
    GraphQLType existingType = result.get(type.getName());
    // do we have an existing definition
    if (existingType != null) {
      // type references are ok
      if (!(existingType instanceof GraphQLTypeReference || type instanceof GraphQLTypeReference))
      // object comparison here is deliberate
      {
        if (existingType != type) {
          LOG.error(new AssertException(
            format("""
                     All types within a GraphQL schema must have unique names. No two provided types may have the same name.
                     No provided type may have a name which conflicts with any built in types (including Scalar and Introspection types).
                     You have redefined the type '%s' from being a '%s' to a '%s'""",
                   type.getName(), existingType.getClass().getSimpleName(), type.getClass().getSimpleName())));
        }
      }
    }
  }

  public Map<String, GraphQLNamedType> getResult() {
    return result;
  }
}
