/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.toolwindow;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.ide.errorTreeView.NewErrorTreeViewPanel;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.impl.ContentImpl;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.ImmutableList;
import com.intellij.util.ui.MessageCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * GraphQL tool window manager based on JSLanguageCompilerToolWindowManager in the JavaScript plugin.
 * Contains the language service console and errors tree view.
 */
public class JSGraphQLLanguageToolWindowManager implements Disposable {

    @NotNull
    private final Project myProject;

    private volatile NewErrorTreeViewPanel myCurrentErrorTreeViewPanel;

    private volatile NewErrorTreeViewPanel myProjectErrorTreeViewPanel;

    private volatile boolean myFirstInitialized;

    private ConsoleView myConsoleView;

    private ContentImpl myConsoleContent;

    private volatile ToolWindow myToolWindow;

    private String myHelpId;

    private final Icon myIcon;

    private AnAction[] myActions;

    private final String myToolWindowName;

    private volatile List<JSGraphQLErrorResult> myLastResult;

    public JSGraphQLLanguageToolWindowManager(@NotNull Project project, @NotNull String toolWindowName, @NotNull String helpId, @Nullable Icon icon, @Nullable AnAction... actions) {
        myProject = project;
        myToolWindowName = toolWindowName;
        myHelpId = helpId;
        myIcon = icon;
        myActions = actions;
    }

    public synchronized void connectToProcessHandler(ProcessHandler handler) {
        init();
        if (myCurrentErrorTreeViewPanel != null) {
            myCurrentErrorTreeViewPanel.getErrorViewStructure().clear();
        }
        ApplicationManager.getApplication().assertIsDispatchThread();
        if (myToolWindow != null) {
            myConsoleView = new ConsoleViewImpl(myProject, GlobalSearchScope.allScope(myProject), true, false) {
            };
            myConsoleView.attachToProcess(handler);
            myConsoleContent = new ContentImpl(myConsoleView.getComponent(), "Console", false);
            myToolWindow.getContentManager().addContent(myConsoleContent);
        }
    }

    public synchronized void init() {
        if (!myFirstInitialized || myToolWindow == null) {
            ApplicationManager.getApplication().assertIsDispatchThread();
            ToolWindowManager manager = ToolWindowManager.getInstance(myProject);
            myToolWindow = manager.registerToolWindow(myToolWindowName, true, ToolWindowAnchor.BOTTOM, myProject, true);
            myToolWindow.setIcon(myIcon);
            createAllErrorsPanel();
            myFirstInitialized = true;
        }
    }

    private Content createCurrentErrorContent(final ToolWindow toolWindow) {
        final Ref<Content> contentRef = new Ref<>();
        myCurrentErrorTreeViewPanel = new JSGraphQLErrorTreeViewPanel(myProject, myHelpId, () -> myLastResult = null, myActions) {
            public void close() {
                toolWindow.hide(() -> {
                    myLastResult = null;
                    final Content contentImpl = contentRef.get();
                    if (contentImpl != null) {
                        toolWindow.getContentManager().removeContent(contentImpl, true);
                    }
                    Content newContentImpl = createCurrentErrorContent(toolWindow);
                    toolWindow.getContentManager().setSelectedContent(newContentImpl);
                });
            }
        };
        Disposer.register(myProject, myCurrentErrorTreeViewPanel);
        contentRef.set(new ContentImpl(myCurrentErrorTreeViewPanel, "Current Errors", false));
        toolWindow.getContentManager().addContent(contentRef.get(), 0);
        return contentRef.get();
    }

