/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
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
