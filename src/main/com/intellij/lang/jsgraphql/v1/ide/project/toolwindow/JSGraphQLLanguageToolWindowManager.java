/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.project.toolwindow;

import com.intellij.lang.jsgraphql.ide.project.schemastatus.GraphQLSchemasPanel;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.impl.ContentImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * GraphQL tool window manager based on JSLanguageCompilerToolWindowManager in the JavaScript plugin.
 * Contains the language service console and errors tree view.
 */
public class JSGraphQLLanguageToolWindowManager implements Disposable {

    @NotNull
    private final Project myProject;

    private volatile JPanel mySchemasTree;

    private volatile boolean myFirstInitialized;

    private volatile ToolWindow myToolWindow;

    private final Icon myIcon;

    private final String myToolWindowName;

    public JSGraphQLLanguageToolWindowManager(@NotNull Project project,
                                              @NotNull String toolWindowName,
                                              @Nullable Icon icon) {
        myProject = project;
        myToolWindowName = toolWindowName;
        myIcon = icon;
    }

    public synchronized void init() {
        if (!myFirstInitialized || myToolWindow == null) {
            ApplicationManager.getApplication().assertIsDispatchThread();
            ToolWindowManager manager = ToolWindowManager.getInstance(myProject);
            myToolWindow = manager.registerToolWindow(myToolWindowName, true, ToolWindowAnchor.BOTTOM, myProject, true);
            if (myIcon != null) {
                myToolWindow.setIcon(myIcon);
            }
            createSchemasPanel();
            myFirstInitialized = true;
        }
    }

    private void createSchemasPanel() {
        ApplicationManager.getApplication().assertIsDispatchThread();
        if (mySchemasTree == null) {
            final Ref<Content> contentRef = new Ref<>();
            mySchemasTree = new GraphQLSchemasPanel(myProject);
            final ContentImpl schemasContent = new ContentImpl(mySchemasTree, "Schemas and Project Structure", false);
            schemasContent.setCloseable(false);
            contentRef.set(schemasContent);
            myToolWindow.getContentManager().addContent(contentRef.get(), 0);

        }
    }

    @Override
    public void dispose() {
        cleanPanel();
    }

    private synchronized void cleanPanel() {
        if (!myProject.isDefault()) {
            if (!myProject.isDisposed()) {
                ToolWindowManager.getInstance(myProject).unregisterToolWindow(myToolWindowName);
            }
            myToolWindow = null;
        }
    }
}
