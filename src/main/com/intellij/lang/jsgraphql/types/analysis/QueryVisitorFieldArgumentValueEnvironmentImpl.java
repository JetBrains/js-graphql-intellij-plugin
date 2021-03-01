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
