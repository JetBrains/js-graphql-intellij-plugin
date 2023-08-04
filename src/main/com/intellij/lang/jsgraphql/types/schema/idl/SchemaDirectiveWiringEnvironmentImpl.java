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
import com.intellij.lang.jsgraphql.types.language.NamedNode;
import com.intellij.lang.jsgraphql.types.language.NodeParentTree;
import com.intellij.lang.jsgraphql.types.schema.*;
import com.intellij.lang.jsgraphql.types.util.FpKit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

@Internal
public class SchemaDirectiveWiringEnvironmentImpl<T extends GraphQLDirectiveContainer> implements SchemaDirectiveWiringEnvironment<T> {

  private final T element;
  private final Map<String, GraphQLDirective> directives;
  private final NodeParentTree<NamedNode<?>> nodeParentTree;
  private final TypeDefinitionRegistry typeDefinitionRegistry;
  private final Map<String, Object> context;
  private final GraphQLCodeRegistry.Builder codeRegistry;
  private final GraphqlElementParentTree elementParentTree;
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
    this.codeRegistry = parameters.getCodeRegistry();
    this.nodeParentTree = parameters.getNodeParentTree();
    this.elementParentTree = parameters.getElementParentTree();
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
  public NodeParentTree<NamedNode<?>> getNodeParentTree() {
    return nodeParentTree;
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
  public GraphQLCodeRegistry.Builder getCodeRegistry() {
    return codeRegistry;
  }

  @Override
  public GraphQLFieldsContainer getFieldsContainer() {
    return fieldsContainer;
  }

  @Override
  public GraphqlElementParentTree getElementParentTree() {
    return elementParentTree;
  }

  @Override
  public GraphQLFieldDefinition getFieldDefinition() {
    return fieldDefinition;
  }

  @Override
  public DataFetcher<?> getFieldDataFetcher() {
    assertNotNull(fieldDefinition, () -> "An output field must be in context to call this method");
    assertNotNull(fieldsContainer, () -> "An output field container must be in context to call this method");
    return codeRegistry.getDataFetcher(fieldsContainer, fieldDefinition);
  }

  @Override
  public GraphQLFieldDefinition setFieldDataFetcher(DataFetcher<?> newDataFetcher) {
    assertNotNull(fieldDefinition, () -> "An output field must be in context to call this method");
    assertNotNull(fieldsContainer, () -> "An output field container must be in context to call this method");

    FieldCoordinates coordinates = FieldCoordinates.coordinates(fieldsContainer, fieldDefinition);
    codeRegistry.dataFetcher(coordinates, newDataFetcher);
    return fieldDefinition;
  }
}
