package com.intellij.lang.jsgraphql.schema;

import java.util.EventListener;

/**
 * Events relating to GraphQL schemas
 */
public interface GraphQLSchemaEventListener extends EventListener {

    /**
     * One or more GraphQL schema changes are likely based on changed to the PSI trees
     */
    void onGraphQLSchemaChanged();
}
