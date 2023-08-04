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
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.util.TraversalControl;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;

import static com.intellij.lang.jsgraphql.types.Assert.assertValidName;

/**
 * A special type to allow a object/interface types to reference itself. It's replaced with the real type
 * object when the schema is built.
 */
@PublicApi
public class GraphQLTypeReference implements GraphQLNamedOutputType, GraphQLNamedInputType {

  /**
   * A factory method for creating type references so that when used with static imports allows
   * more readable code such as
   * {@code .type(typeRef(GraphQLString)) }
   *
   * @param typeName the name of the type to reference
   * @return a GraphQLTypeReference of that named type
   */
  public static GraphQLTypeReference typeRef(String typeName) {
    return new GraphQLTypeReference(typeName);
  }

  private final String name;

  public GraphQLTypeReference(String name) {
    assertValidName(name);
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public Node getDefinition() {
    return null;
  }

  @Override
  public String toString() {
    return "GraphQLTypeReference{" +
           "name='" + name + '\'' +
           '}';
  }

  @Override
  public TraversalControl accept(TraverserContext<GraphQLSchemaElement> context, GraphQLTypeVisitor visitor) {
    return visitor.visitGraphQLTypeReference(this, context);
  }
}
