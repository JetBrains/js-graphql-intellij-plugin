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
import com.intellij.lang.jsgraphql.types.util.NodeAdapter;
import com.intellij.lang.jsgraphql.types.util.NodeLocation;

import java.util.List;
import java.util.Map;

@Internal
public class GraphQLSchemaElementAdapter implements NodeAdapter<GraphQLSchemaElement> {

  public static final GraphQLSchemaElementAdapter SCHEMA_ELEMENT_ADAPTER = new GraphQLSchemaElementAdapter();

  private GraphQLSchemaElementAdapter() {

  }

  @Override
  public Map<String, List<GraphQLSchemaElement>> getNamedChildren(GraphQLSchemaElement node) {
    return node.getChildrenWithTypeReferences().getChildren();
  }

  @Override
  public GraphQLSchemaElement withNewChildren(GraphQLSchemaElement node, Map<String, List<GraphQLSchemaElement>> newChildren) {
    SchemaElementChildrenContainer childrenContainer =
      SchemaElementChildrenContainer.newSchemaElementChildrenContainer(newChildren).build();
    return node.withNewChildren(childrenContainer);
  }

  @Override
  public GraphQLSchemaElement removeChild(GraphQLSchemaElement node, NodeLocation location) {
    SchemaElementChildrenContainer children = node.getChildrenWithTypeReferences();
    SchemaElementChildrenContainer newChildren =
      children.transform(builder -> builder.removeChild(location.getName(), location.getIndex()));
    return node.withNewChildren(newChildren);
  }
}
