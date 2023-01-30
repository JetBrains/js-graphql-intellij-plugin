/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.graphqlconfig

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectPostStartupActivity

class GraphQLConfigProjectStartupActivity : ProjectPostStartupActivity {
    override suspend fun execute(project: Project) {
        // TODO: get rid of
        GraphQLConfigManager.getService(project).initialize()
    }
}
