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

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.NamedNode;
import com.intellij.lang.jsgraphql.types.language.NodeParentTree;
import com.intellij.lang.jsgraphql.types.schema.*;

import java.util.Map;

/**
 * {@link com.intellij.lang.jsgraphql.types.schema.idl.SchemaDirectiveWiring} is passed this object as parameters
 * when it builds out behaviour
 *
 * @param <T> the type of the object in play
 */
@PublicApi
public interface SchemaDirectiveWiringEnvironment<T extends GraphQLDirectiveContainer> {

  /**
   * @return the runtime element in play
   */
  T getElement();

  /**
   * This returns the directive that the {@link com.intellij.lang.jsgraphql.types.schema.idl.SchemaDirectiveWiring} was registered
   * against during calls to {@link com.intellij.lang.jsgraphql.types.schema.idl.RuntimeWiring.Builder#directive(String, SchemaDirectiveWiring)}
   * <p>
   * If this method of registration is not used (say because
   * {@link com.intellij.lang.jsgraphql.types.schema.idl.WiringFactory#providesSchemaDirectiveWiring(SchemaDirectiveWiringEnvironment)} or
   * {@link com.intellij.lang.jsgraphql.types.schema.idl.RuntimeWiring.Builder#directiveWiring(SchemaDirectiveWiring)} was used)
   * then this will return null.
   *
   * @return the directive that was registered under specific directive name or null if it was not
   * registered this way
   */
  GraphQLDirective getDirective();

  /**
   * @return all of the directives that are on the runtime element
   */
  Map<String, GraphQLDirective> getDirectives();

  /**
   * Returns a named directive or null
   *
   * @param directiveName the name of the directive
   * @return a named directive or null
   */
  GraphQLDirective getDirective(String directiveName);

  /**
   * Returns true if the named directive is present
   *
   * @param directiveName the name of the directive
   * @return true if the named directive is present
   */
  boolean containsDirective(String directiveName);

  /**
   * The node hierarchy depends on the element in question.  For example {@link com.intellij.lang.jsgraphql.types.language.ObjectTypeDefinition} nodes
   * have no parent, however a {@link com.intellij.lang.jsgraphql.types.language.Argument} might be on a {@link com.intellij.lang.jsgraphql.types.language.FieldDefinition}
   * which in turn might be on a {@link com.intellij.lang.jsgraphql.types.language.ObjectTypeDefinition} say
   *
   * @return hierarchical graphql language node information
   */
  NodeParentTree<NamedNode<?>> getNodeParentTree();

  /**
   * The type hierarchy depends on the element in question.  For example {@link com.intellij.lang.jsgraphql.types.schema.GraphQLObjectType} elements
   * have no parent, however a {@link com.intellij.lang.jsgraphql.types.schema.GraphQLArgument} might be on a {@link com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition}
   * which in turn might be on a {@link com.intellij.lang.jsgraphql.types.schema.GraphQLObjectType} say
   *
   * @return hierarchical graphql type information
   */
  GraphqlElementParentTree getElementParentTree();

  /**
   * @return the type registry
   */
  TypeDefinitionRegistry getRegistry();

  /**
   * @return a mpa that can be used by implementors to hold context during the SDL build process
   */
  Map<String, Object> getBuildContext();

  /**
   * @return a builder of the current code registry builder
   */
  GraphQLCodeRegistry.Builder getCodeRegistry();

  /**
   * @return a {@link com.intellij.lang.jsgraphql.types.schema.GraphQLFieldsContainer} when the element is contained with a fields container
   */
  GraphQLFieldsContainer getFieldsContainer();

  /**
   * @return a {@link GraphQLFieldDefinition} when the element is as field or is contained within one
   */
  GraphQLFieldDefinition getFieldDefinition();

  /**
   * This is useful as a shortcut to get the current fields existing data fetcher
   *
   * @return a {@link com.intellij.lang.jsgraphql.types.schema.DataFetcher} when the element is as field or is contained within one
   * @throws com.intellij.lang.jsgraphql.types.AssertException if there is not field in context at the time of the directive wiring callback
   */
  DataFetcher<?> getFieldDataFetcher();

  /**
   * This is a shortcut method to set a new data fetcher in the underlying {@link com.intellij.lang.jsgraphql.types.schema.GraphQLCodeRegistry}
   * against the current field.
   * <p>
   * Often schema directive wiring modify behaviour by wrapping or replacing data fetchers on
   * fields.  This method is a helper to make this easier in code.
   *
   * @param newDataFetcher the new data fetcher to use for this field
   * @return the environments {@link #getFieldDefinition()} to allow for a more fluent code style
   * @throws com.intellij.lang.jsgraphql.types.AssertException if there is not field in context at the time of the directive wiring callback
   */
  GraphQLFieldDefinition setFieldDataFetcher(DataFetcher<?> newDataFetcher);
}
