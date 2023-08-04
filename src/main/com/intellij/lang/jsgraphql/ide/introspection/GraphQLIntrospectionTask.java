/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.introspection;

import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigEndpoint;

/**
 * Represents an executable introspection query against a GraphQL endpoint
 */
public class GraphQLIntrospectionTask {

  private final GraphQLConfigEndpoint endpoint;
  private final Runnable runnable;

  public GraphQLIntrospectionTask(GraphQLConfigEndpoint endpoint, Runnable runnable) {

    this.endpoint = endpoint;
    this.runnable = runnable;
  }

  public GraphQLConfigEndpoint getEndpoint() {
    return endpoint;
  }

  public Runnable getRunnable() {
    return runnable;
  }
}
