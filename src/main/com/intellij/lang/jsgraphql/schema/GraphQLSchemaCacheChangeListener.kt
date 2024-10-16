/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema

import com.intellij.util.messages.Topic

/**
 * Some schema computation has finished in the background. Use [GraphQLSchemaProvider.getCachedSchemaInfo]
 * to retrieve the latest version without the need to wait for a computation to finish.
 */
interface GraphQLSchemaCacheChangeListener {

  companion object {
    @JvmField
    @Topic.ProjectLevel
    val TOPIC = Topic(
      "GraphQL Schema Cache Change Events",
      GraphQLSchemaCacheChangeListener::class.java,
      Topic.BroadcastDirection.NONE
    )
  }

  /**
   * One or more GraphQL schema computations have finished and been put into the cache within [GraphQLSchemaProvider].
   */
  fun onSchemaCacheChanged()
}
