package com.intellij.lang.jsgraphql.types.validation.rules;


import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Field;
import com.intellij.lang.jsgraphql.types.schema.GraphQLOutputType;
import com.intellij.lang.jsgraphql.types.validation.AbstractRule;
import com.intellij.lang.jsgraphql.types.validation.ValidationContext;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorCollector;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;

import static com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil.isLeaf;
import static com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil.simplePrint;

@Internal
public class ScalarLeafs extends AbstractRule {

    public ScalarLeafs(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
        super(validationContext, validationErrorCollector);
    }

    @Override
    public void checkField(Field field) {
        GraphQLOutputType type = getValidationContext().getOutputType();
        if (type == null) return;
        if (isLeaf(type)) {
            if (field.getSelectionSet() != null) {
                String message = String.format("Sub selection not allowed on leaf type %s of field %s", simplePrint(type), field.getName());
                addError(ValidationErrorType.SubSelectionNotAllowed, field.getSourceLocation(), message);
            }
        } else {
            if (field.getSelectionSet() == null) {
                String message = String.format("Sub selection required for type %s of field %s", simplePrint(type), field.getName());
                addError(ValidationErrorType.SubSelectionRequired, field.getSourceLocation(), message);
            }
        }
    }
}
