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
import com.intellij.lang.jsgraphql.types.language.VariableReference;
import com.intellij.lang.jsgraphql.types.validation.AbstractRule;
import com.intellij.lang.jsgraphql.types.validation.ValidationContext;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorCollector;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Internal
public class NoUnusedVariables extends AbstractRule {

  private final List<VariableDefinition> variableDefinitions = new ArrayList<>();
  private final Set<String> usedVariables = new LinkedHashSet<>();

  public NoUnusedVariables(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
    super(validationContext, validationErrorCollector);
    setVisitFragmentSpreads(true);
  }

  @Override
  public void leaveOperationDefinition(OperationDefinition operationDefinition) {
    for (VariableDefinition variableDefinition : variableDefinitions) {
      if (!usedVariables.contains(variableDefinition.getName())) {
        String message = String.format("Unused variable %s", variableDefinition.getName());
        addError(ValidationErrorType.UnusedVariable, variableDefinition.getSourceLocation(), message);
      }
    }
  }

  @Override
  public void checkOperationDefinition(OperationDefinition operationDefinition) {
    usedVariables.clear();
    variableDefinitions.clear();
  }

  @Override
  public void checkVariableDefinition(VariableDefinition variableDefinition) {
    variableDefinitions.add(variableDefinition);
  }

  @Override
  public void checkVariable(VariableReference variableReference) {
    usedVariables.add(variableReference.getName());
  }
}
