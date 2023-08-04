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

import com.intellij.lang.jsgraphql.types.Internal;

/**
 * This class is the guts of a property data fetcher and also used in AST code to turn
 * in memory java objects into AST elements
 */
@Internal
public class PropertyDataFetcherHelper {

  private static final PropertyFetchingImpl impl = new PropertyFetchingImpl(DataFetchingEnvironment.class);

  public static Object getPropertyValue(String propertyName, Object object, GraphQLType graphQLType) {
    return impl.getPropertyValue(propertyName, object, graphQLType, null);
  }

  public static Object getPropertyValue(String propertyName, Object object, GraphQLType graphQLType, DataFetchingEnvironment environment) {
    return impl.getPropertyValue(propertyName, object, graphQLType, environment);
  }

  public static void clearReflectionCache() {
    impl.clearReflectionCache();
  }

  public static boolean setUseSetAccessible(boolean flag) {
    return impl.setUseSetAccessible(flag);
  }

  public static boolean setUseNegativeCache(boolean flag) {
    return impl.setUseNegativeCache(flag);
  }
}
