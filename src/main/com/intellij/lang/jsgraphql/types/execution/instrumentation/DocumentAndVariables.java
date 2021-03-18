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
package com.intellij.lang.jsgraphql.types.execution.instrumentation;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.collect.ImmutableMapWithNullValues;
import com.intellij.lang.jsgraphql.types.language.Document;

import java.util.Map;
import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

@PublicApi
public class DocumentAndVariables {
    private final Document document;
    private final ImmutableMapWithNullValues<String, Object> variables;

    private DocumentAndVariables(Document document, Map<String, Object> variables) {
        this.document = assertNotNull(document);
        this.variables = ImmutableMapWithNullValues.copyOf(assertNotNull(variables));
    }

    public Document getDocument() {
        return document;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public DocumentAndVariables transform(Consumer<Builder> builderConsumer) {
        Builder builder = new Builder().document(this.document).variables(this.variables);
        builderConsumer.accept(builder);
        return builder.build();
    }

    public static Builder newDocumentAndVariables() {
        return new Builder();
    }

    public static class Builder {
        private Document document;
        private Map<String, Object> variables;

        public Builder document(Document document) {
            this.document = document;
            return this;
        }

        public Builder variables(Map<String, Object> variables) {
            this.variables = variables;
            return this;
        }

        public DocumentAndVariables build() {
            return new DocumentAndVariables(document, variables);
        }
    }
}
