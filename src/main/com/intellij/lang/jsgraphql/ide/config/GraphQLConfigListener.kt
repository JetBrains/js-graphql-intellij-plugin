/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.config

import com.intellij.util.messages.Topic


interface GraphQLConfigListener {
  companion object {
    @JvmField
    @Topic.ProjectLevel
    val TOPIC = Topic(
      "GraphQL Configuration File Change Events",
      GraphQLConfigListener::class.java,
      Topic.BroadcastDirection.NONE
    )
  }

  fun onConfigurationChanged()
}
