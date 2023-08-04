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
 * Events relating to GraphQL schemas
 */
interface GraphQLSchemaContentChangeListener {

  companion object {
    @JvmField
    @Topic.ProjectLevel
    val TOPIC = Topic(
      "GraphQL Schema Content Change Events",
      GraphQLSchemaContentChangeListener::class.java,
      Topic.BroadcastDirection.NONE
    )
  }

  /**
   * One or more GraphQL schema changes are likely based on changed to the PSI trees
   */
  fun onSchemaChanged()
}