    private void createAllErrorsPanel() {
        ApplicationManager.getApplication().assertIsDispatchThread();
        if (myProjectErrorTreeViewPanel == null) {
            final Ref<Content> contentRef = new Ref<>();
            myProjectErrorTreeViewPanel = new JSGraphQLErrorTreeViewPanel(myProject, myHelpId, null, myActions) {
                public void close() {
                    final NewErrorTreeViewPanel oldPanel = myProjectErrorTreeViewPanel;
                    myProjectErrorTreeViewPanel = null;
                    myToolWindow.hide(() -> {
                        Content content = contentRef.get();
                        if (content != null) {
                            myToolWindow.getContentManager().removeContent(content, true);
                        }
                        if (oldPanel != null) {
                            Disposer.dispose(oldPanel);
                        }
                    });
                }
            };
            Disposer.register(myProject, myProjectErrorTreeViewPanel);
            contentRef.set(new ContentImpl(myProjectErrorTreeViewPanel, "Project Errors", false));
            myToolWindow.getContentManager().addContent(contentRef.get());

        }
    }

    public void disconnectFromProcessHandler() {
        ApplicationManager.getApplication().assertIsDispatchThread();
        if (myConsoleView != null && myConsoleContent != null && myToolWindow != null) {
            myToolWindow.getContentManager().removeContent(myConsoleContent, true);
            Disposer.dispose(myConsoleView);
            myConsoleView = null;
            myConsoleContent = null;
        }
    }

    private void show() {
        if (myToolWindow != null) {
            myToolWindow.show(null);
        }
    }

    private void setActivePanel(NewErrorTreeViewPanel panel, ToolWindow window) {
        final Content[] contents = window.getContentManager().getContents();
        for (Content content : contents) {
            if (content.getComponent() == panel) {
                window.getContentManager().setSelectedContent(content);
                break;
            }
        }
        show();
    }

    public void logCurrentErrors(@NotNull ImmutableList<JSGraphQLErrorResult> immutableResults, boolean setActive) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        if (myCurrentErrorTreeViewPanel == null) {
            if (!myFirstInitialized || myToolWindow == null) {
                return;
            }

            Content list = createCurrentErrorContent(myToolWindow);
            myToolWindow.getContentManager().setSelectedContent(list);
            show();
        }

        if (myFirstInitialized && myCurrentErrorTreeViewPanel != null) {
            final List<JSGraphQLErrorResult> results = ContainerUtil.newArrayList(immutableResults);
            if (!results.equals(myLastResult)) {
                logErrorsImpl(myCurrentErrorTreeViewPanel, results);
                myLastResult = results;
            }
        }

        if (setActive) {
            setActivePanel(myCurrentErrorTreeViewPanel, myToolWindow);
        }

    }

    public void dispose() {
        cleanPanel();
    }

    private static void logErrorsImpl(NewErrorTreeViewPanel errorTreeViewPanel, List<JSGraphQLErrorResult> list) {
        errorTreeViewPanel.getErrorViewStructure().clear();
        for (JSGraphQLErrorResult compilerResult : list) {
            String path = compilerResult.myFileAbsoluteSystemDependPath;
            VirtualFile file = null;
            if (path != null) {
                file = LocalFileSystem.getInstance().findFileByPath(FileUtil.toSystemIndependentName(path));
            }
            int category = "warning".equalsIgnoreCase(compilerResult.myCategory) ? MessageCategory.WARNING : MessageCategory.ERROR;
            errorTreeViewPanel.addMessage(category, new String[]{compilerResult.myErrorText}, file, compilerResult.myLine, compilerResult.myColumn, null);
        }
        errorTreeViewPanel.updateTree();
    }

    private synchronized void cleanPanel() {
        if (!myProject.isDefault()) {
            if (!myProject.isDisposed()) {
                ToolWindowManager.getInstance(myProject).unregisterToolWindow(myToolWindowName);
            }

            if (myCurrentErrorTreeViewPanel != null) {
                Disposer.dispose(myCurrentErrorTreeViewPanel);
            }

            if (myConsoleView != null) {
                Disposer.dispose(myConsoleView);
            }

            if (myProjectErrorTreeViewPanel != null) {
                Disposer.dispose(myProjectErrorTreeViewPanel);
            }

            myConsoleView = null;
            myProjectErrorTreeViewPanel = null;
            myToolWindow = null;
            myConsoleContent = null;
            myCurrentErrorTreeViewPanel = null;
            myLastResult = null;
        }
    }
}
