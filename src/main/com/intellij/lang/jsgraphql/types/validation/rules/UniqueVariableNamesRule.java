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
import com.intellij.lang.jsgraphql.types.language.OperationDefinition;
import com.intellij.lang.jsgraphql.types.language.VariableDefinition;
import com.intellij.lang.jsgraphql.types.validation.AbstractRule;
import com.intellij.lang.jsgraphql.types.validation.ValidationContext;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorCollector;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Unique variable names
 * <p>
 * A GraphQL operation is only valid if all its variables are uniquely named.
 */
@Internal
public class UniqueVariableNamesRule extends AbstractRule {

  public UniqueVariableNamesRule(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
    super(validationContext, validationErrorCollector);
  }

  @Override
  public void checkOperationDefinition(OperationDefinition operationDefinition) {
    List<VariableDefinition> variableDefinitions = operationDefinition.getVariableDefinitions();
    if (variableDefinitions == null || variableDefinitions.size() <= 1) {
      return;
    }

    Set<String> variableNameList = new LinkedHashSet<>(variableDefinitions.size());


    for (VariableDefinition variableDefinition : variableDefinitions) {
      if (variableNameList.contains(variableDefinition.getName())) {
        addError(ValidationErrorType.DuplicateVariableName, variableDefinition.getSourceLocation(),
                 duplicateVariableNameMessage(variableDefinition.getName()));
      }
      else {
        variableNameList.add(variableDefinition.getName());
      }
    }
  }

  static String duplicateVariableNameMessage(String variableName) {
    return String.format("There can be only one variable named '%s'", variableName);
  }
}
