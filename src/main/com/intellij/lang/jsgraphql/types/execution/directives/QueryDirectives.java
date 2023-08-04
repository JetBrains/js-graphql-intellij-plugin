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
package com.intellij.lang.jsgraphql.types.execution.directives;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.Field;
import com.intellij.lang.jsgraphql.types.schema.GraphQLDirective;

import java.util.List;
import java.util.Map;

/**
 * This gives you access to the immediate directives on a {@link com.intellij.lang.jsgraphql.types.execution.MergedField}.  This does not include directives on parent
 * fields or fragment containers.
 * <p>
 * Because a {@link com.intellij.lang.jsgraphql.types.execution.MergedField} can actually have multiple fields and hence
 * directives on each field instance its possible that there is more than one directive named "foo"
 * on the merged field.  How you decide which one to use is up to your code.
 * <p>
 * NOTE: A future version of the interface will try to add access to the inherited directives from
 * parent fields and fragments.  This proved to be a non trivial problem and hence we decide
 * to give access to immediate field directives and provide this holder interface so we can
 * add the other directives in the future
 *
 * @see com.intellij.lang.jsgraphql.types.execution.MergedField
 */
@PublicApi
public interface QueryDirectives {

  /**
   * This will return a map of the directives that are immediately on a merged field
   *
   * @return a map of all the directives immediately on this merged field
   */
  Map<String, List<GraphQLDirective>> getImmediateDirectivesByName();


  /**
   * This will return a list of the named directives that are immediately on this merged field.
   * <p>
   * Read above for why this is a list of directives and not just one
   *
   * @param directiveName the named directive
   * @return a list of the named directives that are immediately on this merged field
   */
  List<GraphQLDirective> getImmediateDirective(String directiveName);

  /**
   * This will return a map of the {@link com.intellij.lang.jsgraphql.types.language.Field}s inside a {@link com.intellij.lang.jsgraphql.types.execution.MergedField}
   * and the immediate directives that are on each specific field
   *
   * @return a map of all directives on each field inside this
   */
  Map<Field, List<GraphQLDirective>> getImmediateDirectivesByField();
}
