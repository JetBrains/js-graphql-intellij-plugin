/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.notifications;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.jsgraphql.v1.ide.injection.JSGraphQLLanguageInjectionUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import com.intellij.ui.EditorNotifications.Provider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Editor notification that asks the user to decide between Apollo and Lokka for gql tagged templates.
 */
public class JSGraphQLApolloLokkaEditorNotificationProvider extends Provider {

    private static final Key<EditorNotificationPanel> KEY = Key.create("JSGraphQLApolloLokkaEditorNotificationProvider");
    private static final ExtensionPointName EDITOR_NOTIFICATION_EP = ExtensionPointName.create("com.intellij.editorNotificationProvider");

    private final Project myProject;
    private final EditorNotifications myNotifications;

    @NotNull
    public Key getKey() {
        return KEY;
    }

    public JSGraphQLApolloLokkaEditorNotificationProvider(Project project, EditorNotifications notifications) {
        myProject = project;
        myNotifications = notifications;
    }

    @Nullable
    public JComponent createNotificationPanel(@NotNull VirtualFile file, @NotNull FileEditor fileEditor) {
        return !hasOverride() && showNotification(file) ? new ApolloLokkaEditorNotificationPanel() : null;
    }

    private boolean hasOverride() {
        final Object[] extensions = EDITOR_NOTIFICATION_EP.getExtensions(myProject);
        for (Object extension : extensions) {
            if (extension instanceof JSGraphQLApolloLokkaEditorNotificationProvider.NotificationOverride) {
                return true;
            }
        }
        return false;
    }

    private boolean showNotification(@NotNull VirtualFile file) {
        if (!isGraphQLInjectionRelatedFile(file)) {
            return false;
        }
        if (!JSGraphQLLanguageInjectionUtil.isGQLEnvironmentConfigured(myProject)) {
            // no gql configured yet
            final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);
            if (psiFile != null) {
                final String environment = JSGraphQLLanguageInjectionUtil.getEnvironment(psiFile);
                if (JSGraphQLLanguageInjectionUtil.DEFAULT_GQL_ENVIRONMENT.equals(environment)) {
                    // default gql is in use in this file, so show the option to configure
                    return GlobalSearchScope.projectScope(myProject).accept(file);
                }
            }
        }
        return false;
    }

    private boolean isGraphQLInjectionRelatedFile(VirtualFile file) {
        return JavaScriptFileType.getFileTypesCompilableToJavaScript().contains(file.getFileType());
    }

    private Color getColor(EditorNotificationPanel panel) {
        Color color = EditorColorsManager.getInstance().getGlobalScheme().getColor(EditorColors.GUTTER_BACKGROUND);
        return color != null ? color : panel.getBackground();
    }

    @NotNull
    private Runnable getApolloAction() {
        return () -> setGQLEnv(JSGraphQLLanguageInjectionUtil.APOLLO_ENVIRONMENT);
    }

    @NotNull
    private Runnable getLokkaAction() {
        return () -> setGQLEnv(JSGraphQLLanguageInjectionUtil.LOKKA_ENVIRONMENT);
    }

    private void setGQLEnv(String env) {
        JSGraphQLLanguageInjectionUtil.setGQLEnvironment(myProject, env);
        DaemonCodeAnalyzer.getInstance(myProject).restart();
        myNotifications.updateAllNotifications();
        Notifications.Bus.notify(new Notification("GraphQL", "Project gql environment updated", "Using \"" + env + "\" gql tagged GraphQL templates. <a href=\"undo\">Undo</a>", NotificationType.INFORMATION, (notification, event) -> {
            JSGraphQLLanguageInjectionUtil.setGQLEnvironment(myProject, null);
            DaemonCodeAnalyzer.getInstance(myProject).restart();
            myNotifications.updateAllNotifications();
            notification.expire();
        }));
    }

    private class ApolloLokkaEditorNotificationPanel extends EditorNotificationPanel {
        ApolloLokkaEditorNotificationPanel() {
            setText("Please select the GraphQL Client you want to use with gql tagged templates in this project...");
            createActionLabel("Apollo", getApolloAction());
            createActionLabel("Lokka", getLokkaAction());
        }

        public Color getBackground() {
            return getColor(this);
        }
    }

    interface NotificationOverride {
    }
}
