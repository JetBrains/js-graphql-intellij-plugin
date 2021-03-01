package com.intellij.lang.jsgraphql.types.schema;

import com.intellij.lang.jsgraphql.types.PublicApi;

/**
 * A GraphQLType which is also a named element, which means it has a getName() method.
 */
@PublicApi
public interface GraphQLNamedType extends GraphQLType, GraphQLNamedSchemaElement {


}
