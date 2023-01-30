/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.startup

import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigChangeTracker
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.highlighting.query.GraphQLQueryContextCaretListener
import com.intellij.lang.jsgraphql.ide.project.GraphQLUIProjectService
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaChangeTracker
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectPostStartupActivity

/**
 * Starts up the UI Service during project startup
 */
class GraphQLStartupActivity : ProjectPostStartupActivity {
    override suspend fun execute(project: Project) {
        // init mandatory services
        GraphQLSchemaChangeTracker.getInstance(project)
        GraphQLConfigChangeTracker.getInstance(project)
        GraphQLConfigProvider.getInstance(project).scheduleConfigurationReload()

        if (!ApplicationManager.getApplication().isUnitTestMode) {
            GraphQLUIProjectService.getService(project)
            GraphQLQueryContextCaretListener.getInstance(project).listen()
        }
    }
}
