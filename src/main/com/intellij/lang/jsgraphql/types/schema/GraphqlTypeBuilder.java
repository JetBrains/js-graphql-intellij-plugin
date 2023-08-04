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

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.intellij.lang.jsgraphql.types.util.FpKit.valuesToList;

@Internal
public abstract class GraphqlTypeBuilder {

  protected String name;
  protected String description;
  protected GraphqlTypeComparatorRegistry comparatorRegistry = GraphqlTypeComparatorRegistry.AS_IS_REGISTRY;

  GraphqlTypeBuilder name(String name) {
    this.name = name;
    return this;
  }

  GraphqlTypeBuilder description(String description) {
    this.description = description;
    return this;
  }

  GraphqlTypeBuilder comparatorRegistry(GraphqlTypeComparatorRegistry comparatorRegistry) {
    this.comparatorRegistry = comparatorRegistry;
    return this;
  }


  <T extends GraphQLSchemaElement> List<T> sort(Map<String, T> types,
                                                Class<? extends GraphQLSchemaElement> parentType,
                                                Class<? extends GraphQLSchemaElement> elementType) {
    return sort(valuesToList(types), parentType, elementType);
  }

  <T extends GraphQLSchemaElement> List<T> sort(List<T> types,
                                                Class<? extends GraphQLSchemaElement> parentType,
                                                Class<? extends GraphQLSchemaElement> elementType) {
    Comparator<? super GraphQLSchemaElement> comparator = getComparatorImpl(comparatorRegistry, parentType, elementType);
    return ImmutableList.copyOf(GraphqlTypeComparators.sortTypes(comparator, types));
  }

  Comparator<? super GraphQLSchemaElement> getComparator(Class<? extends GraphQLSchemaElement> parentType,
                                                         Class<? extends GraphQLNamedSchemaElement> elementType) {
    return getComparatorImpl(comparatorRegistry, parentType, elementType);
  }

  private static Comparator<? super GraphQLSchemaElement> getComparatorImpl(GraphqlTypeComparatorRegistry comparatorRegistry,
                                                                            Class<? extends GraphQLSchemaElement> parentType,
                                                                            Class<? extends GraphQLSchemaElement> elementType) {
    GraphqlTypeComparatorEnvironment environment = GraphqlTypeComparatorEnvironment.newEnvironment()
      .parentType(parentType)
      .elementType(elementType)
      .build();
    return comparatorRegistry.getComparator(environment);
  }
}
