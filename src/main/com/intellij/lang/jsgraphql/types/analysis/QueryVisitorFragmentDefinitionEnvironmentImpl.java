package com.intellij.lang.jsgraphql.types.analysis;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.FragmentDefinition;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;

import java.util.Objects;

@Internal
public class QueryVisitorFragmentDefinitionEnvironmentImpl implements QueryVisitorFragmentDefinitionEnvironment {

    private final FragmentDefinition fragmentDefinition;
    private final TraverserContext<Node> traverserContext;
    private final GraphQLSchema schema;


    public QueryVisitorFragmentDefinitionEnvironmentImpl(FragmentDefinition fragmentDefinition, TraverserContext<Node> traverserContext, GraphQLSchema schema) {
        this.fragmentDefinition = fragmentDefinition;
        this.traverserContext = traverserContext;
        this.schema = schema;
    }

    @Override
    public GraphQLSchema getSchema() {
        return schema;
    }

    @Override
    public FragmentDefinition getFragmentDefinition() {
        return fragmentDefinition;
    }

    @Override
    public TraverserContext<Node> getTraverserContext() {
        return traverserContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueryVisitorFragmentDefinitionEnvironmentImpl that = (QueryVisitorFragmentDefinitionEnvironmentImpl) o;
        return Objects.equals(fragmentDefinition, that.fragmentDefinition);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fragmentDefinition);
    }

    @Override
    public String toString() {
        return "QueryVisitorFragmentDefinitionEnvironmentImpl{" +
                "fragmentDefinition=" + fragmentDefinition +
                '}';
    }
}

