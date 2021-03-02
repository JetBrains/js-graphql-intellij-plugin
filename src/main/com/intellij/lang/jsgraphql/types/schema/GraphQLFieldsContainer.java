package com.intellij.lang.jsgraphql.types.schema;

import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.List;


/**
 * Types that can contain output fields are marked with this interface
 *
 * @see com.intellij.lang.jsgraphql.types.schema.GraphQLObjectType
 * @see com.intellij.lang.jsgraphql.types.schema.GraphQLInterfaceType
 */
@PublicApi
public interface GraphQLFieldsContainer extends GraphQLCompositeType {

    GraphQLFieldDefinition getFieldDefinition(String name);

    List<GraphQLFieldDefinition> getFieldDefinitions();

    default GraphQLFieldDefinition getField(String name) {
        return getFieldDefinition(name);
    }

    default List<GraphQLFieldDefinition> getFields() {
        return getFieldDefinitions();
    }
}
