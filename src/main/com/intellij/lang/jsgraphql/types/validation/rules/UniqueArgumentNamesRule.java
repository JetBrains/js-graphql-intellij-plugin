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
package com.intellij.lang.jsgraphql.types.validation.rules;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Argument;
import com.intellij.lang.jsgraphql.types.language.Directive;
import com.intellij.lang.jsgraphql.types.language.Field;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.validation.AbstractRule;
import com.intellij.lang.jsgraphql.types.validation.ValidationContext;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorCollector;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unique argument names
 * <p>
 * A GraphQL field or directive is only valid if all supplied arguments are uniquely named.
 */
@Internal
public class UniqueArgumentNamesRule extends AbstractRule {
  public UniqueArgumentNamesRule(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
    super(validationContext, validationErrorCollector);
  }

  @Override
  public void checkField(Field field) {
    if (field.getArguments() == null || field.getArguments().size() <= 1) {
      return;
    }

    Set<String> arguments = new HashSet<>();

    for (Argument argument : field.getArguments()) {
      if (arguments.contains(argument.getName())) {
        addError(ValidationErrorType.DuplicateArgumentNames, field.getSourceLocation(), duplicateArgumentNameMessage(argument.getName()));
      }
      else {
        arguments.add(argument.getName());
      }
    }
  }

  @Override
  public void checkDirective(Directive directive, List<Node> ancestors) {
    if (directive.getArguments() == null || directive.getArguments().size() <= 1) {
      return;
    }

    Set<String> arguments = new HashSet<>(directive.getArguments().size());

    for (Argument argument : directive.getArguments()) {
      if (arguments.contains(argument.getName())) {
        addError(ValidationErrorType.DuplicateArgumentNames, directive.getSourceLocation(),
                 duplicateArgumentNameMessage(argument.getName()));
      }
      else {
        arguments.add(argument.getName());
      }
    }
  }

  static String duplicateArgumentNameMessage(String argumentName) {
    return String.format("There can be only one argument named '%s'", argumentName);
  }
}
