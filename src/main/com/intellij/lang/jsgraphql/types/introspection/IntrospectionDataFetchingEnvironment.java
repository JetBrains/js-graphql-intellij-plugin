package com.intellij.lang.jsgraphql.types.introspection;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;

import java.util.Map;

/**
 * Extracted from {@link graphql.schema.DataFetchingEnvironment} to only capture
 * the data really needed for {@link Introspection}
 */
@Internal
public interface IntrospectionDataFetchingEnvironment {

    <T> T getSource();

    Map<String, Object> getArguments();

    GraphQLSchema getGraphQLSchema();

    <T> T getArgument(String name);

    GraphQLType getParentType();
}
