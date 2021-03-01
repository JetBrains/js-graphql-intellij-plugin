package com.intellij.lang.jsgraphql.types.validation.rules;


import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.FragmentDefinition;
import com.intellij.lang.jsgraphql.types.language.InlineFragment;
import com.intellij.lang.jsgraphql.types.schema.GraphQLCompositeType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import com.intellij.lang.jsgraphql.types.validation.AbstractRule;
import com.intellij.lang.jsgraphql.types.validation.ValidationContext;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorCollector;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;

@Internal
public class FragmentsOnCompositeType extends AbstractRule {


    public FragmentsOnCompositeType(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
        super(validationContext, validationErrorCollector);
    }

    @Override
    public void checkInlineFragment(InlineFragment inlineFragment) {
        if (inlineFragment.getTypeCondition() == null) {
            return;
        }
        GraphQLType type = getValidationContext().getSchema().getType(inlineFragment.getTypeCondition().getName());
        if (type == null) return;
        if (!(type instanceof GraphQLCompositeType)) {
            String message = "Inline fragment type condition is invalid, must be on Object/Interface/Union";
            addError(ValidationErrorType.InlineFragmentTypeConditionInvalid, inlineFragment.getSourceLocation(), message);
        }
    }

    @Override
    public void checkFragmentDefinition(FragmentDefinition fragmentDefinition) {
        GraphQLType type = getValidationContext().getSchema().getType(fragmentDefinition.getTypeCondition().getName());
        if (type == null) return;
        if (!(type instanceof GraphQLCompositeType)) {
            String message = "Fragment type condition is invalid, must be on Object/Interface/Union";
            addError(ValidationErrorType.FragmentTypeConditionInvalid, fragmentDefinition.getSourceLocation(), message);
        }
    }
}
