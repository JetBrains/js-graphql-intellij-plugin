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

import com.google.common.collect.ImmutableList;
import com.intellij.lang.jsgraphql.types.Internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Internal
public class GraphqlTypeComparators {

  /**
   * This sorts the list of {@link com.intellij.lang.jsgraphql.types.schema.GraphQLType} objects (by name) and allocates a new sorted
   * list back.
   *
   * @param <T>        the type of type
   * @param comparator the comparator to use
   * @param types      the types to sort
   * @return a new allocated list of sorted things
   */
  public static <T extends GraphQLSchemaElement> List<T> sortTypes(Comparator<? super GraphQLSchemaElement> comparator,
                                                                   Collection<T> types) {
    List<T> sorted = new ArrayList<>(types);
    sorted.sort(comparator);
    return ImmutableList.copyOf(sorted);
  }

  /**
   * Returns a comparator that laves {@link com.intellij.lang.jsgraphql.types.schema.GraphQLType} objects as they are
   *
   * @return a comparator that laves {@link com.intellij.lang.jsgraphql.types.schema.GraphQLType} objects as they are
   */
  public static Comparator<? super GraphQLSchemaElement> asIsOrder() {
    return (o1, o2) -> 0;
  }

  /**
   * Returns a comparator that compares {@link com.intellij.lang.jsgraphql.types.schema.GraphQLType} objects by ascending name
   *
   * @return a comparator that compares {@link com.intellij.lang.jsgraphql.types.schema.GraphQLType} objects by ascending name
   */
  public static Comparator<? super GraphQLSchemaElement> byNameAsc() {
    return Comparator.comparing(graphQLSchemaElement -> ((GraphQLNamedSchemaElement)graphQLSchemaElement).getName());
  }
}
