/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.notifications;

import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.jsgraphql.JSGraphQLFileType;
import com.intellij.lang.jsgraphql.JSGraphQLParserDefinition;
import com.intellij.lang.jsgraphql.languageservice.JSGraphQLNodeLanguageServiceInstance;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import com.intellij.ui.EditorNotifications.Provider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class JSGraphQLNodeInterpreterEditorNotificationProvider extends Provider {

    private static final Key<EditorNotificationPanel> KEY = Key.create("JSGraphQLNodeInterpreterEditorNotificationProvider");
    private static final ExtensionPointName EDITOR_NOTIFICATION_EP = ExtensionPointName.create("com.intellij.editorNotificationProvider");
    private static final String SHOW_JSGRAPHQL_NODE_EDITOR_NOTIFICATION = "jsgraphql-node-editor-notification";

    protected final Project myProject;
    protected final EditorNotifications myNotifications;

    @NotNull
    public Key getKey() {
       return KEY;
    }

    public JSGraphQLNodeInterpreterEditorNotificationProvider(Project project, EditorNotifications notifications) {
        myProject = project;
        myNotifications = notifications;
    }

    @Nullable
    public JComponent createNotificationPanel(@NotNull VirtualFile file, @NotNull FileEditor fileEditor) {
        return !hasOverride() && showNotification(file) ? new NodeInterpreterEditorNotificationPanel(file) : null;
    }

    protected boolean hasOverride() {
        final Object[] extensions = EDITOR_NOTIFICATION_EP.getExtensions(myProject);
        for (Object extension : extensions) {
            if(extension instanceof JSGraphQLNodeInterpreterEditorNotificationProvider.NotificationOverride) {
                return true;
            }
        }
        return false;
    }

    protected boolean showNotification(@NotNull VirtualFile file) {
        if(!Boolean.TRUE.equals(myProject.getUserData(JSGraphQLParserDefinition.JSGRAPHQL_ACTIVATED))) {
            // not using GraphQL at the moment
            return false;
        }
        if(!PropertiesComponent.getInstance(myProject).getBoolean(SHOW_JSGRAPHQL_NODE_EDITOR_NOTIFICATION, true)) {
            // ignored for project
            return false;
        }
        if(!isGraphQLRelatedFile(file)) {
            return false;
        }
        if(JSGraphQLNodeLanguageServiceInstance.getNodeInterpreter(myProject) != null) {
            // already configured node
            return false;
        }
        return GlobalSearchScope.projectScope(myProject).accept(file);
    }

    protected boolean isGraphQLRelatedFile(VirtualFile file) {
        if(file.getFileType() == JSGraphQLFileType.INSTANCE) {
            return true;
        }
        if(JavaScriptFileType.getFileTypesCompilableToJavaScript().contains(file.getFileType())) {
            return true;
        }
        return false;
    }


    @NotNull
    protected Runnable getDismissAction() {
        return () -> {
            PropertiesComponent.getInstance(myProject).setValue(SHOW_JSGRAPHQL_NODE_EDITOR_NOTIFICATION, Boolean.FALSE.toString());
            myNotifications.updateAllNotifications();
        };
    }

    protected Color getColor(EditorNotificationPanel panel) {
        Color color = EditorColorsManager.getInstance().getGlobalScheme().getColor(EditorColors.GUTTER_BACKGROUND);
        return color != null ? color : panel.getBackground();
    }

    @NotNull
    protected Runnable getConfigureAction() {
        return () -> {
            ShowSettingsUtilImpl.showSettingsDialog(myProject, "settings.nodejs", "Node interpreter");
            myNotifications.updateAllNotifications();
        };
    }

    protected class NodeInterpreterEditorNotificationPanel extends EditorNotificationPanel {
        public NodeInterpreterEditorNotificationPanel(final VirtualFile file) {
            setText("Please configure the Node.js interpreter to enable GraphQL language features.");
            createActionLabel("Configure...", getConfigureAction());
            createActionLabel("Ignore", getDismissAction());
        }

        public Color getBackground() {
            return getColor(this);
        }
    }

    public interface NotificationOverride {
    }
}
