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

    VariableTypesMatchRule(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector, VariablesTypesMatcher variablesTypesMatcher) {
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
