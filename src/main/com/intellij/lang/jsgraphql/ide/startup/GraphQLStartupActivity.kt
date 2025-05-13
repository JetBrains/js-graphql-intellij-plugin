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
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryManager
import com.intellij.lang.jsgraphql.ui.GraphQLUIProjectService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class GraphQLStartupActivity : ProjectActivity {
  override suspend fun execute(project: Project) {
    // init mandatory services
    project.serviceAsync<GraphQLSchemaContentTracker>()
    project.serviceAsync<GraphQLConfigWatcher>()
    project.serviceAsync<GraphQLGeneratedSourcesUpdater>()
    project.serviceAsync<GraphQLConfigEnvironment>()
    project.serviceAsync<GraphQLConfigProvider>().scheduleConfigurationReload()

    GraphQLLibraryManager.getInstanceAsync(project).syncLibraries()

    if (!ApplicationManager.getApplication().isHeadlessEnvironment) {
      project.serviceAsync<GraphQLUIProjectService>().projectOpened()
    }
  }
}
