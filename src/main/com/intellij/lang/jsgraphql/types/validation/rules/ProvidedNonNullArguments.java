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
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.GraphQLArgument;
import com.intellij.lang.jsgraphql.types.schema.GraphQLDirective;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.types.validation.AbstractRule;
import com.intellij.lang.jsgraphql.types.validation.ValidationContext;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorCollector;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil.isNonNull;

@Internal
public class ProvidedNonNullArguments extends AbstractRule {

  public ProvidedNonNullArguments(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
    super(validationContext, validationErrorCollector);
  }

  @Override
  public void checkField(Field field) {
    GraphQLFieldDefinition fieldDef = getValidationContext().getFieldDef();
    if (fieldDef == null) return;
    Map<String, Argument> argumentMap = argumentMap(field.getArguments());

    for (GraphQLArgument graphQLArgument : fieldDef.getArguments()) {
      Argument argument = argumentMap.get(graphQLArgument.getName());
      boolean nonNullType = isNonNull(graphQLArgument.getType());
      boolean noDefaultValue = graphQLArgument.getDefaultValue() == null;
      if (argument == null && nonNullType && noDefaultValue) {
        String message = String.format("Missing field argument '%s'", graphQLArgument.getName());
        addError(ValidationErrorType.MissingFieldArgument, field.getSourceLocation(), message);
      }

      if (argument != null) {
        Value value = argument.getValue();
        if ((value == null || value instanceof NullValue) && nonNullType && noDefaultValue) {
          String message = String.format("null value for non-null field argument '%s'", graphQLArgument.getName());
          addError(ValidationErrorType.NullValueForNonNullArgument, field.getSourceLocation(), message);
        }
      }
    }
  }


  @Override
  public void checkDirective(Directive directive, List<Node> ancestors) {
    GraphQLDirective graphQLDirective = getValidationContext().getDirective();
    if (graphQLDirective == null) return;
    Map<String, Argument> argumentMap = argumentMap(directive.getArguments());

    for (GraphQLArgument graphQLArgument : graphQLDirective.getArguments()) {
      Argument argument = argumentMap.get(graphQLArgument.getName());
      boolean nonNullType = isNonNull(graphQLArgument.getType());
      boolean noDefaultValue = graphQLArgument.getDefaultValue() == null;
      if (argument == null && nonNullType && noDefaultValue) {
        String message = String.format("Missing directive argument '%s'", graphQLArgument.getName());
        addError(ValidationErrorType.MissingDirectiveArgument, directive.getSourceLocation(), message);
      }
    }
  }

  private Map<String, Argument> argumentMap(List<Argument> arguments) {
    Map<String, Argument> result = new LinkedHashMap<>();
    for (Argument argument : arguments) {
      result.put(argument.getName(), argument);
    }
    return result;
  }
}
