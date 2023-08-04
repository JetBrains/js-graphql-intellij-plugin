/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.highlighting.query;

public class GraphQLQueryContext {

  public String query;
  public Runnable onError;

  public GraphQLQueryContext(String query, Runnable onError) {
    this.query = query;
    this.onError = onError;
  }
}
