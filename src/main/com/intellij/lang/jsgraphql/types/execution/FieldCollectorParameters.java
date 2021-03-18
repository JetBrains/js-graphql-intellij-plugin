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
package com.intellij.lang.jsgraphql.types.execution;

import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.FragmentDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLObjectType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;

import java.util.Map;

/**
 * Internal because FieldCollector is internal.
 */
@Internal
public class FieldCollectorParameters {
    private final GraphQLSchema graphQLSchema;
    private final Map<String, FragmentDefinition> fragmentsByName;
    private final Map<String, Object> variables;
    private final GraphQLObjectType objectType;

    public GraphQLSchema getGraphQLSchema() {
        return graphQLSchema;
    }

    public Map<String, FragmentDefinition> getFragmentsByName() {
        return fragmentsByName;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public GraphQLObjectType getObjectType() {
        return objectType;
    }

    private FieldCollectorParameters(GraphQLSchema graphQLSchema, Map<String, Object> variables, Map<String, FragmentDefinition> fragmentsByName, GraphQLObjectType objectType) {
        this.fragmentsByName = fragmentsByName;
        this.graphQLSchema = graphQLSchema;
        this.variables = variables;
        this.objectType = objectType;
    }

    public static Builder newParameters() {
        return new Builder();
    }

    public static class Builder {
        private GraphQLSchema graphQLSchema;
        private Map<String, FragmentDefinition> fragmentsByName;
        private Map<String, Object> variables;
        private GraphQLObjectType objectType;

        /**
         * @see FieldCollectorParameters#newParameters()
         */
        private Builder() {

        }

        public Builder schema(GraphQLSchema graphQLSchema) {
            this.graphQLSchema = graphQLSchema;
            return this;
        }

        public Builder objectType(GraphQLObjectType objectType) {
            this.objectType = objectType;
            return this;
        }

        public Builder fragments(Map<String, FragmentDefinition> fragmentsByName) {
            this.fragmentsByName = fragmentsByName;
            return this;
        }

        public Builder variables(Map<String, Object> variables) {
            this.variables = variables;
            return this;
        }

        public FieldCollectorParameters build() {
            Assert.assertNotNull(graphQLSchema, () -> "You must provide a schema");
            return new FieldCollectorParameters(graphQLSchema, variables, fragmentsByName, objectType);
        }

    }
}
