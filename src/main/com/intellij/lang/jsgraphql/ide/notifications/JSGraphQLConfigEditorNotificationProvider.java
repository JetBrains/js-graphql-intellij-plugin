/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.notifications;

import com.google.common.collect.Sets;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.jsgraphql.JSGraphQLFileType;
import com.intellij.lang.jsgraphql.JSGraphQLParserDefinition;
import com.intellij.lang.jsgraphql.ide.configuration.JSGraphQLConfigurationProvider;
import com.intellij.lang.jsgraphql.ide.findUsages.JSGraphQLFindUsagesUtil;
import com.intellij.lang.jsgraphql.ide.project.JSGraphQLLanguageUIProjectService;
import com.intellij.lang.jsgraphql.languageservice.JSGraphQLNodeLanguageServiceClient;
import com.intellij.lang.jsgraphql.languageservice.JSGraphQLNodeLanguageServiceInstance;
import com.intellij.lang.jsgraphql.schema.JSGraphQLSchemaFileType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import com.intellij.ui.EditorNotifications.Provider;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.webcore.ui.ModuleSelectionDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class JSGraphQLConfigEditorNotificationProvider extends Provider {

    private static final Key<EditorNotificationPanel> KEY = Key.create("JSGraphQLConfigEditorNotificationProvider");
    private static final ExtensionPointName EDITOR_NOTIFICATION_EP = ExtensionPointName.create("com.intellij.editorNotificationProvider");
    private static final String SHOW_JSGRAPHQL_SCHEMA_EDITOR_NOTIFICATION = "jsgraphql-schema-editor-notification";

    protected final Project myProject;
    protected final EditorNotifications myNotifications;

    @NotNull
    public Key getKey() {
       return KEY;
    }

    public JSGraphQLConfigEditorNotificationProvider(Project project, EditorNotifications notifications) {
        myProject = project;
        myNotifications = notifications;
    }

    @Nullable
    public JComponent createNotificationPanel(@NotNull VirtualFile file, @NotNull FileEditor fileEditor) {
        return !hasOverride() && showNotification(file) ? new SchemaConfigEditorNotificationPanel(file) : null;
    }

    protected boolean hasOverride() {
        final Object[] extensions = EDITOR_NOTIFICATION_EP.getExtensions(myProject);
        for (Object extension : extensions) {
            if(extension instanceof JSGraphQLConfigEditorNotificationProvider.NotificationOverride) {
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
        if(!PropertiesComponent.getInstance(myProject).getBoolean(SHOW_JSGRAPHQL_SCHEMA_EDITOR_NOTIFICATION, true)) {
            // ignored for project
            return false;
        }
        if(!isGraphQLRelatedFile(file)) {
            return false;
        }
        if(JSGraphQLNodeLanguageServiceInstance.getNodeInterpreter(myProject) == null) {
            // didn't configure node yet
            return false;
        }
        if(JSGraphQLConfigurationProvider.getService(myProject).hasGraphQLConfig()) {
            // already has config
            return false;
        }
        if(file.getFileType() == JSGraphQLSchemaFileType.INSTANCE) {
            return true;
        }
        return GlobalSearchScope.projectScope(myProject).accept(file);
    }

    protected boolean isGraphQLRelatedFile(VirtualFile file) {
        if(file.getFileType() == JSGraphQLFileType.INSTANCE || file.getFileType() == JSGraphQLSchemaFileType.INSTANCE) {
            return true;
        }
        if(JSGraphQLFindUsagesUtil.INCLUDED_FILE_TYPES.contains(file.getFileType())) {
            return true;
        }
        return false;
    }

    @NotNull
    protected Runnable getDismissAction() {
        return () -> {
            PropertiesComponent.getInstance(myProject).setValue(SHOW_JSGRAPHQL_SCHEMA_EDITOR_NOTIFICATION, Boolean.FALSE.toString());
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
            final JSGraphQLConfigurationProvider configurationProvider = JSGraphQLConfigurationProvider.getService(myProject);
            boolean setProjectDir = false;
            if(configurationProvider.getConfigurationBaseDir() == null) {
                // couldn't detect the config dir automatically, so we have to ask which module to place the config in
                final JSGraphQLConfigModuleDialog dialog = new JSGraphQLConfigModuleDialog(myProject);
                if(dialog.showAndGet()) {
                    final Module module = dialog.getSelectedModule();
                    if(module != null) {
                        configurationProvider.setConfigurationBasDirFromModule(module);
                        // we should set the project dir when we've created the config files
                        // since we couldn't detect it before
                        setProjectDir = true;
                    }
                }
            }
            if(configurationProvider.getConfigurationBaseDir() == null) {
                // didn't detect or select the base dir yet
                return;
            }
            VirtualFile defaultSchema = configurationProvider.getOrCreateFile(JSGraphQLConfigurationProvider.GRAPHQL_DEFAULT_SCHEMA);
            VirtualFile config = configurationProvider.getOrCreateFile(JSGraphQLConfigurationProvider.GRAPHQL_CONFIG_JSON);
            if(config != null && defaultSchema != null) {
                JSGraphQLLanguageUIProjectService.showConsole(myProject);
                FileEditorManager.getInstance(myProject).openFile(defaultSchema, false, true);
                FileEditorManager.getInstance(myProject).openFile(config, true, true);
                if(setProjectDir) {
                    // fetch tokens from the client to force it to set the project dir base on the module we picked
                    JSGraphQLNodeLanguageServiceClient.getTokens("", myProject);
                }
                Notifications.Bus.notify(new Notification("GraphQL", "Created " + JSGraphQLConfigurationProvider.GRAPHQL_CONFIG_JSON + " and " + JSGraphQLConfigurationProvider.GRAPHQL_DEFAULT_SCHEMA, "Edit to load your own GraphQL schema.", NotificationType.INFORMATION));
            }
            myNotifications.updateAllNotifications();
        };
    }

    protected class SchemaConfigEditorNotificationPanel extends EditorNotificationPanel {
        public SchemaConfigEditorNotificationPanel(final VirtualFile file) {
            setText("GraphQL language features are based on a graphql.config.json file that configures your schema and endpoint locations. Create one now?");
            createActionLabel("Create a graphql.config.json", getConfigureAction());
            createActionLabel("No thanks", getDismissAction());
        }

        public Color getBackground() {
            return getColor(this);
        }
    }

    public interface NotificationOverride {
    }
}
