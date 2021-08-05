/*
 * Copyright (c) 2019-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.graphqlconfig;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.ide.BrowserUtil;
import com.intellij.json.JsonFileType;
import com.intellij.lang.jsgraphql.ide.notifications.GraphQLNotificationUtil;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.legacy.GraphQLConfigJsonConfiguration;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.legacy.GraphQLConfigJsonEndpoint;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.legacy.GraphQLConfigJsonSchemaConfiguration;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigData;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigEndpoint;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.source.codeStyle.CodeStyleManagerImpl;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager.ENDPOINTS_EXTENSION;

/**
 * Helper to migrate from the old graphql.config.json configuration format to .graphqlconfig (graphql-config)
 */
public final class GraphQLConfigMigrationHelper {

    public static final String GRAPHQL_CONFIG_DOCS = "https://github.com/kamilkisiela/graphql-config/tree/legacy";

    public static void checkGraphQLConfigJsonMigrations(Project project) {

        final Task.Backgroundable task = new Task.Backgroundable(project, "Verifying GraphQL configuration", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                final GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
                final Collection<VirtualFile> legacyConfigFiles = ApplicationManager.getApplication().runReadAction(
                    (Computable<Collection<VirtualFile>>) () -> FilenameIndex.getVirtualFilesByName(project, "graphql.config.json", scope)
                );
                for (VirtualFile virtualFile : legacyConfigFiles) {
                    if (!virtualFile.isDirectory() && virtualFile.isInLocalFileSystem()) {
                        boolean migrate = true;
                        for (String fileName : GraphQLConfigManager.GRAPHQLCONFIG_FILE_NAMES) {
                            if (virtualFile.getParent().findChild(fileName) != null) {
                                migrate = false;
                                break;
                            }
                        }
                        if (migrate) {
                            createMigrationNotification(project, virtualFile);
                        }
                    }
                }
            }
        };
        ProgressManager.getInstance().run(task);
    }

    static void createMigrationNotification(Project project, VirtualFile configurationFile) {
        try (InputStream inputStream = configurationFile.getInputStream()) {
            final String configJson = IOUtils.toString(inputStream, configurationFile.getCharset());
            final GraphQLConfigJsonConfiguration legacyConfiguration = new Gson().fromJson(configJson, GraphQLConfigJsonConfiguration.class);

            final GraphQLConfigJsonSchemaConfiguration schema = legacyConfiguration.schema;
            if (schema != null) {

                // convert legacy schema discovery
                final GraphQLConfigData newConfiguration = new GraphQLConfigData();
                final Map<String, GraphQLConfigEndpoint> newEndpoints = Maps.newLinkedHashMap();
                newConfiguration.extensions = Maps.newHashMap();
                newConfiguration.extensions.put(ENDPOINTS_EXTENSION, newEndpoints);

                boolean canMigrate = false;
                if (schema.file != null) {
                    newConfiguration.schemaPath = schema.file;
                    canMigrate = true;
                } else if (schema.request != null) {
                    if (Boolean.TRUE.equals(schema.request.postIntrospectionQuery)) {
                        newConfiguration.schemaPath = "schema.graphql";
                        final GraphQLConfigEndpoint newEndpoint = convertEndpoint(schema.request);
                        newEndpoints.put("Default Introspection Endpoint", newEndpoint);
                        canMigrate = true;
                    }
                }

                if (canMigrate) {
                    if (legacyConfiguration.endpoints != null) {
                        for (GraphQLConfigJsonEndpoint legacyEndpoint : legacyConfiguration.endpoints) {
                            GraphQLConfigEndpoint newEndpoint = convertEndpoint(legacyEndpoint);
                            newEndpoints.put(Optional.ofNullable(legacyEndpoint.name).orElse(legacyEndpoint.url), newEndpoint);
                        }
                    }

                    // show migration
                    Notification migrateNotification = new Notification(
                        GraphQLNotificationUtil.NOTIFICATION_GROUP_ID,
                        "GraphQL configuration migration required",
                        "The <a href=\"config-v1\">graphql.config.json</a> file is deprecated.",
                        NotificationType.INFORMATION
                    ).setImportant(true).setListener((notification, event) -> {
                        if ("config-v1".equals(event.getDescription())) {
                            FileEditorManager.getInstance(project).openFile(configurationFile, true);
                        }
                    });

                    migrateNotification.addAction(new NotificationAction("Migrate to .graphqlconfig") {
                        @Override
                        public void actionPerformed(@NotNull AnActionEvent evt, @NotNull Notification notification) {
                            GraphQLConfigManager.getService(project).createAndOpenConfigFile(configurationFile.getParent(), true, outputStream -> {
                                final String newConfigJson = new Gson().toJson(newConfiguration);
                                try {
                                    final PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
                                    final PsiFile jsonPsiFile = psiFileFactory.createFileFromText("", JsonFileType.INSTANCE, newConfigJson);
                                    CodeStyleManagerImpl.getInstance(project).reformat(jsonPsiFile);
                                    Document document = jsonPsiFile.getViewProvider().getDocument();
                                    String formattedJson;
                                    if (document != null) {
                                        formattedJson = document.getText();
                                    } else {
                                        formattedJson = jsonPsiFile.getText();
                                    }
                                    outputStream.write(formattedJson.getBytes(configurationFile.getCharset()));
                                } catch (IOException e) {
                                    throw new RuntimeException("Unable to create GraphQL configuration", e);
                                }
                            });
                            notification.expire();
                        }
                    });
                    migrateNotification.addAction(new NotificationAction("About graphql-config") {

                        @Override
                        public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                            BrowserUtil.browse(GRAPHQL_CONFIG_DOCS);
                        }
                    });
                    Notifications.Bus.notify(migrateNotification);
                    return;
                }
            }

        } catch (IOException | JsonSyntaxException e) {
            Notifications.Bus.notify(new Notification(
                GraphQLNotificationUtil.NOTIFICATION_GROUP_ID,
                "Unable to read " + configurationFile.getPresentableName(),
                e.getMessage(),
                NotificationType.ERROR)
            );
        }

        // fallback is to notify that work is required to setup the project
        Notifications.Bus.notify(new Notification(
            GraphQLNotificationUtil.NOTIFICATION_GROUP_ID,
            "Unable to migrate graphql.config.json",
            "Your <a href=\"config-v1\">graphql.config.json</a> could not be migrated to <a href=\"config-v2\">.graphqlconfig</a>. Schema discovery and endpoints is using defaults.<div style='margin-top: 6px'><a href=\"graphql-config\">About graphql-config</a></div>",
            NotificationType.WARNING
        ).setImportant(true).setListener((notification, event) -> {
            if ("config-v1".equals(event.getDescription())) {
                FileEditorManager.getInstance(project).openFile(configurationFile, true);
            } else if ("config-v2".equals(event.getDescription())) {
                GraphQLConfigManager.getService(project).createAndOpenConfigFile(configurationFile.getParent(), true);
            } else if ("graphql-config".equals(event.getDescription())) {
                BrowserUtil.browse(GRAPHQL_CONFIG_DOCS);
            }
        }));
    }

    @SuppressWarnings("unchecked")
    private static GraphQLConfigEndpoint convertEndpoint(GraphQLConfigJsonEndpoint legacyEndpoint) {
        GraphQLConfigEndpoint newEndpoint = new GraphQLConfigEndpoint(null, null, legacyEndpoint.url);
        newEndpoint.introspect = legacyEndpoint.postIntrospectionQuery;
        if (legacyEndpoint.options != null) {
            final Object legacyHeaders = legacyEndpoint.options.get("headers");
            if (legacyHeaders instanceof Map) {
                newEndpoint.headers = (Map<String, Object>) legacyHeaders;
            }
        }
        return newEndpoint;
    }

}
