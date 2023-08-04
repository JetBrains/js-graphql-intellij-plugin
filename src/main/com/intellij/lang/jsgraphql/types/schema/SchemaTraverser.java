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
import com.intellij.lang.jsgraphql.types.util.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.intellij.lang.jsgraphql.types.util.TraversalControl.CONTINUE;

@PublicApi
public class SchemaTraverser {


  private final Function<? super GraphQLSchemaElement, ? extends List<GraphQLSchemaElement>> getChildren;

  public SchemaTraverser(Function<? super GraphQLSchemaElement, ? extends List<GraphQLSchemaElement>> getChildren) {
    this.getChildren = getChildren;
  }

  public SchemaTraverser() {
    this(GraphQLSchemaElement::getChildren);
  }

  public TraverserResult depthFirst(GraphQLTypeVisitor graphQLTypeVisitor, GraphQLSchemaElement root) {
    return depthFirst(graphQLTypeVisitor, Collections.singletonList(root));
  }

  public TraverserResult depthFirst(final GraphQLTypeVisitor graphQLTypeVisitor, Collection<? extends GraphQLSchemaElement> roots) {
    return depthFirst(initTraverser(), new TraverserDelegateVisitor(graphQLTypeVisitor), roots);
  }

  public TraverserResult depthFirst(final GraphQLTypeVisitor graphQLTypeVisitor,
                                    Collection<? extends GraphQLSchemaElement> roots,
                                    Map<String, GraphQLNamedType> types) {
    Traverser<GraphQLSchemaElement> traverser = initTraverser().rootVar(SchemaTraverser.class, types);
    return depthFirst(traverser, new TraverserDelegateVisitor(graphQLTypeVisitor), roots);
  }

  public TraverserResult depthFirst(final Traverser<GraphQLSchemaElement> traverser,
                                    final TraverserDelegateVisitor traverserDelegateVisitor,
                                    Collection<? extends GraphQLSchemaElement> roots) {
    return doTraverse(traverser, roots, traverserDelegateVisitor);
  }

  private Traverser<GraphQLSchemaElement> initTraverser() {
    return Traverser.depthFirst(getChildren);
  }

  private TraverserResult doTraverse(Traverser<GraphQLSchemaElement> traverser,
                                     Collection<? extends GraphQLSchemaElement> roots,
                                     TraverserDelegateVisitor traverserDelegateVisitor) {
    return traverser.traverse(roots, traverserDelegateVisitor);
  }

  private static class TraverserDelegateVisitor implements TraverserVisitor<GraphQLSchemaElement> {
    private final GraphQLTypeVisitor before;

    TraverserDelegateVisitor(GraphQLTypeVisitor delegate) {
      this.before = delegate;
    }

    @Override
    public TraversalControl enter(TraverserContext<GraphQLSchemaElement> context) {
      return context.thisNode().accept(context, before);
    }

    @Override
    public TraversalControl leave(TraverserContext<GraphQLSchemaElement> context) {
      return CONTINUE;
    }

    @Override
    public TraversalControl backRef(TraverserContext<GraphQLSchemaElement> context) {
      return before.visitBackRef(context);
    }
  }
}
