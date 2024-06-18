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
package com.intellij.lang.jsgraphql.types.validation;


import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Definition;
import com.intellij.lang.jsgraphql.types.language.Document;
import com.intellij.lang.jsgraphql.types.language.FragmentDefinition;
import com.intellij.lang.jsgraphql.types.schema.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Internal
public class ValidationContext {

  private final GraphQLSchema schema;
  private final Document document;

  private final TraversalContext traversalContext;
  private final Map<String, FragmentDefinition> fragmentDefinitionMap = new LinkedHashMap<>();


  public ValidationContext(GraphQLSchema schema, Document document) {
    this.schema = schema;
    this.document = document;
    this.traversalContext = new TraversalContext(schema);
    buildFragmentMap();
  }

  private void buildFragmentMap() {
    for (Definition definition : document.getDefinitions()) {
      if (!(definition instanceof FragmentDefinition fragmentDefinition)) continue;
      fragmentDefinitionMap.put(fragmentDefinition.getName(), fragmentDefinition);
    }
  }

  public TraversalContext getTraversalContext() {
    return traversalContext;
  }

  public GraphQLSchema getSchema() {
    return schema;
  }

  public Document getDocument() {
    return document;
  }

  public FragmentDefinition getFragment(String name) {
    return fragmentDefinitionMap.get(name);
  }

  public GraphQLCompositeType getParentType() {
    return traversalContext.getParentType();
  }

  public GraphQLInputType getInputType() {
    return traversalContext.getInputType();
  }

  public GraphQLFieldDefinition getFieldDef() {
    return traversalContext.getFieldDef();
  }

  public GraphQLDirective getDirective() {
    return traversalContext.getDirective();
  }

  public GraphQLArgument getArgument() {
    return traversalContext.getArgument();
  }

  public GraphQLOutputType getOutputType() {
    return traversalContext.getOutputType();
  }


  public List<String> getQueryPath() {
    return traversalContext.getQueryPath();
  }

  @Override
  public String toString() {
    return "ValidationContext{" + getQueryPath() + "}";
  }
}
