/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.startup

import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigWatcher
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLConfigEnvironment
import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourcesUpdater
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaContentTracker
import com.intellij.lang.jsgraphql.ui.GraphQLUIProjectService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

/**
 * Starts up the UI Service during project startup
 */
class GraphQLStartupActivity : StartupActivity.Background {
  override fun runActivity(project: Project) {
    // init mandatory services
    GraphQLSchemaContentTracker.getInstance(project)
    GraphQLConfigWatcher.getInstance(project)
    GraphQLGeneratedSourcesUpdater.getInstance(project)
    GraphQLConfigProvider.getInstance(project).scheduleConfigurationReload()
    GraphQLConfigEnvironment.getInstance(project)

    if (!ApplicationManager.getApplication().isHeadlessEnvironment) {
      GraphQLUIProjectService.getInstance(project).projectOpened()
    }
  }
}
