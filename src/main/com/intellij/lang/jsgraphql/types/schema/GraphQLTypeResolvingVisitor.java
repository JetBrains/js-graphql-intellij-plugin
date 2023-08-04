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

import java.util.Map;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.map;
import static com.intellij.lang.jsgraphql.types.util.TraversalControl.CONTINUE;

@Internal
public class GraphQLTypeResolvingVisitor extends GraphQLTypeVisitorStub {
  protected final Map<String, GraphQLNamedType> typeMap;

  public GraphQLTypeResolvingVisitor(Map<String, GraphQLNamedType> typeMap) {
    this.typeMap = typeMap;
  }

  @Override
  public TraversalControl visitGraphQLObjectType(GraphQLObjectType node, TraverserContext<GraphQLSchemaElement> context) {

    node.replaceInterfaces(map(node.getInterfaces(), type -> (GraphQLNamedOutputType)typeMap.get(type.getName())));
    return super.visitGraphQLObjectType(node, context);
  }

  @Override
  public TraversalControl visitGraphQLInterfaceType(GraphQLInterfaceType node, TraverserContext<GraphQLSchemaElement> context) {
    node.replaceInterfaces(map(node.getInterfaces(), type -> (GraphQLNamedOutputType)typeMap.get(type.getName())));
    return super.visitGraphQLInterfaceType(node, context);
  }


  @Override
  public TraversalControl visitGraphQLUnionType(GraphQLUnionType node, TraverserContext<GraphQLSchemaElement> context) {

    node.replaceTypes(map(node.getTypes(), type -> (GraphQLNamedOutputType)typeMap.get(type.getName())));
    return super.visitGraphQLUnionType(node, context);
  }

  @Override
  public TraversalControl visitGraphQLTypeReference(GraphQLTypeReference node, TraverserContext<GraphQLSchemaElement> context) {
    return handleTypeReference(node, context);
  }

  public TraversalControl handleTypeReference(GraphQLTypeReference node, TraverserContext<GraphQLSchemaElement> context) {
    final GraphQLType resolvedType = typeMap.get(node.getName());
    assertNotNull(resolvedType, () -> String.format("type %s not found in schema", node.getName()));
    context.getParentContext().thisNode().accept(context, new TypeRefResolvingVisitor(resolvedType));
    return CONTINUE;
  }

  @Override
  public TraversalControl visitBackRef(TraverserContext<GraphQLSchemaElement> context) {
    GraphQLSchemaElement schemaElement = context.thisNode();
    if (schemaElement instanceof GraphQLTypeReference) {
      return handleTypeReference((GraphQLTypeReference)schemaElement, context);
    }
    return CONTINUE;
  }

  private static class TypeRefResolvingVisitor extends GraphQLTypeVisitorStub {
    protected final GraphQLType resolvedType;

    TypeRefResolvingVisitor(GraphQLType resolvedType) {
      this.resolvedType = resolvedType;
    }

    @Override
    public TraversalControl visitGraphQLFieldDefinition(GraphQLFieldDefinition node, TraverserContext<GraphQLSchemaElement> context) {
      node.replaceType((GraphQLOutputType)resolvedType);
      return super.visitGraphQLFieldDefinition(node, context);
    }

    @Override
    public TraversalControl visitGraphQLArgument(GraphQLArgument node, TraverserContext<GraphQLSchemaElement> context) {
      node.replaceType((GraphQLInputType)resolvedType);
      return super.visitGraphQLArgument(node, context);
    }

    @Override
    public TraversalControl visitGraphQLInputObjectField(GraphQLInputObjectField node, TraverserContext<GraphQLSchemaElement> context) {
      node.replaceType((GraphQLInputType)resolvedType);
      return super.visitGraphQLInputObjectField(node, context);
    }

    @Override
    public TraversalControl visitGraphQLList(GraphQLList node, TraverserContext<GraphQLSchemaElement> context) {
      node.replaceType(resolvedType);
      return super.visitGraphQLList(node, context);
    }

    @Override
    public TraversalControl visitGraphQLNonNull(GraphQLNonNull node, TraverserContext<GraphQLSchemaElement> context) {
      node.replaceType(resolvedType);
      return super.visitGraphQLNonNull(node, context);
    }
  }
}
