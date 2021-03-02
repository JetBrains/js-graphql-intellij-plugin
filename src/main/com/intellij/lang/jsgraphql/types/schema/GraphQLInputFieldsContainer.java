package com.intellij.lang.jsgraphql.types.schema;

import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.List;

/**
 * Types that can contain input fields are marked with this interface
 *
 * @see com.intellij.lang.jsgraphql.types.schema.GraphQLInputType
 */
@PublicApi
public interface GraphQLInputFieldsContainer extends GraphQLNamedType {

    GraphQLInputObjectField getFieldDefinition(String name);

    List<GraphQLInputObjectField> getFieldDefinitions();
}
