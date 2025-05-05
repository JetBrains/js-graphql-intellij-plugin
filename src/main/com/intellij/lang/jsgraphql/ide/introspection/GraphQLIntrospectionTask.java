/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.introspection;

import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigEndpoint;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an executable introspection query against a GraphQL endpoint
 */
public final class GraphQLIntrospectionTask implements Runnable {

  @NotNull private final Project myProject;
  @NotNull private final GraphQLConfigEndpoint myEndpoint;

  public GraphQLIntrospectionTask(@NotNull Project project, @NotNull GraphQLConfigEndpoint endpoint) {
    myProject = project;
    myEndpoint = endpoint;
  }

  public @NotNull GraphQLConfigEndpoint getEndpoint() {
    return myEndpoint;
  }

  @Override
  public void run() {
    GraphQLIntrospectionService.getInstance(myProject).performIntrospectionQuery(myEndpoint);
  }
}
