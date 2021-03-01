package com.intellij.lang.jsgraphql.types.analysis;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.schema.GraphQLArgument;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;

import java.util.Map;

@PublicApi
public interface QueryVisitorFieldArgumentValueEnvironment {

    GraphQLSchema getSchema();

    GraphQLFieldDefinition getFieldDefinition();

    GraphQLArgument getGraphQLArgument();

    QueryVisitorFieldArgumentInputValue getArgumentInputValue();

    Map<String, Object> getVariables();

    TraverserContext<Node> getTraverserContext();

}
