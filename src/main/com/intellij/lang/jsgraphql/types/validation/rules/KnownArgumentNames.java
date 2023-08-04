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
import com.intellij.lang.jsgraphql.types.schema.GraphQLArgument;
import com.intellij.lang.jsgraphql.types.schema.GraphQLDirective;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.types.validation.AbstractRule;
import com.intellij.lang.jsgraphql.types.validation.ValidationContext;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorCollector;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;


@Internal
public class KnownArgumentNames extends AbstractRule {

  public KnownArgumentNames(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
    super(validationContext, validationErrorCollector);
  }


  @Override
  public void checkArgument(Argument argument) {
    GraphQLDirective directiveDef = getValidationContext().getDirective();
    if (directiveDef != null) {
      GraphQLArgument directiveArgument = directiveDef.getArgument(argument.getName());
      if (directiveArgument == null) {
        String message = String.format("Unknown directive argument %s", argument.getName());
        addError(ValidationErrorType.UnknownDirective, argument.getSourceLocation(), message);
      }

      return;
    }

    GraphQLFieldDefinition fieldDef = getValidationContext().getFieldDef();
    if (fieldDef == null) return;
    GraphQLArgument fieldArgument = fieldDef.getArgument(argument.getName());
    if (fieldArgument == null) {
      String message = String.format("Unknown field argument %s", argument.getName());
      addError(ValidationErrorType.UnknownArgument, argument.getSourceLocation(), message);
    }
  }
}
