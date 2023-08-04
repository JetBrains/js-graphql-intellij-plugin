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
package com.intellij.lang.jsgraphql.types.language;


import com.google.common.collect.ImmutableMap;
import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.List;
import java.util.Map;

import static com.intellij.lang.jsgraphql.types.language.NodeUtil.allDirectivesByName;
import static java.util.Collections.emptyList;

/**
 * Represents a language node that can contain Directives.  Directives can be repeatable and (by default) non repeatable.
 * <p>
 * There are access methods here that get the two different types.
 *
 * @see com.intellij.lang.jsgraphql.types.language.DirectiveDefinition
 * @see DirectiveDefinition#isRepeatable()
 */
@PublicApi
public interface DirectivesContainer<T extends DirectivesContainer> extends Node<T> {

  /**
   * This will return a list of all the directives that have been put on {@link com.intellij.lang.jsgraphql.types.language.Node} as a flat list, which may contain repeatable
   * and non repeatable directives.
   *
   * @return a list of all the directives associated with this Node
   */
  List<Directive> getDirectives();

  /**
   * This will return a Map of the all directives that are associated with a {@link com.intellij.lang.jsgraphql.types.language.Node}, including both repeatable and non repeatable directives.
   *
   * @return a map of all directives by directive name
   */
  default Map<String, List<Directive>> getDirectivesByName() {
    return ImmutableMap.copyOf(allDirectivesByName(getDirectives()));
  }

  /**
   * Returns all of the directives with the provided name, including repeatable and non repeatable directives.
   *
   * @param directiveName the name of the directives to retrieve
   * @return the directives or empty list if there is not one with that name
   */
  default List<Directive> getDirectives(String directiveName) {
    return getDirectivesByName().getOrDefault(directiveName, emptyList());
  }

  /**
   * This returns true if the AST node contains one or more directives by the specified name
   *
   * @param directiveName the name ot check
   * @return true if the AST node contains one or more directives by the specified name
   */
  default boolean hasDirective(String directiveName) {
    return !getDirectives(directiveName).isEmpty();
  }
}
