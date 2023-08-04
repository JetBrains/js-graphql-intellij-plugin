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


import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLUnexpectedTypeInspection;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Argument;
import com.intellij.lang.jsgraphql.types.schema.GraphQLArgument;
import com.intellij.lang.jsgraphql.types.validation.*;

@Internal
public class ArgumentsOfCorrectType extends AbstractRule {

  public ArgumentsOfCorrectType(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
    super(validationContext, validationErrorCollector);
  }

  @Override
  public void checkArgument(Argument argument) {
    GraphQLArgument fieldArgument = getValidationContext().getArgument();
    if (fieldArgument == null) {
      return;
    }
    ArgumentValidationUtil validationUtil = new ArgumentValidationUtil(argument);
    if (!validationUtil.isValidLiteralValue(argument.getValue(), fieldArgument.getType(), getValidationContext().getSchema())) {
      addError(
        ValidationError.newValidationError()
          .validationErrorType(ValidationErrorType.WrongType)
          .sourceLocation(argument.getSourceLocation())
          .description(validationUtil.getMessage())
          .extensions(validationUtil.getErrorExtensions())
          .node(argument)
          .inspectionClass(GraphQLUnexpectedTypeInspection.class)
      );
    }
  }
}
