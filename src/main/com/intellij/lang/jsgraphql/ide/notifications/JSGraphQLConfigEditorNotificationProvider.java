/**
 *  Copyright (c) 2015, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.notifications;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.jsgraphql.JSGraphQLFileType;
import com.intellij.lang.jsgraphql.JSGraphQLParserDefinition;
import com.intellij.lang.jsgraphql.ide.project.JSGraphQLLanguageUIProjectService;
import com.intellij.lang.jsgraphql.languageservice.JSGraphQLNodeLanguageServiceInstance;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.PluginAware;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import com.intellij.ui.EditorNotifications.Provider;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JSGraphQLConfigEditorNotificationProvider extends Provider implements PluginAware {

    private final static Logger log = Logger.getInstance(JSGraphQLConfigEditorNotificationProvider.class);
    private static final Key<EditorNotificationPanel> KEY = Key.create("JSGraphQLConfigEditorNotificationProvider");
    private static final ExtensionPointName EDITOR_NOTIFICATION_EP = ExtensionPointName.create("com.intellij.editorNotificationProvider");
    private static final String SHOW_JSGRAPHQL_SCHEMA_EDITOR_NOTIFICATION = "jsgraphql-schema-editor-notification";

    public static final String GRAPHQL_CONFIG_JSON = "graphql.config.json";
    public static final String GRAPHQL_DEFAULT_SCHEMA = "graphql.schema.json";

    protected final Project myProject;
    protected final EditorNotifications myNotifications;
    private PluginDescriptor pluginDescriptor;

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
        if(hasGraphQLConfig()) {
            // already has config
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

    protected boolean hasGraphQLConfig() {
        if(myProject.getBaseDir() != null) {
            VirtualFile config = myProject.getBaseDir().findFileByRelativePath(GRAPHQL_CONFIG_JSON);
            return config != null;
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
            final VirtualFile baseDir = myProject.getBaseDir();
            if(baseDir != null) {
                VirtualFile defaultSchema = createFile(baseDir, GRAPHQL_DEFAULT_SCHEMA, GRAPHQL_DEFAULT_SCHEMA);
                VirtualFile config = createFile(baseDir, GRAPHQL_CONFIG_JSON, GRAPHQL_CONFIG_JSON);
                if(config != null && defaultSchema != null) {
                    JSGraphQLLanguageUIProjectService.showConsole(myProject);
                    FileEditorManager.getInstance(myProject).openFile(defaultSchema, false, true);
                    FileEditorManager.getInstance(myProject).openFile(config, true, true);
                    Notifications.Bus.notify(new Notification("GraphQL", "Created " + GRAPHQL_CONFIG_JSON + " and " + GRAPHQL_DEFAULT_SCHEMA, "Edit to load your own GraphQL schema.", NotificationType.INFORMATION));
                }
            }
            myNotifications.updateAllNotifications();
        };
    }

    private VirtualFile createFile(VirtualFile baseDir, String name, String resourceName) {
        VirtualFile file = baseDir.findFileByRelativePath(name);
        if(file == null) {
            Ref<VirtualFile> fileRef = new Ref<>();
            ApplicationManager.getApplication().runWriteAction(() -> {
                try {
                    fileRef.set(baseDir.createChildData(this, name));
                    try(OutputStream stream = fileRef.get().getOutputStream(this)) {
                        try(InputStream inputStream = pluginDescriptor.getPluginClassLoader().getResourceAsStream("/META-INF/"+resourceName)) {
                            if(inputStream != null) {
                                IOUtils.copy(inputStream, stream);
                            }
                        }
                    }
                } catch (IOException e) {
                    UIUtil.invokeLaterIfNeeded(() -> {
                        Notifications.Bus.notify(new Notification("GraphQL", "JS GraphQL", "Unable to create file '" + name + "' in project directory '" + baseDir.getPath() + "': " + e.getMessage(), NotificationType.ERROR));
                    });
                }
            });
            return fileRef.get();
        }
        return file;
    }

    @Override
    public void setPluginDescriptor(PluginDescriptor pluginDescriptor) {
        this.pluginDescriptor = pluginDescriptor;
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
