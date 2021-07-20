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
package com.intellij.lang.jsgraphql.types.analysis;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.schema.GraphQLArgument;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;

import java.util.Map;

@Internal
public class QueryVisitorFieldArgumentValueEnvironmentImpl implements QueryVisitorFieldArgumentValueEnvironment {

    private final GraphQLFieldDefinition fieldDefinition;
    private final GraphQLArgument graphQLArgument;
    private final QueryVisitorFieldArgumentInputValue argumentInputValue;
    private final TraverserContext<Node> traverserContext;
    private final GraphQLSchema schema;
    private final Map<String, Object> variables;

    public QueryVisitorFieldArgumentValueEnvironmentImpl(GraphQLSchema schema, GraphQLFieldDefinition fieldDefinition, GraphQLArgument graphQLArgument, QueryVisitorFieldArgumentInputValue argumentInputValue, TraverserContext<Node> traverserContext, Map<String, Object> variables) {
        this.fieldDefinition = fieldDefinition;
        this.graphQLArgument = graphQLArgument;
        this.argumentInputValue = argumentInputValue;
        this.traverserContext = traverserContext;
        this.schema = schema;
        this.variables = variables;
    }

    @Override
    public GraphQLSchema getSchema() {
        return schema;
    }

    @Override
    public GraphQLFieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    @Override
    public GraphQLArgument getGraphQLArgument() {
        return graphQLArgument;
    }

    @Override
    public QueryVisitorFieldArgumentInputValue getArgumentInputValue() {
        return argumentInputValue;
    }

    @Override
    public Map<String, Object> getVariables() {
        return variables;
    }

    @Override
    public TraverserContext<Node> getTraverserContext() {
        return traverserContext;
    }

    @Override
    public String toString() {
        return "QueryVisitorFieldArgumentValueEnvironmentImpl{" +
                "fieldDefinition=" + fieldDefinition +
                ", graphQLArgument=" + graphQLArgument +
                ", argumentInputValue=" + argumentInputValue +
                ", traverserContext=" + traverserContext +
                ", schema=" + schema +
                '}';
    }
}
