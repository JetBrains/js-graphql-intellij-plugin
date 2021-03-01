package com.intellij.lang.jsgraphql.types.validation.rules;


import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.FragmentDefinition;
import com.intellij.lang.jsgraphql.types.language.FragmentSpread;
import com.intellij.lang.jsgraphql.types.validation.AbstractRule;
import com.intellij.lang.jsgraphql.types.validation.ValidationContext;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorCollector;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;

@Internal
public class KnownFragmentNames extends AbstractRule {

    public KnownFragmentNames(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
        super(validationContext, validationErrorCollector);
    }

    @Override
    public void checkFragmentSpread(FragmentSpread fragmentSpread) {
        FragmentDefinition fragmentDefinition = getValidationContext().getFragment(fragmentSpread.getName());
        if (fragmentDefinition == null) {
            String message = String.format("Undefined fragment %s", fragmentSpread.getName());
            addError(ValidationErrorType.UndefinedFragment, fragmentSpread.getSourceLocation(), message);
        }
    }
}
