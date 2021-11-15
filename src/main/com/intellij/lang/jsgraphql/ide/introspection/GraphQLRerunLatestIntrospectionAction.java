/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.introspection;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class GraphQLRerunLatestIntrospectionAction extends AnAction {

    private static final String TEXT = "Update the Local Schema by Running Introspection Query";

    public GraphQLRerunLatestIntrospectionAction() {
        super(TEXT, "Re-runs the latest introspection query that was performed to update the local schema, e.g. when a remote schema has been changed", AllIcons.Actions.Rerun);
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        boolean enabled = false;
        e.getPresentation().setText(TEXT);
        if (e.getProject() != null) {
            final GraphQLIntrospectionTask latestIntrospection = GraphQLIntrospectionService.getInstance(e.getProject()).getLatestIntrospection();
            if (latestIntrospection != null) {
                enabled = true;
                e.getPresentation().setText(TEXT + " (" + latestIntrospection.getEndpoint().getName() + ")");
            }
        }
        e.getPresentation().setEnabled(enabled);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        if (e.getProject() != null) {
            GraphQLIntrospectionTask latestIntrospection = GraphQLIntrospectionService.getInstance(e.getProject()).getLatestIntrospection();
            if (latestIntrospection != null) {
                latestIntrospection.getRunnable().run();
            }
        }
    }
}
