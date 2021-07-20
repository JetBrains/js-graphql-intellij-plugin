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
package com.intellij.lang.jsgraphql.types.execution.preparsed;


import com.intellij.lang.jsgraphql.types.ExecutionInput;
import com.intellij.lang.jsgraphql.types.PublicSpi;

import java.util.function.Function;

/**
 * Interface that allows clients to hook in Document caching and/or the whitelisting of queries.
 */
@PublicSpi
public interface PreparsedDocumentProvider {
    /**
     * This is called to get a "cached" pre-parsed query and if its not present, then the "parseAndValidateFunction"
     * can be called to parse and validate the query.
     * <p>
     * Note - the "parseAndValidateFunction" MUST be called if you dont have a per parsed version of the query because it not only parses
     * and validates the query, it invokes {@link com.intellij.lang.jsgraphql.types.execution.instrumentation.Instrumentation} calls as well for parsing and validation.
     * if you dont make a call back on this then these wont happen.
     *
     * @param executionInput           The {@link ExecutionInput} containing the query
     * @param parseAndValidateFunction If the query has not be pre-parsed, this function MUST be called to parse and validate it
     * @return an instance of {@link PreparsedDocumentEntry}
     */
    PreparsedDocumentEntry getDocument(ExecutionInput executionInput, Function<ExecutionInput, PreparsedDocumentEntry> parseAndValidateFunction);
}


