package com.intellij.lang.jsgraphql.types.validation.rules;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Argument;
import com.intellij.lang.jsgraphql.types.schema.GraphQLArgument;
import com.intellij.lang.jsgraphql.types.schema.GraphQLDirective;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.types.validation.AbstractRule;
import com.intellij.lang.jsgraphql.types.validation.ValidationContext;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorCollector;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;


@Internal
public class KnownArgumentNames extends AbstractRule {

    public KnownArgumentNames(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
        super(validationContext, validationErrorCollector);
    }


    @Override
    public void checkArgument(Argument argument) {
        GraphQLDirective directiveDef = getValidationContext().getDirective();
        if (directiveDef != null) {
            GraphQLArgument directiveArgument = directiveDef.getArgument(argument.getName());
            if (directiveArgument == null) {
                String message = String.format("Unknown directive argument %s", argument.getName());
                addError(ValidationErrorType.UnknownDirective, argument.getSourceLocation(), message);
            }

            return;
        }

        GraphQLFieldDefinition fieldDef = getValidationContext().getFieldDef();
        if (fieldDef == null) return;
        GraphQLArgument fieldArgument = fieldDef.getArgument(argument.getName());
        if (fieldArgument == null) {
            String message = String.format("Unknown field argument %s", argument.getName());
            addError(ValidationErrorType.UnknownArgument, argument.getSourceLocation(), message);
        }
    }
}
