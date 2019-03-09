/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.editor;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.intellij.ide.actions.CreateFileAction;
import com.intellij.ide.impl.DataManagerImpl;
import com.intellij.lang.jsgraphql.GraphQLSettings;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigEndpoint;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigVariableAwareEndpoint;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaKeys;
import com.intellij.lang.jsgraphql.v1.ide.project.JSGraphQLLanguageUIProjectService;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import graphql.GraphQLException;
import graphql.introspection.IntrospectionQuery;
import graphql.language.Description;
import graphql.language.Document;
import graphql.language.Node;
import graphql.language.ScalarTypeDefinition;
import graphql.schema.idl.SchemaPrinter;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.intellij.lang.jsgraphql.v1.ide.project.JSGraphQLLanguageUIProjectService.setHeadersFromOptions;

public class GraphQLIntrospectionHelper {

    private GraphQLIntrospectionTask latestIntrospection = null;

    public static GraphQLIntrospectionHelper getService(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLIntrospectionHelper.class);
    }

    public GraphQLIntrospectionHelper(Project project) {
        if (project != null) {
            project.getMessageBus().connect().subscribe(GraphQLConfigManager.TOPIC, () -> latestIntrospection = null);
        }
    }

    public void performIntrospectionQueryAndUpdateSchemaPathFile(Project project, GraphQLConfigEndpoint endpoint) {
        final VirtualFile configFile = GraphQLConfigManager.getService(project).getClosestConfigFile(endpoint.configPackageSet.getConfigBaseDir());
        if (configFile != null) {
            final String schemaPath = endpoint.configPackageSet.getConfigData().schemaPath;
            if (schemaPath == null || schemaPath.trim().isEmpty()) {
                Notifications.Bus.notify(new Notification("GraphQL", "Unable to perform introspection", "Please set a non-empty 'schemaPath' field in the <a href=\"edit-config\">" + configFile.getName() + "</a> file. The introspection result will be written to that file.", NotificationType.WARNING, (notification, event) -> {
                    FileEditorManager.getInstance(project).openFile(configFile, true);
                }));
                return;
            }

            performIntrospectionQueryAndUpdateSchemaPathFile(project, new GraphQLConfigVariableAwareEndpoint(endpoint, project), schemaPath, configFile);
        }

    }

    public void performIntrospectionQueryAndUpdateSchemaPathFile(Project project, GraphQLConfigVariableAwareEndpoint endpoint, String schemaPath, VirtualFile introspectionSourceFile) {

        latestIntrospection = new GraphQLIntrospectionTask(endpoint, () -> performIntrospectionQueryAndUpdateSchemaPathFile(project, endpoint, schemaPath, introspectionSourceFile));

        final NotificationAction retry = new NotificationAction("Retry") {

            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                notification.expire();
                performIntrospectionQueryAndUpdateSchemaPathFile(project, endpoint, schemaPath, introspectionSourceFile);
            }
        };

        final String url = endpoint.getUrl();

        final HttpClient httpClient = new HttpClient(new HttpClientParams());

        try {

            final GraphQLSettings graphQLSettings = GraphQLSettings.getSettings(project);
            String query = graphQLSettings.getIntrospectionQuery();
            if (StringUtils.isBlank(query)) {
                query = IntrospectionQuery.INTROSPECTION_QUERY;
            }
            if (!graphQLSettings.isEnableIntrospectionDefaultValues()) {
                query = query.replace("defaultValue", "");
            }

            final String requestJson = "{\"query\":\"" + StringEscapeUtils.escapeJavaScript(query) + "\"}";

            final PostMethod method = new PostMethod(url);
            method.setRequestEntity(new StringRequestEntity(requestJson, "application/json", "UTF-8"));

            setHeadersFromOptions(endpoint, method);

            final Task.Backgroundable task = new Task.Backgroundable(project, "Executing GraphQL Introspection Query", false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    indicator.setIndeterminate(true);
                    try {
                        httpClient.executeMethod(method);
                        final String responseJson = sanitizeIntrospectionJson(Optional.ofNullable(method.getResponseBodyAsString()).orElse(""));
                        ApplicationManager.getApplication().invokeLater(() -> {
                            try {
                                JSGraphQLLanguageUIProjectService.getService(project).showQueryResult(responseJson, JSGraphQLLanguageUIProjectService.QueryResultDisplay.ON_ERRORS_ONLY);
                                IntrospectionOutputFormat format = schemaPath.endsWith(".json") ? IntrospectionOutputFormat.JSON : IntrospectionOutputFormat.SDL;
                                final String schemaAsSDL = printIntrospectionJsonAsGraphQL(responseJson); // always try to print the schema to validate it since that will be done in schema discovery of the JSON anyway
                                final String schemaText = format == IntrospectionOutputFormat.SDL ? schemaAsSDL : responseJson;
                                createOrUpdateIntrospectionOutputFile(schemaText, format, introspectionSourceFile, schemaPath, project);
                            } catch (Exception e) {
                                final Notification notification = new Notification(
                                        "GraphQL",
                                        "GraphQL Introspection Error",
                                        "A valid schema could not be built using the introspection result: " + e.getMessage(),
                                        NotificationType.WARNING
                                ).addAction(retry).setImportant(true);
                                if (e instanceof GraphQLException) {
                                    final String content = "A valid schema could not be built using the introspection result. The endpoint may not follow the GraphQL Specification. The error was:\n\""+e.getMessage()+ "\".";
                                    notification.setContent(content);
                                    if (GraphQLSettings.getSettings(project).isEnableIntrospectionDefaultValues()) {
                                        // suggest retrying without the default values as they're a common cause of spec compliance issues
                                        final NotificationAction retryWithoutDefaultValues = new NotificationAction("Retry (skip default values from now on)") {
                                            @Override
                                            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                                                GraphQLSettings.getSettings(project).setEnableIntrospectionDefaultValues(false);
                                                ApplicationManager.getApplication().saveSettings();
                                                notification.expire();
                                                performIntrospectionQueryAndUpdateSchemaPathFile(project, endpoint, schemaPath, introspectionSourceFile);
                                            }
                                        };
                                        notification.addAction(retryWithoutDefaultValues);
                                    }
                                }
                                Notifications.Bus.notify(notification, project);
                            }
                        });
                    } catch (IOException e) {
                        Notifications.Bus.notify(new Notification("GraphQL", "GraphQL Query Error", url + ": " + e.getMessage(), NotificationType.WARNING).addAction(retry), project);
                    }
                }
            };
            ProgressManager.getInstance().run(task);

        } catch (UnsupportedEncodingException | IllegalStateException | IllegalArgumentException e) {
            Notifications.Bus.notify(new Notification("GraphQL", "GraphQL Query Error", url + ": " + e.getMessage(), NotificationType.ERROR).addAction(retry), project);
        }
    }

    /**
     * Ensures that the JSON response falls within the GraphQL specification character range such that it can be expressed as valid GraphQL SDL in the editor
     * @param introspectionJson the JSON to sanitize
     * @return a sanitized version where the character ranges are within those allowed by the GraphQL Language Specification
     */
    private String sanitizeIntrospectionJson(String introspectionJson) {
        // Strip out emojis (e.g. the one in the GitHub schema) since they're outside the allowed range
        return introspectionJson.replaceAll("[\ud83c\udf00-\ud83d\ude4f]|[\ud83d\ude80-\ud83d\udeff]", "");
    }

    @SuppressWarnings("unchecked")
    public String printIntrospectionJsonAsGraphQL(String introspectionJson) {
        Map<String, Object> introspectionAsMap = new Gson().fromJson(introspectionJson, Map.class);
        if (!introspectionAsMap.containsKey("__schema")) {
            // possibly a full query result
            if (introspectionAsMap.containsKey("errors")) {
                throw new IllegalArgumentException("Introspection query returned errors: " + new Gson().toJson(introspectionAsMap.get("errors")));
            }
            if (!introspectionAsMap.containsKey("data")) {
                throw new IllegalArgumentException("Expected data key to be present in query result. Got keys: " + introspectionAsMap.keySet());
            }
            introspectionAsMap = (Map<String, Object>) introspectionAsMap.get("data");
            if (!introspectionAsMap.containsKey("__schema")) {
                throw new IllegalArgumentException("Expected __schema key to be present in query result data. Got keys: " + introspectionAsMap.keySet());
            }
        }
        final Document schemaDefinition = new GraphQLIntrospectionResultToSchema().createSchemaDefinition(introspectionAsMap);
        final SchemaPrinter.Options options = SchemaPrinter.Options.defaultOptions().includeScalarTypes(false).includeSchemaDefintion(true);
        final StringBuilder sb = new StringBuilder(new SchemaPrinter(options).print(schemaDefinition));

        // graphql-java only prints scalars that are used by fields since it visits fields to discover types, so add the scalars here manually
        final Set<String> knownScalars = Sets.newHashSet();
        for (Node definition : schemaDefinition.getChildren()) {
            if (definition instanceof ScalarTypeDefinition) {
                final ScalarTypeDefinition scalarTypeDefinition = (ScalarTypeDefinition) definition;
                String scalarName = scalarTypeDefinition.getName();
                if (knownScalars.add(scalarName)) {
                    sb.append("\n");
                    final Description description = scalarTypeDefinition.getDescription();
                    if (description != null) {
                        if (description.isMultiLine()) {
                            sb.append("\"\"\"").append(description.getContent()).append("\"\"\"");
                        } else {
                            sb.append("\"").append(description.getContent()).append("\"");
                        }
                        sb.append("\n");
                    }
                    sb.append("scalar ").append(scalarName);
                }
            }
        }
        return sb.toString();
    }

    public GraphQLIntrospectionTask getLatestIntrospection() {
        return latestIntrospection;
    }

    enum IntrospectionOutputFormat {
        JSON,
        SDL
    }

    void createOrUpdateIntrospectionOutputFile(String schemaText, IntrospectionOutputFormat format, VirtualFile introspectionSourceFile, String outputFileName, Project project) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                final String header;
                switch (format) {
                    case SDL:
                        header = "# This file was generated based on \"" + introspectionSourceFile.getName() + "\" at " + new Date() + ". Do not edit manually.\n\n";
                        break;
                    case JSON:
                        header = "";
                        break;
                    default:
                        throw new IllegalArgumentException("unsupported output format: " + format);
                }
                String relativeOutputFileName = StringUtils.replaceChars(outputFileName, '\\', '/');
                VirtualFile outputFile = introspectionSourceFile.getParent().findFileByRelativePath(relativeOutputFileName);
                if (outputFile == null) {
                    PsiDirectory directory = PsiDirectoryFactory.getInstance(project).createDirectory(introspectionSourceFile.getParent());
                    CreateFileAction.MkDirs dirs = new CreateFileAction.MkDirs(relativeOutputFileName, directory);
                    outputFile = dirs.directory.getVirtualFile().createChildData(introspectionSourceFile, dirs.newName);
                }
                outputFile.putUserData(GraphQLSchemaKeys.IS_GRAPHQL_INTROSPECTION_JSON, true);
                final FileEditor[] fileEditors = FileEditorManager.getInstance(project).openFile(outputFile, true, true);
                if (fileEditors.length > 0) {
                    final FileEditor fileEditor = fileEditors[0];
                    setEditorTextAndFormatLines(header + schemaText, fileEditor);
                } else {
                    Notifications.Bus.notify(new Notification("GraphQL", "GraphQL Error", "Unable to open an editor for '" + outputFile.getPath() + "'", NotificationType.ERROR));
                }
            } catch (IOException ioe) {
                Notifications.Bus.notify(new Notification("GraphQL", "GraphQL IO Error", "Unable to create file '" + outputFileName + "' in directory '" + introspectionSourceFile.getParent().getPath() + "': " + ioe.getMessage(), NotificationType.ERROR));
            }
        });
    }

    void setEditorTextAndFormatLines(String text, FileEditor fileEditor) {
        if (fileEditor instanceof TextEditor) {
            final Editor editor = ((TextEditor) fileEditor).getEditor();
            editor.getDocument().setText(text);
            AnAction reformatCode = ActionManager.getInstance().getAction("ReformatCode");
            if (reformatCode != null) {
                final AnActionEvent actionEvent = AnActionEvent.createFromDataContext(
                        ActionPlaces.UNKNOWN,
                        null,
                        new DataManagerImpl.MyDataContext(editor.getComponent())
                );
                reformatCode.actionPerformed(actionEvent);
            }

        }
    }

}
