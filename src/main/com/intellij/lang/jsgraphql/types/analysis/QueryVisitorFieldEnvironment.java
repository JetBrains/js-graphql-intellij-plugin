package com.intellij.lang.jsgraphql.types.analysis;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.Field;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.language.SelectionSetContainer;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldsContainer;
import com.intellij.lang.jsgraphql.types.schema.GraphQLOutputType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;

import java.util.Map;

@PublicApi
public interface QueryVisitorFieldEnvironment {

    /**
     * @return the graphql schema in play
     */
    GraphQLSchema getSchema();

    /**
     * @return true if the current field is __typename
     */
    boolean isTypeNameIntrospectionField();

    /**
     * @return the current Field
     */
    Field getField();

    GraphQLFieldDefinition getFieldDefinition();

    /**
     * @return the parent output type of the current field.
     */
    GraphQLOutputType getParentType();

    /**
     * @return the unmodified fields container fot the current type. This is the unwrapped version of {@link #getParentType()}
     * It is either {@link graphql.schema.GraphQLObjectType} or {@link graphql.schema.GraphQLInterfaceType}. because these
     * are the only {@link GraphQLFieldsContainer}
     *
     * @throws IllegalStateException if the current field is __typename see {@link #isTypeNameIntrospectionField()}
     */
    GraphQLFieldsContainer getFieldsContainer();

    QueryVisitorFieldEnvironment getParentEnvironment();

    Map<String, Object> getArguments();

    SelectionSetContainer getSelectionSetContainer();

    TraverserContext<Node> getTraverserContext();
}
