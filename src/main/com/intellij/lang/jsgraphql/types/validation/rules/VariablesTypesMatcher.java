package com.intellij.lang.jsgraphql.types.validation.rules;


import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.NullValue;
import com.intellij.lang.jsgraphql.types.language.Value;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;

import static com.intellij.lang.jsgraphql.types.schema.GraphQLNonNull.nonNull;
import static com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil.*;

@Internal
public class VariablesTypesMatcher {

    public boolean doesVariableTypesMatch(GraphQLType variableType, Value variableDefaultValue, GraphQLType expectedType) {
        return checkType(effectiveType(variableType, variableDefaultValue), expectedType);
    }

    public GraphQLType effectiveType(GraphQLType variableType, Value defaultValue) {
        if (defaultValue == null || defaultValue instanceof NullValue) {
            return variableType;
        }
        if (isNonNull(variableType)) {
            return variableType;
        }
        return nonNull(variableType);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean checkType(GraphQLType actualType, GraphQLType expectedType) {

        if (isNonNull(expectedType)) {
            if (isNonNull(actualType)) {
                return checkType(unwrapOne(actualType), unwrapOne(expectedType));
            }
            return false;
        }

        if (isNonNull(actualType)) {
            return checkType(unwrapOne(actualType), expectedType);
        }


        if (isList(actualType) && isList(expectedType)) {
            return checkType(unwrapOne(actualType), unwrapOne(expectedType));
        }
        return actualType == expectedType;
    }

}
