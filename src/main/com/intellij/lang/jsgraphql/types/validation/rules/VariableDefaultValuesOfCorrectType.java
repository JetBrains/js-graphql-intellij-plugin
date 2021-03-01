package com.intellij.lang.jsgraphql.types.validation.rules;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.VariableDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLInputType;
import com.intellij.lang.jsgraphql.types.validation.AbstractRule;
import com.intellij.lang.jsgraphql.types.validation.ValidationContext;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorCollector;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;

import static com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil.simplePrint;


@Internal
public class VariableDefaultValuesOfCorrectType extends AbstractRule {


    public VariableDefaultValuesOfCorrectType(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
        super(validationContext, validationErrorCollector);
    }


    @Override
    public void checkVariableDefinition(VariableDefinition variableDefinition) {
        GraphQLInputType inputType = getValidationContext().getInputType();
        if (inputType == null) return;
        if (variableDefinition.getDefaultValue() != null
                && !getValidationUtil().isValidLiteralValue(variableDefinition.getDefaultValue(), inputType, getValidationContext().getSchema())) {
            String message = String.format("Bad default value %s for type %s", variableDefinition.getDefaultValue(), simplePrint(inputType));
            addError(ValidationErrorType.BadValueForDefaultArg, variableDefinition.getSourceLocation(), message);
        }
    }
}
