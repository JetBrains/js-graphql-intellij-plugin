/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql;

import com.intellij.openapi.util.NlsSafe;

public final class GraphQLConstants {

  public static final @NlsSafe String GraphQL = "GraphQL";

  public static final class Schema {
    public static final String __TYPE = "__type";
    public static final String __SCHEMA = "__schema";
  }

  public static final class Config {
    public static final String CODEGEN = "codegen";
    public static final String GRAPHQL = "graphql.config";
  }
}
