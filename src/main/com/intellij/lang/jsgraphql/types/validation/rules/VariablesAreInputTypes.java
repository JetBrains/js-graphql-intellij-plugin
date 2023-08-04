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
import com.intellij.lang.jsgraphql.types.language.TypeName;
import com.intellij.lang.jsgraphql.types.language.VariableDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import com.intellij.lang.jsgraphql.types.validation.AbstractRule;
import com.intellij.lang.jsgraphql.types.validation.ValidationContext;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorCollector;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;

import static com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil.isInput;

@Internal
public class VariablesAreInputTypes extends AbstractRule {

  public VariablesAreInputTypes(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
    super(validationContext, validationErrorCollector);
  }

  @Override
  public void checkVariableDefinition(VariableDefinition variableDefinition) {
    TypeName unmodifiedAstType = getValidationUtil().getUnmodifiedType(variableDefinition.getType());

    GraphQLType type = getValidationContext().getSchema().getType(unmodifiedAstType.getName());
    if (type == null) return;
    if (!isInput(type)) {
      String message = "Wrong type for a variable";
      addError(ValidationErrorType.NonInputTypeOnVariable, variableDefinition.getSourceLocation(), message);
    }
  }
}
