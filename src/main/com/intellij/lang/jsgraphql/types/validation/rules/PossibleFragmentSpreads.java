package com.intellij.lang.jsgraphql.types.validation.rules;


import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.execution.TypeFromAST;
import com.intellij.lang.jsgraphql.types.language.FragmentDefinition;
import com.intellij.lang.jsgraphql.types.language.FragmentSpread;
import com.intellij.lang.jsgraphql.types.language.InlineFragment;
import com.intellij.lang.jsgraphql.types.schema.*;
import com.intellij.lang.jsgraphql.types.validation.AbstractRule;
import com.intellij.lang.jsgraphql.types.validation.ValidationContext;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorCollector;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;

import java.util.Collections;
import java.util.List;

import static com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil.simplePrint;

@Internal
public class PossibleFragmentSpreads extends AbstractRule {

    public PossibleFragmentSpreads(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
        super(validationContext, validationErrorCollector);
    }


    @Override
    public void checkInlineFragment(InlineFragment inlineFragment) {
        GraphQLOutputType fragType = getValidationContext().getOutputType();
        GraphQLCompositeType parentType = getValidationContext().getParentType();
        if (fragType == null || parentType == null) return;

        if (isValidTargetCompositeType(fragType) && isValidTargetCompositeType(parentType) && !doTypesOverlap(fragType, parentType)) {
            String message = String.format("Fragment cannot be spread here as objects of " +
                    "type %s can never be of type %s", parentType.getName(), simplePrint(fragType));
            addError(ValidationErrorType.InvalidFragmentType, inlineFragment.getSourceLocation(), message);
        }
    }

    @Override
    public void checkFragmentSpread(FragmentSpread fragmentSpread) {
        FragmentDefinition fragment = getValidationContext().getFragment(fragmentSpread.getName());
        if (fragment == null) return;
        GraphQLType typeCondition = TypeFromAST.getTypeFromAST(getValidationContext().getSchema(), fragment.getTypeCondition());
        GraphQLCompositeType parentType = getValidationContext().getParentType();
        if (typeCondition == null || parentType == null) return;

        if (isValidTargetCompositeType(typeCondition) && isValidTargetCompositeType(parentType) && !doTypesOverlap(typeCondition, parentType)) {
            String message = String.format("Fragment %s cannot be spread here as objects of " +
                    "type %s can never be of type %s", fragmentSpread.getName(), parentType.getName(), simplePrint(typeCondition));
            addError(ValidationErrorType.InvalidFragmentType, fragmentSpread.getSourceLocation(), message);
        }
    }

    private boolean doTypesOverlap(GraphQLType type, GraphQLCompositeType parent) {
        if (type == parent) {
            return true;
        }

        List<? extends GraphQLType> possibleParentTypes = getPossibleType(parent);
        List<? extends GraphQLType> possibleConditionTypes = getPossibleType(type);

        return !Collections.disjoint(possibleParentTypes, possibleConditionTypes);

    }

    private List<? extends GraphQLType> getPossibleType(GraphQLType type) {
        List<? extends GraphQLType> possibleConditionTypes = null;
        if (type instanceof GraphQLObjectType) {
            possibleConditionTypes = Collections.singletonList(type);
        } else if (type instanceof GraphQLInterfaceType) {
            possibleConditionTypes = getValidationContext().getSchema().getImplementations((GraphQLInterfaceType) type);
        } else if (type instanceof GraphQLUnionType) {
            possibleConditionTypes = ((GraphQLUnionType) type).getTypes();
        } else {
            Assert.assertShouldNeverHappen();
        }
        return possibleConditionTypes;
    }

    /**
     * Per spec: The target type of fragment (type condition)
     * must have kind UNION, INTERFACE, or OBJECT.
     * @param type GraphQLType
     * @return true if it is a union, interface, or object.
     */
    private boolean isValidTargetCompositeType(GraphQLType type) {
        return type instanceof GraphQLCompositeType;
    }
}
