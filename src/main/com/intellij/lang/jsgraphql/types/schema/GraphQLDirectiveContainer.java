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

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

/**
 * Represents a graphql runtime type that can have {@link com.intellij.lang.jsgraphql.types.schema.GraphQLDirective}'s.
 * <p>
 * Directives can be repeatable and (by default) non repeatable.
 * <p>
 * There are access methods here that get the two different types.
 *
 * @see com.intellij.lang.jsgraphql.types.language.DirectiveDefinition
 * @see com.intellij.lang.jsgraphql.types.language.DirectiveDefinition#isRepeatable()
 */
@PublicApi
public interface GraphQLDirectiveContainer extends GraphQLNamedSchemaElement {

  /**
   * This will return a list of all the directives that have been put on {@link com.intellij.lang.jsgraphql.types.schema.GraphQLNamedSchemaElement} as a flat list, which may contain repeatable
   * and non repeatable directives.
   *
   * @return a list of all the directives associated with this named schema element
   */
  List<GraphQLDirective> getDirectives();

  /**
   * This will return a Map of the non repeatable directives that are associated with a {@link com.intellij.lang.jsgraphql.types.schema.GraphQLNamedSchemaElement}.  Any repeatable directives
   * will be filtered out of this map.
   *
   * @return a map of non repeatable directives by directive name.
   */
  Map<String, GraphQLDirective> getDirectivesByName();

  /**
   * This will return a Map of the all directives that are associated with a {@link com.intellij.lang.jsgraphql.types.schema.GraphQLNamedSchemaElement}, including both
   * repeatable and non repeatable directives.
   *
   * @return a map of all directives by directive name
   */
  Map<String, List<GraphQLDirective>> getAllDirectivesByName();

  /**
   * Returns a non repeatable directive with the provided name.  This will throw a {@link com.intellij.lang.jsgraphql.types.AssertException} if
   * the directive is a repeatable directive that has more then one instance.
   *
   * @param directiveName the name of the directive to retrieve
   * @return the directive or null if there is not one with that name
   */
  GraphQLDirective getDirective(String directiveName);

  /**
   * Returns all of the directives with the provided name, including repeatable and non repeatable directives.
   *
   * @param directiveName the name of the directives to retrieve
   * @return the directives or empty list if there is not one with that name
   */
  default List<GraphQLDirective> getDirectives(String directiveName) {
    return getAllDirectivesByName().getOrDefault(directiveName, emptyList());
  }
}
