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
