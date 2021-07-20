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
package com.intellij.lang.jsgraphql.types;

import com.intellij.lang.jsgraphql.types.language.Document;
import com.intellij.lang.jsgraphql.types.parser.InvalidSyntaxException;
import com.intellij.lang.jsgraphql.types.parser.Parser;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.validation.ValidationError;
import com.intellij.lang.jsgraphql.types.validation.Validator;

import java.util.List;

/**
 * This class allows you to parse and validate a graphql query without executing it.  It will tell you
 * if its syntactically valid and also semantically valid according to the graphql specification
 * and the provided schema.
 */
@PublicApi
public class ParseAndValidate {

    /**
     * This can be called to parse and validate a graphql query against a schema, which is useful if you want to know if it would be acceptable
     * for execution.
     *
     * @param graphQLSchema  the schema to validate against
     * @param executionInput the execution input containing the query
     * @return a result object that indicates how this operation went
     */
    public static ParseAndValidateResult parseAndValidate(GraphQLSchema graphQLSchema, ExecutionInput executionInput) {
        ParseAndValidateResult result = parse(executionInput);
        if (!result.isFailure()) {
            List<ValidationError> errors = validate(graphQLSchema, result.getDocument());
            return result.transform(builder -> builder.validationErrors(errors));
        }
        return result;
    }

    /**
     * This can be called to parse (but not validate) a graphql query.
     *
     * @param executionInput the input containing the query
     * @return a result object that indicates how this operation went
     */
    public static ParseAndValidateResult parse(ExecutionInput executionInput) {
        try {
            Parser parser = new Parser();
            Document document = parser.parseDocument(executionInput.getQuery());
            return ParseAndValidateResult.newResult().document(document).variables(executionInput.getVariables()).build();
        } catch (InvalidSyntaxException e) {
            return ParseAndValidateResult.newResult().syntaxException(e).variables(executionInput.getVariables()).build();
        }
    }

    /**
     * This can be called to validate a parsed graphql query.
     *
     * @param graphQLSchema  the graphql schema to validate against
     * @param parsedDocument the previously parsed document
     * @return a result object that indicates how this operation went
     */
    public static List<ValidationError> validate(GraphQLSchema graphQLSchema, Document parsedDocument) {
        Validator validator = new Validator();
        return validator.validateDocument(graphQLSchema, parsedDocument);
    }
}
