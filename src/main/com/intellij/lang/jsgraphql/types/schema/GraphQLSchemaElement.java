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

import java.util.Collections;
import java.util.List;

import static com.intellij.lang.jsgraphql.types.schema.SchemaElementChildrenContainer.newSchemaElementChildrenContainer;

/**
 * A GraphQLSchema can be viewed as a graph of GraphQLSchemaElement. Every node (vertex) of this graph implements
 * this interface.
 */
@PublicApi
public interface GraphQLSchemaElement {

  default List<GraphQLSchemaElement> getChildren() {
    return Collections.emptyList();
  }

  default SchemaElementChildrenContainer getChildrenWithTypeReferences() {
    return newSchemaElementChildrenContainer().build();
  }

  default GraphQLSchemaElement withNewChildren(SchemaElementChildrenContainer newChildren) {
    return this;
  }

  TraversalControl accept(TraverserContext<GraphQLSchemaElement> context, GraphQLTypeVisitor visitor);


  /**
   * No GraphQLSchemaElement implements `equals` because we need object identity
   * to treat a GraphQLSchema as an abstract graph.
   *
   * @param obj the reference object with which to compare.
   * @return {@code true} if this object is the same as the obj
   * argument; {@code false} otherwise.
   */
  boolean equals(Object obj);

  /**
   * No GraphQLSchemaElement implements `equals/hashCode` because we need object identity
   * to treat a GraphQLSchema as an abstract graph.
   *
   * @return a hash code value for this object.
   */
  int hashCode();
}
