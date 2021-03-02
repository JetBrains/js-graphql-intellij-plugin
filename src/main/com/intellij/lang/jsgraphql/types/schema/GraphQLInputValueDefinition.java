package com.intellij.lang.jsgraphql.types.schema;

import com.intellij.lang.jsgraphql.types.PublicApi;

/**
 * Named schema elements that contain input type information.
 *
 *
 * @see com.intellij.lang.jsgraphql.types.schema.GraphQLInputType
 * @see com.intellij.lang.jsgraphql.types.schema.GraphQLInputObjectField
 * @see com.intellij.lang.jsgraphql.types.schema.GraphQLArgument
 */
@PublicApi
public interface GraphQLInputValueDefinition extends GraphQLDirectiveContainer {

    <T extends GraphQLInputType> T getType();
}
