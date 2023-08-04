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
import com.intellij.lang.jsgraphql.types.execution.TypeFromAST;
import com.intellij.lang.jsgraphql.types.language.OperationDefinition;
import com.intellij.lang.jsgraphql.types.language.VariableDefinition;
import com.intellij.lang.jsgraphql.types.language.VariableReference;
import com.intellij.lang.jsgraphql.types.schema.GraphQLInputType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil;
import com.intellij.lang.jsgraphql.types.validation.AbstractRule;
import com.intellij.lang.jsgraphql.types.validation.ValidationContext;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorCollector;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;

import java.util.LinkedHashMap;
import java.util.Map;

@Internal
public class VariableTypesMatchRule extends AbstractRule {

  final VariablesTypesMatcher variablesTypesMatcher;

  private Map<String, VariableDefinition> variableDefinitionMap;

  public VariableTypesMatchRule(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
    this(validationContext, validationErrorCollector, new VariablesTypesMatcher());
  }

  VariableTypesMatchRule(ValidationContext validationContext,
                         ValidationErrorCollector validationErrorCollector,
                         VariablesTypesMatcher variablesTypesMatcher) {
    super(validationContext, validationErrorCollector);
    setVisitFragmentSpreads(true);
    this.variablesTypesMatcher = variablesTypesMatcher;
  }

  @Override
  public void checkOperationDefinition(OperationDefinition operationDefinition) {
    variableDefinitionMap = new LinkedHashMap<>();
  }

  @Override
  public void checkVariableDefinition(VariableDefinition variableDefinition) {
    variableDefinitionMap.put(variableDefinition.getName(), variableDefinition);
  }

  @Override
  public void checkVariable(VariableReference variableReference) {
    VariableDefinition variableDefinition = variableDefinitionMap.get(variableReference.getName());
    if (variableDefinition == null) {
      return;
    }
    GraphQLType variableType = TypeFromAST.getTypeFromAST(getValidationContext().getSchema(), variableDefinition.getType());
    if (variableType == null) {
      return;
    }
    GraphQLInputType expectedType = getValidationContext().getInputType();
    if (expectedType == null) {
      // we must have a unknown variable say to not have a known type
      return;
    }
    if (!variablesTypesMatcher.doesVariableTypesMatch(variableType, variableDefinition.getDefaultValue(), expectedType)) {
      GraphQLType effectiveType = variablesTypesMatcher.effectiveType(variableType, variableDefinition.getDefaultValue());
      String message = String.format("Variable type '%s' doesn't match expected type '%s'",
                                     GraphQLTypeUtil.simplePrint(effectiveType),
                                     GraphQLTypeUtil.simplePrint(expectedType));
      addError(ValidationErrorType.VariableTypeMismatch, variableReference.getSourceLocation(), message);
    }
  }
}
