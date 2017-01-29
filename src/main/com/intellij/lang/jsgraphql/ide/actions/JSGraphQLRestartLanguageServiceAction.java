/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.actions;

import com.intellij.icons.AllIcons;
import com.intellij.lang.jsgraphql.ide.project.JSGraphQLLanguageUIProjectService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class JSGraphQLRestartLanguageServiceAction extends AnAction {

    public JSGraphQLRestartLanguageServiceAction() {
        super("Restart JS GraphQL Language Service", "Restarts the JS GraphQL Language Service Node.js process", AllIcons.Javaee.UpdateRunningApplication);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        if (e.getProject() != null) {
            JSGraphQLLanguageUIProjectService.getService(e.getProject()).restartInstance();
        }
    }
}
