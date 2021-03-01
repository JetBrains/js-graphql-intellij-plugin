package com.intellij.lang.jsgraphql.types.validation.rules;


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
            addError(ValidationError.newValidationError()
                    .validationErrorType(ValidationErrorType.WrongType)
                    .sourceLocation(argument.getSourceLocation())
                    .description(validationUtil.getMessage())
                    .extensions(validationUtil.getErrorExtensions()));
        }
    }
}
