package com.intellij.lang.jsgraphql.types.schema;


import com.intellij.lang.jsgraphql.types.PublicApi;

/**
 * A modified type wraps another graphql type and modifies it behavior
 *
 * @see com.intellij.lang.jsgraphql.types.schema.GraphQLNonNull
 * @see com.intellij.lang.jsgraphql.types.schema.GraphQLList
 */
@PublicApi
public interface GraphQLModifiedType extends GraphQLType {

    GraphQLType getWrappedType();
}
