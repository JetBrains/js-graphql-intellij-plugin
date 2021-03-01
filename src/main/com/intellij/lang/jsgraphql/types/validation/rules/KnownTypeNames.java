package com.intellij.lang.jsgraphql.types.validation.rules;


import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.TypeName;
import com.intellij.lang.jsgraphql.types.validation.AbstractRule;
import com.intellij.lang.jsgraphql.types.validation.ValidationContext;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorCollector;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;

@Internal
public class KnownTypeNames extends AbstractRule {


    public KnownTypeNames(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
        super(validationContext, validationErrorCollector);
    }

    @Override
    public void checkTypeName(TypeName typeName) {
        if ((getValidationContext().getSchema().getType(typeName.getName())) == null) {
            String message = String.format("Unknown type %s", typeName.getName());
            addError(ValidationErrorType.UnknownType, typeName.getSourceLocation(), message);
        }
    }
}
