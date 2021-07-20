/*
    The MIT License (MIT)

    Copyright (c) 2015 Andreas Marek and Contributors

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
    (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
    publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
    so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
    OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
    CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.intellij.lang.jsgraphql.types.execution.instrumentation.fieldvalidation;

import com.google.common.collect.ImmutableList;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.execution.ResultPath;

import java.util.*;
import java.util.function.BiFunction;

/**
 * This very simple field validation will run the supplied function for a given field path and if it returns an error
 * it will be added to the list of problems.
 *
 * Use {@link #addRule(ResultPath, BiFunction)} to supply the rule callbacks where
 * you implement your specific business logic
 */
@PublicApi
public class SimpleFieldValidation implements FieldValidation {

    private final Map<ResultPath, BiFunction<FieldAndArguments, FieldValidationEnvironment, Optional<GraphQLError>>> rules = new LinkedHashMap<>();

    /**
     * Adds the rule against the field address path.  If the rule returns an error, it will be added to the list of errors
     *
     * @param fieldPath the path to the field
     * @param rule      the rule function
     *
     * @return this validator
     */
    public SimpleFieldValidation addRule(ResultPath fieldPath, BiFunction<FieldAndArguments, FieldValidationEnvironment, Optional<GraphQLError>> rule) {
        rules.put(fieldPath, rule);
        return this;
    }

    @Override
    public List<GraphQLError> validateFields(FieldValidationEnvironment validationEnvironment) {
        List<GraphQLError> errors = new ArrayList<>();
        for (ResultPath fieldPath : rules.keySet()) {
            List<FieldAndArguments> fieldAndArguments = validationEnvironment.getFieldsByPath().get(fieldPath);
            if (fieldAndArguments != null) {
                BiFunction<FieldAndArguments, FieldValidationEnvironment, Optional<GraphQLError>> ruleFunction = rules.get(fieldPath);

                for (FieldAndArguments fieldAndArgument : fieldAndArguments) {
                    Optional<GraphQLError> graphQLError = ruleFunction.apply(fieldAndArgument, validationEnvironment);
                    graphQLError.ifPresent(errors::add);
                }
            }
        }
        return ImmutableList.copyOf(errors);
    }
}
