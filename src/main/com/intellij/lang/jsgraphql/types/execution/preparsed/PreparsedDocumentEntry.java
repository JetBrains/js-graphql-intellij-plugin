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

import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.Document;

import java.io.Serializable;
import java.util.List;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static java.util.Collections.singletonList;

/**
 * An instance of a preparsed document entry represents the result of a query parse and validation, like
 * an either implementation it contains either the correct result in the document property or the errors.
 *
 * NOTE: This class implements {@link Serializable} and hence it can be serialised and placed into a distributed cache.  However we
 * are not aiming to provide long term compatibility and do not intend for you to place this serialised data into permanent storage,
 * with times frames that cross graphql-java versions.  While we don't change things unnecessarily,  we may inadvertently break
 * the serialised compatibility across versions.
 */
@PublicApi
public class PreparsedDocumentEntry implements Serializable {
    private final Document document;
    private final List<? extends GraphQLError> errors;

    public PreparsedDocumentEntry(Document document) {
        assertNotNull(document);
        this.document = document;
        this.errors = null;
    }

    public PreparsedDocumentEntry(List<? extends GraphQLError> errors) {
        assertNotNull(errors);
        this.document = null;
        this.errors = errors;
    }

    public PreparsedDocumentEntry(GraphQLError error) {
        this(singletonList(assertNotNull(error)));
    }

    public Document getDocument() {
        return document;
    }

    public List<? extends GraphQLError> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
}
