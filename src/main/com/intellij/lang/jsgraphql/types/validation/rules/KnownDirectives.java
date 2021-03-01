package com.intellij.lang.jsgraphql.types.validation.rules;


import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.introspection.Introspection.DirectiveLocation;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.language.OperationDefinition.Operation;
import com.intellij.lang.jsgraphql.types.schema.GraphQLDirective;
import com.intellij.lang.jsgraphql.types.validation.AbstractRule;
import com.intellij.lang.jsgraphql.types.validation.ValidationContext;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorCollector;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;

import java.util.EnumSet;
import java.util.List;

@Internal
public class KnownDirectives extends AbstractRule {


    public KnownDirectives(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
        super(validationContext, validationErrorCollector);
    }

    @Override
    public void checkDirective(Directive directive, List<Node> ancestors) {
        GraphQLDirective graphQLDirective = getValidationContext().getSchema().getFirstDirective(directive.getName());
        if (graphQLDirective == null) {
            String message = String.format("Unknown directive %s", directive.getName());
            addError(ValidationErrorType.UnknownDirective, directive.getSourceLocation(), message);
            return;
        }

        Node ancestor = ancestors.get(ancestors.size() - 1);
        if (hasInvalidLocation(graphQLDirective, ancestor)) {
            String message = String.format("Directive %s not allowed here", directive.getName());
            addError(ValidationErrorType.MisplacedDirective, directive.getSourceLocation(), message);
        }
    }

    @SuppressWarnings("deprecation") // the suppression stands because its deprecated but still in graphql spec
    private boolean hasInvalidLocation(GraphQLDirective directive, Node ancestor) {
        EnumSet<DirectiveLocation> validLocations = directive.validLocations();
        if (ancestor instanceof OperationDefinition) {
            Operation operation = ((OperationDefinition) ancestor).getOperation();
            if (Operation.QUERY.equals(operation)) {
                return !validLocations.contains(DirectiveLocation.QUERY);
            } else if (Operation.MUTATION.equals(operation)) {
                return !validLocations.contains(DirectiveLocation.MUTATION);
            } else if (Operation.SUBSCRIPTION.equals(operation)) {
                return !validLocations.contains(DirectiveLocation.SUBSCRIPTION);
            }
        } else if (ancestor instanceof Field) {
            return !(validLocations.contains(DirectiveLocation.FIELD));
        } else if (ancestor instanceof FragmentSpread) {
            return !(validLocations.contains(DirectiveLocation.FRAGMENT_SPREAD));
        } else if (ancestor instanceof FragmentDefinition) {
            return !(validLocations.contains(DirectiveLocation.FRAGMENT_DEFINITION));
        } else if (ancestor instanceof InlineFragment) {
            return !(validLocations.contains(DirectiveLocation.INLINE_FRAGMENT));
        } else if (ancestor instanceof VariableDefinition) {
            return !(validLocations.contains(DirectiveLocation.VARIABLE_DEFINITION));
        }
        return true;
    }
}
