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
import com.intellij.lang.jsgraphql.types.language.FragmentDefinition;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;

import java.util.Objects;

@Internal
public class QueryVisitorFragmentDefinitionEnvironmentImpl implements QueryVisitorFragmentDefinitionEnvironment {

  private final FragmentDefinition fragmentDefinition;
  private final TraverserContext<Node> traverserContext;
  private final GraphQLSchema schema;


  public QueryVisitorFragmentDefinitionEnvironmentImpl(FragmentDefinition fragmentDefinition,
                                                       TraverserContext<Node> traverserContext,
                                                       GraphQLSchema schema) {
    this.fragmentDefinition = fragmentDefinition;
    this.traverserContext = traverserContext;
    this.schema = schema;
  }

  @Override
  public GraphQLSchema getSchema() {
    return schema;
  }

  @Override
  public FragmentDefinition getFragmentDefinition() {
    return fragmentDefinition;
  }

  @Override
  public TraverserContext<Node> getTraverserContext() {
    return traverserContext;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QueryVisitorFragmentDefinitionEnvironmentImpl that = (QueryVisitorFragmentDefinitionEnvironmentImpl)o;
    return Objects.equals(fragmentDefinition, that.fragmentDefinition);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(fragmentDefinition);
  }

  @Override
  public String toString() {
    return "QueryVisitorFragmentDefinitionEnvironmentImpl{" +
           "fragmentDefinition=" + fragmentDefinition +
           '}';
  }
}

