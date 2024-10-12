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
package com.intellij.lang.jsgraphql.types.schema.idl;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.schema.GraphQLDirective;
import com.intellij.lang.jsgraphql.types.schema.GraphQLDirectiveContainer;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldsContainer;
import com.intellij.lang.jsgraphql.types.util.FpKit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Internal
public class SchemaDirectiveWiringEnvironmentImpl<T extends GraphQLDirectiveContainer> implements SchemaDirectiveWiringEnvironment<T> {

  private final T element;
  private final Map<String, GraphQLDirective> directives;
  private final TypeDefinitionRegistry typeDefinitionRegistry;
  private final Map<String, Object> context;
  private final GraphQLFieldsContainer fieldsContainer;
  private final GraphQLFieldDefinition fieldDefinition;
  private final GraphQLDirective registeredDirective;

  public SchemaDirectiveWiringEnvironmentImpl(T element,
                                              List<GraphQLDirective> directives,
                                              GraphQLDirective registeredDirective,
                                              SchemaGeneratorDirectiveHelper.Parameters parameters) {
    this.element = element;
    this.registeredDirective = registeredDirective;
    this.typeDefinitionRegistry = parameters.getTypeRegistry();
    this.directives = FpKit.getByName(directives, GraphQLDirective::getName);
    this.context = parameters.getContext();
    this.fieldsContainer = parameters.getFieldsContainer();
    this.fieldDefinition = parameters.getFieldsDefinition();
  }

  @Override
  public T getElement() {
    return element;
  }

  @Override
  public GraphQLDirective getDirective() {
    return registeredDirective;
  }

  @Override
  public Map<String, GraphQLDirective> getDirectives() {
    return new LinkedHashMap<>(directives);
  }

  @Override
  public GraphQLDirective getDirective(String directiveName) {
    return directives.get(directiveName);
  }

  @Override
  public boolean containsDirective(String directiveName) {
    return directives.containsKey(directiveName);
  }

  @Override
  public TypeDefinitionRegistry getRegistry() {
    return typeDefinitionRegistry;
  }

  @Override
  public Map<String, Object> getBuildContext() {
    return context;
  }

  @Override
  public GraphQLFieldsContainer getFieldsContainer() {
    return fieldsContainer;
  }

  @Override
  public GraphQLFieldDefinition getFieldDefinition() {
    return fieldDefinition;
  }
}
