package com.intellij.lang.jsgraphql.types.validation.rules;


import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Field;
import com.intellij.lang.jsgraphql.types.schema.GraphQLCompositeType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.types.validation.AbstractRule;
import com.intellij.lang.jsgraphql.types.validation.ValidationContext;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorCollector;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;

@Internal
public class FieldsOnCorrectType extends AbstractRule {


    public FieldsOnCorrectType(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
        super(validationContext, validationErrorCollector);
    }


    @Override
    public void checkField(Field field) {
        GraphQLCompositeType parentType = getValidationContext().getParentType();
        // this means the parent type is not a CompositeType, which is an error handled elsewhere
        if (parentType == null) return;
        GraphQLFieldDefinition fieldDef = getValidationContext().getFieldDef();
        if (fieldDef == null) {
            String message = String.format("Field '%s' in type '%s' is undefined", field.getName(), parentType.getName());
            addError(ValidationErrorType.FieldUndefined, field.getSourceLocation(), message);
        }

    }
}
