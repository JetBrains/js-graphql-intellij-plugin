/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.startup.javascript;

import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil;
import com.intellij.lang.jsgraphql.ide.references.GraphQLFindUsagesUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class GraphQLJavaScriptStartupActivity implements StartupActivity, DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {

        // register the JS file types for find usages
        GraphQLFindUsagesUtil.INCLUDED_FILE_TYPES.addAll(TypeScriptUtil.TYPESCRIPT_FILE_TYPES);
        GraphQLFindUsagesUtil.INCLUDED_FILE_TYPES.addAll(DialectDetector.JAVASCRIPT_FILE_TYPES);
    }
}
