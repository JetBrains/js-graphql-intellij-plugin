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
package com.intellij.lang.jsgraphql.types.execution;

import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.VisibleForTesting;
import com.intellij.lang.jsgraphql.types.language.Directive;
import com.intellij.lang.jsgraphql.types.language.NodeUtil;

import java.util.List;
import java.util.Map;

import static com.intellij.lang.jsgraphql.types.Directives.IncludeDirective;
import static com.intellij.lang.jsgraphql.types.Directives.SkipDirective;

@Internal
public class ConditionalNodes {

  @VisibleForTesting
  ValuesResolver valuesResolver = new ValuesResolver();

  public boolean shouldInclude(Map<String, Object> variables, List<Directive> directives) {
    boolean skip = getDirectiveResult(variables, directives, SkipDirective.getName(), false);
    boolean include = getDirectiveResult(variables, directives, IncludeDirective.getName(), true);
    return !skip && include;
  }

  private boolean getDirectiveResult(Map<String, Object> variables,
                                     List<Directive> directives,
                                     String directiveName,
                                     boolean defaultValue) {
    Directive foundDirective = NodeUtil.findNodeByName(directives, directiveName);
    if (foundDirective != null) {
      Map<String, Object> argumentValues =
        valuesResolver.getArgumentValues(SkipDirective.getArguments(), foundDirective.getArguments(), variables);
      Object flag = argumentValues.get("if");
      Assert.assertTrue(flag instanceof Boolean,
                        () -> String.format("The '%s' directive MUST have a value for the 'if' argument", directiveName));
      return (Boolean)flag;
    }
    return defaultValue;
  }
}
