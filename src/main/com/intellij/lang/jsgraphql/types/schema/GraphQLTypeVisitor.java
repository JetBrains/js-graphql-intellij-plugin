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
import com.intellij.lang.jsgraphql.types.util.TreeTransformerUtil;

@PublicApi
public interface GraphQLTypeVisitor {
  TraversalControl visitGraphQLArgument(GraphQLArgument node, TraverserContext<GraphQLSchemaElement> context);

  TraversalControl visitGraphQLInterfaceType(GraphQLInterfaceType node, TraverserContext<GraphQLSchemaElement> context);

  TraversalControl visitGraphQLEnumType(GraphQLEnumType node, TraverserContext<GraphQLSchemaElement> context);

  TraversalControl visitGraphQLEnumValueDefinition(GraphQLEnumValueDefinition node, TraverserContext<GraphQLSchemaElement> context);

  TraversalControl visitGraphQLFieldDefinition(GraphQLFieldDefinition node, TraverserContext<GraphQLSchemaElement> context);

  TraversalControl visitGraphQLDirective(GraphQLDirective node, TraverserContext<GraphQLSchemaElement> context);

  TraversalControl visitGraphQLInputObjectField(GraphQLInputObjectField node, TraverserContext<GraphQLSchemaElement> context);

  TraversalControl visitGraphQLInputObjectType(GraphQLInputObjectType node, TraverserContext<GraphQLSchemaElement> context);

  TraversalControl visitGraphQLList(GraphQLList node, TraverserContext<GraphQLSchemaElement> context);

  TraversalControl visitGraphQLNonNull(GraphQLNonNull node, TraverserContext<GraphQLSchemaElement> context);

  TraversalControl visitGraphQLObjectType(GraphQLObjectType node, TraverserContext<GraphQLSchemaElement> context);

  TraversalControl visitGraphQLScalarType(GraphQLScalarType node, TraverserContext<GraphQLSchemaElement> context);

  TraversalControl visitGraphQLTypeReference(GraphQLTypeReference node, TraverserContext<GraphQLSchemaElement> context);

  TraversalControl visitGraphQLUnionType(GraphQLUnionType node, TraverserContext<GraphQLSchemaElement> context);

  /**
   * Called when a node is visited more than once within a context.  {@link com.intellij.lang.jsgraphql.types.util.TraverserContext#thisNode()} contains
   * the node
   *
   * @param context the traversal context
   * @return by default CONTINUE
   */
  default TraversalControl visitBackRef(TraverserContext<GraphQLSchemaElement> context) {
    return TraversalControl.CONTINUE;
  }

  // Marker interfaces
  default TraversalControl visitGraphQLModifiedType(GraphQLModifiedType node, TraverserContext<GraphQLSchemaElement> context) {
    throw new UnsupportedOperationException();
  }

  default TraversalControl visitGraphQLCompositeType(GraphQLCompositeType node, TraverserContext<GraphQLSchemaElement> context) {
    throw new UnsupportedOperationException();
  }

  default TraversalControl visitGraphQLDirectiveContainer(GraphQLDirectiveContainer node, TraverserContext<GraphQLSchemaElement> context) {
    throw new UnsupportedOperationException();
  }

  default TraversalControl visitGraphQLFieldsContainer(GraphQLFieldsContainer node, TraverserContext<GraphQLSchemaElement> context) {
    throw new UnsupportedOperationException();
  }

  default TraversalControl visitGraphQLInputFieldsContainer(GraphQLInputFieldsContainer node,
                                                            TraverserContext<GraphQLSchemaElement> context) {
    throw new UnsupportedOperationException();
  }

  default TraversalControl visitGraphQLInputType(GraphQLInputType node, TraverserContext<GraphQLSchemaElement> context) {
    throw new UnsupportedOperationException();
  }

  default TraversalControl visitGraphQLNullableType(GraphQLNullableType node, TraverserContext<GraphQLSchemaElement> context) {
    throw new UnsupportedOperationException();
  }

  default TraversalControl visitGraphQLOutputType(GraphQLOutputType node, TraverserContext<GraphQLSchemaElement> context) {
    throw new UnsupportedOperationException();
  }

  default TraversalControl visitGraphQLUnmodifiedType(GraphQLUnmodifiedType node, TraverserContext<GraphQLSchemaElement> context) {
    throw new UnsupportedOperationException();
  }

  /**
   * This helper method can be used to "change" a node when returning control from this visitor
   *
   * @param newChangedNode the new to be changed at this place
   * @param context        the current traversal context
   * @return this will always sent back TraversalControl.CONTINUE
   */
  default TraversalControl changedNode(GraphQLSchemaElement newChangedNode, TraverserContext<GraphQLSchemaElement> context) {
    return TreeTransformerUtil.changeNode(context, newChangedNode);
  }

  /**
   * This helper method can be used to "delete" the current node when returning control from this visitor
   *
   * @param context the current traversal context which is pointing to the current node to be deleted
   * @return this will always sent back TraversalControl.CONTINUE
   */
  default TraversalControl deletedNode(TraverserContext<GraphQLSchemaElement> context) {
    return TreeTransformerUtil.deleteNode(context);
  }

  /**
   * This helper method can be used to "insert a new node" AFTER the current node when returning control from this visitor
   *
   * @param toInsertAfter the new to be inserted AFTER this current code
   * @param context       the current traversal context
   * @return this will always sent back TraversalControl.CONTINUE
   */
  default TraversalControl insertAfterNode(GraphQLSchemaElement toInsertAfter, TraverserContext<GraphQLSchemaElement> context) {
    return TreeTransformerUtil.insertAfter(context, toInsertAfter);
  }

  /**
   * This helper method can be used to "insert a new node" BEFORE the current node when returning control from this visitor
   *
   * @param toInsertBefore the new to be inserted BEFORE this current code
   * @param context        the current traversal context
   * @return this will always sent back TraversalControl.CONTINUE
   */
  default TraversalControl insertBeforeNode(GraphQLSchemaElement toInsertBefore, TraverserContext<GraphQLSchemaElement> context) {
    return TreeTransformerUtil.insertBefore(context, toInsertBefore);
  }
}
