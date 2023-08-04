package com.intellij.lang.jsgraphql.ide.config.env

import com.intellij.util.messages.Topic

interface GraphQLConfigEnvironmentListener {
  companion object {
    @JvmField
    @Topic.ProjectLevel
    val TOPIC = Topic.create(
      "GraphQL Environment Changed Events",
      GraphQLConfigEnvironmentListener::class.java,
    )
  }

  fun onEnvironmentChanged()
}
