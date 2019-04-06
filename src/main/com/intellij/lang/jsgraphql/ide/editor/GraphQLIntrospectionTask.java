/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.editor;

import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigVariableAwareEndpoint;

/**
 * Represents an executable introspection query against a GraphQL endpoint
 */
public class GraphQLIntrospectionTask {

    private final GraphQLConfigVariableAwareEndpoint endpoint;
    private final Runnable runnable;

    public GraphQLIntrospectionTask(GraphQLConfigVariableAwareEndpoint endpoint, Runnable runnable) {

        this.endpoint = endpoint;
        this.runnable = runnable;
    }

    public GraphQLConfigVariableAwareEndpoint getEndpoint() {
        return endpoint;
    }

    public Runnable getRunnable() {
        return runnable;
    }
}
