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
import com.intellij.lang.jsgraphql.types.schema.GraphQLDirective;
import com.intellij.lang.jsgraphql.types.schema.GraphQLDirectiveContainer;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldsContainer;

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
   * @return the type registry
   */
  TypeDefinitionRegistry getRegistry();

  /**
   * @return a mpa that can be used by implementors to hold context during the SDL build process
   */
  Map<String, Object> getBuildContext();

  /**
   * @return a {@link GraphQLFieldsContainer} when the element is contained with a fields container
   */
  GraphQLFieldsContainer getFieldsContainer();

  /**
   * @return a {@link GraphQLFieldDefinition} when the element is as field or is contained within one
   */
  GraphQLFieldDefinition getFieldDefinition();
}
