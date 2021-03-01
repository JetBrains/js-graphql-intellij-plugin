package com.intellij.lang.jsgraphql.types.validation.rules;

import com.intellij.lang.jsgraphql.types.DirectivesUtil;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.GraphQLDirective;
import com.intellij.lang.jsgraphql.types.validation.AbstractRule;
import com.intellij.lang.jsgraphql.types.validation.ValidationContext;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorCollector;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;

/**
 * https://facebook.github.io/graphql/June2018/#sec-Directives-Are-Unique-Per-Location
 */
@Internal
public class UniqueDirectiveNamesPerLocation extends AbstractRule {

    public UniqueDirectiveNamesPerLocation(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
        super(validationContext, validationErrorCollector);
    }

    @Override
    public void checkDocument(Document document) {
        super.checkDocument(document);
    }

    @Override
    public void checkInlineFragment(InlineFragment inlineFragment) {
        checkDirectivesUniqueness(inlineFragment, inlineFragment.getDirectives());
    }

    @Override
    public void checkFragmentDefinition(FragmentDefinition fragmentDefinition) {
        checkDirectivesUniqueness(fragmentDefinition, fragmentDefinition.getDirectives());
    }

    @Override
    public void checkFragmentSpread(FragmentSpread fragmentSpread) {
        checkDirectivesUniqueness(fragmentSpread, fragmentSpread.getDirectives());
    }

    @Override
    public void checkField(Field field) {
        checkDirectivesUniqueness(field, field.getDirectives());
    }

    @Override
    public void checkOperationDefinition(OperationDefinition operationDefinition) {
        checkDirectivesUniqueness(operationDefinition, operationDefinition.getDirectives());
    }

    private void checkDirectivesUniqueness(Node<?> directivesContainer, List<Directive> directives) {
        Set<String> directiveNames = new LinkedHashSet<>();
        for (Directive directive : directives) {
            String name = directive.getName();
            Map<String, List<GraphQLDirective>> directivesByName = getValidationContext().getSchema().getAllDirectivesByName();
            boolean nonRepeatable = DirectivesUtil.isAllNonRepeatable(directivesByName.getOrDefault(name, emptyList()));
            if (directiveNames.contains(name) && nonRepeatable) {
                addError(ValidationErrorType.DuplicateDirectiveName,
                        directive.getSourceLocation(),
                        duplicateDirectiveNameMessage(name, directivesContainer.getClass().getSimpleName()));
            } else {
                directiveNames.add(name);
            }
        }
    }

    private String duplicateDirectiveNameMessage(String directiveName, String location) {
        return String.format("Non repeatable directives must be uniquely named within a location. The directive '%s' used on a '%s' is not unique.", directiveName, location);
    }
}
