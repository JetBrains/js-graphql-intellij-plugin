package com.intellij.lang.jsgraphql.types.schema;


import com.intellij.lang.jsgraphql.types.PublicApi;

/**
 * Output types represent those set of types that are allowed to be sent back as a graphql response, as opposed
 * to {@link com.intellij.lang.jsgraphql.types.schema.GraphQLInputType}s which can only be used as graphql mutation input.
 */
@PublicApi
public interface GraphQLOutputType extends GraphQLType {
}
