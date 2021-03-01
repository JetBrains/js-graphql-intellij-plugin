package com.intellij.lang.jsgraphql.types.analysis;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Argument;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.schema.GraphQLArgument;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;

import java.util.Map;

@Internal
public class QueryVisitorFieldArgumentEnvironmentImpl implements QueryVisitorFieldArgumentEnvironment {

    private final GraphQLFieldDefinition fieldDefinition;
    private final Argument argument;
    private final GraphQLArgument graphQLArgument;
    private final Object argumentValue;
    private final Map<String, Object> variables;
    private final QueryVisitorFieldEnvironment parentEnvironment;
    private final TraverserContext<Node> traverserContext;
    private final GraphQLSchema schema;

    public QueryVisitorFieldArgumentEnvironmentImpl(GraphQLFieldDefinition fieldDefinition, Argument argument, GraphQLArgument graphQLArgument, Object argumentValue, Map<String, Object> variables, QueryVisitorFieldEnvironment parentEnvironment, TraverserContext<Node> traverserContext, GraphQLSchema schema) {
        this.fieldDefinition = fieldDefinition;
        this.argument = argument;
        this.graphQLArgument = graphQLArgument;
        this.argumentValue = argumentValue;
        this.variables = variables;
        this.parentEnvironment = parentEnvironment;
        this.traverserContext = traverserContext;
        this.schema = schema;
    }

    @Override
    public GraphQLSchema getSchema() {
        return schema;
    }

    @Override
    public Argument getArgument() {
        return argument;
    }

    public GraphQLFieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    @Override
    public GraphQLArgument getGraphQLArgument() {
        return graphQLArgument;
    }

    @Override
    public Object getArgumentValue() {
        return argumentValue;
    }

    @Override
    public Map<String, Object> getVariables() {
        return variables;
    }

    @Override
    public QueryVisitorFieldEnvironment getParentEnvironment() {
        return parentEnvironment;
    }

    @Override
    public TraverserContext<Node> getTraverserContext() {
        return traverserContext;
    }

    @Override
    public String toString() {
        return "QueryVisitorFieldArgumentEnvironmentImpl{" +
                "fieldDefinition=" + fieldDefinition +
                ", argument=" + argument +
                ", graphQLArgument=" + graphQLArgument +
                ", argumentValue=" + argumentValue +
                ", variables=" + variables +
                ", traverserContext=" + traverserContext +
                ", schema=" + schema +
                '}';
    }
}
