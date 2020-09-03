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
import com.google.gson.JsonSyntaxException;
import com.intellij.ide.actions.CreateFileAction;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.lang.jsgraphql.GraphQLSettings;
import com.intellij.lang.jsgraphql.ide.notifications.GraphQLNotificationUtil;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigEndpoint;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigVariableAwareEndpoint;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaKeys;
import com.intellij.lang.jsgraphql.v1.ide.project.JSGraphQLLanguageUIProjectService;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.EditorBundle;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.util.Consumer;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.ObjectUtils;
import graphql.introspection.IntrospectionQuery;
import graphql.language.*;
import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import graphql.util.EscapeUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.intellij.lang.jsgraphql.v1.ide.project.JSGraphQLLanguageUIProjectService.setHeadersFromOptions;

public class GraphQLIntrospectionService implements Disposable {

    private static final Set<String> DEFAULT_DIRECTIVES = Sets.newHashSet("deprecated", "skip", "include", "specifiedBy");
    private static final String DISABLE_EMPTY_ERRORS_WARNING_KEY = "graphql.empty.errors.warning.disabled";

    private GraphQLIntrospectionTask latestIntrospection = null;
    private final Project myProject;

    public static GraphQLIntrospectionService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLIntrospectionService.class);
    }

    public GraphQLIntrospectionService(Project project) {
        myProject = project;
        if (project != null) {
            project.getMessageBus().connect(this).subscribe(GraphQLConfigManager.TOPIC, () -> latestIntrospection = null);
        }
    }

    public void performIntrospectionQueryAndUpdateSchemaPathFile(Project project, GraphQLConfigEndpoint endpoint) {
        final VirtualFile configFile = GraphQLConfigManager.getService(project).getClosestConfigFile(endpoint.configPackageSet.getConfigBaseDir());
        if (configFile != null) {
            final String schemaPath = endpoint.configPackageSet.getConfigData().schemaPath;
            if (StringUtil.isEmptyOrSpaces(schemaPath)) {
                GraphQLNotificationUtil.showInvalidConfigurationNotification(GraphQLBundle.message("graphql.notification.empty.schema.path"), configFile, myProject);
                return;
            }

            performIntrospectionQueryAndUpdateSchemaPathFile(new GraphQLConfigVariableAwareEndpoint(endpoint, project), schemaPath, configFile);
        }

    }

    public void performIntrospectionQueryAndUpdateSchemaPathFile(GraphQLConfigVariableAwareEndpoint endpoint,
                                                                 String schemaPath,
                                                                 VirtualFile introspectionSourceFile) {
        latestIntrospection = new GraphQLIntrospectionTask(endpoint, () -> performIntrospectionQueryAndUpdateSchemaPathFile(endpoint, schemaPath, introspectionSourceFile));

        final NotificationAction retry = new NotificationAction(GraphQLBundle.message("graphql.notification.retry")) {

            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                notification.expire();
                performIntrospectionQueryAndUpdateSchemaPathFile(endpoint, schemaPath, introspectionSourceFile);
            }
        };

        String url = endpoint.getUrl();
        if (StringUtil.isEmptyOrSpaces(url)) {
            GraphQLNotificationUtil.showInvalidConfigurationNotification(GraphQLBundle.message("graphql.notification.empty.endpoint.url"), introspectionSourceFile, myProject);
            return;
        }

        try {
            final GraphQLSettings graphQLSettings = GraphQLSettings.getSettings(myProject);
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

            Task.Backgroundable task = new IntrospectionQueryTask(method, schemaPath, introspectionSourceFile, retry, graphQLSettings, endpoint, url);
            ProgressManager.getInstance().run(task);
        } catch (UnsupportedEncodingException | IllegalStateException | IllegalArgumentException e) {
            GraphQLNotificationUtil.showGraphQLRequestErrorNotification(retry, url, e, NotificationType.ERROR, myProject);
        }
    }

    public void addIntrospectionStackTraceAction(@NotNull Notification notification, @NotNull Exception exception) {
        notification.addAction(new NotificationAction(GraphQLBundle.message("graphql.notification.stack.trace")) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                String stackTrace = ExceptionUtil.getThrowableText(exception);
                PsiFile file = PsiFileFactory.getInstance(myProject)
                    .createFileFromText("introspection-error.txt", PlainTextLanguage.INSTANCE, stackTrace);
                new OpenFileDescriptor(myProject, file.getVirtualFile()).navigate(true);
            }
        });
    }

    /**
     * Ensures that the JSON response falls within the GraphQL specification character range such that it can be expressed as valid GraphQL SDL in the editor
     *
     * @param introspectionJson the JSON to sanitize
     * @return a sanitized version where the character ranges are within those allowed by the GraphQL Language Specification
     */
    private static String sanitizeIntrospectionJson(@NotNull String introspectionJson) {
        // Strip out emojis (e.g. the one in the GitHub schema) since they're outside the allowed range
        return introspectionJson.replaceAll("[\ud83c\udf00-\ud83d\ude4f]|[\ud83d\ude80-\ud83d\udeff]", "");
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static Map<String, Object> parseIntrospectionJson(@NotNull String introspectionJson) {
        return new Gson().fromJson(sanitizeIntrospectionJson(introspectionJson), Map.class);
    }

    @NotNull
    public String printIntrospectionAsGraphQL(@NotNull String introspectionJson) {
        return printIntrospectionAsGraphQL(parseIntrospectionJson(introspectionJson));
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public String printIntrospectionAsGraphQL(@NotNull Map<String, Object> introspection) {
        introspection = getIntrospectionSchemaData(introspection);

        if (!GraphQLSettings.getSettings(myProject).isEnableIntrospectionDefaultValues()) {
            // strip out the defaultValues that are potentially non-spec compliant
            Ref<Consumer<Object>> defaultValueVisitJson = Ref.create();
            defaultValueVisitJson.set((value) -> {
                if (value instanceof Collection) {
                    ((Collection) value).forEach(colValue -> defaultValueVisitJson.get().consume(colValue));
                } else if (value instanceof Map) {
                    ((Map) value).remove("defaultValue");
                    ((Map) value).values().forEach(mapValue -> defaultValueVisitJson.get().consume(mapValue));
                }
            });
            defaultValueVisitJson.get().consume(introspection);
        }

        final Document schemaDefinition = new GraphQLIntrospectionResultToSchema().createSchemaDefinition(introspection);
        final SchemaPrinter.Options options = SchemaPrinter.Options
            .defaultOptions()
            .includeScalarTypes(false)
            .includeSchemaDefinition(true)
            .includeDirectives(directive -> !DEFAULT_DIRECTIVES.contains(directive.getName()));
        final TypeDefinitionRegistry registry = new SchemaParser().buildRegistry(schemaDefinition);
        final StringBuilder sb = new StringBuilder(new SchemaPrinter(options).print(buildIntrospectionSchema(registry)));

        // graphql-java only prints scalars that are used by fields since it visits fields to discover types, so add the scalars here manually
        final Set<String> knownScalars = Sets.newHashSet();
        for (Node definition : schemaDefinition.getChildren()) {
            if (definition instanceof ScalarTypeDefinition) {
                final ScalarTypeDefinition scalarTypeDefinition = (ScalarTypeDefinition) definition;
                String scalarName = scalarTypeDefinition.getName();
                if (knownScalars.add(scalarName)) {
                    sb.append("\n\n");
                    final Description description = scalarTypeDefinition.getDescription();
                    if (description != null) {
                        if (description.isMultiLine()) {
                            sb.append("\"\"\"").append(description.getContent()).append("\"\"\"");
                        } else {
                            sb.append("\"").append(EscapeUtil.escapeJsonString(description.getContent())).append("\"");
                        }
                        sb.append("\n");
                    }
                    sb.append("scalar ").append(scalarName);
                }
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private Map<String, Object> getIntrospectionSchemaData(@NotNull Map<String, Object> introspection) {
        if (introspection.containsKey("__schema")) {
            return introspection;
        }

        // possibly a full query result
        if (introspection.containsKey("errors")) {
            final Object errorsValue = introspection.get("errors");
            if (errorsValue instanceof List && ((List<?>) errorsValue).size() == 0) {
                showEmptyErrorsNotification();
            } else {
                throw new IllegalArgumentException(GraphQLBundle.message("graphql.introspection.errors", new Gson().toJson(introspection.get("errors"))));
            }
        }
        if (!introspection.containsKey("data")) {
            throw new IllegalArgumentException(GraphQLBundle.message("graphql.introspection.missing.data"));
        }
        introspection = (Map<String, Object>) introspection.get("data");
        if (!introspection.containsKey("__schema")) {
            throw new IllegalArgumentException(GraphQLBundle.message("graphql.introspection.missing.schema"));
        }
        return introspection;
    }

    private void showEmptyErrorsNotification() {
        if (!PropertiesComponent.getInstance().isTrueValue(DISABLE_EMPTY_ERRORS_WARNING_KEY)) {
            final Notification emptyErrorNotification = new Notification(
                GraphQLNotificationUtil.NOTIFICATION_GROUP_ID,
                GraphQLBundle.message("graphql.notification.introspection.error.title"),
                GraphQLBundle.message("graphql.notification.introspection.empty.errors"),
                NotificationType.WARNING
            );

            final AnAction dontShowAgainAction = new NotificationAction(EditorBundle.message("notification.dont.show.again.message")) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    PropertiesComponent.getInstance().setValue(DISABLE_EMPTY_ERRORS_WARNING_KEY, "true");
                    notification.hideBalloon();
                }
            };

            emptyErrorNotification.addAction(dontShowAgainAction);
            Notifications.Bus.notify(emptyErrorNotification, myProject);
        }
    }

    private GraphQLSchema buildIntrospectionSchema(TypeDefinitionRegistry registry) {
        final RuntimeWiring runtimeWiring = EchoingWiringFactory.newEchoingWiring(wiring -> {
            Map<String, ScalarTypeDefinition> scalars = registry.scalars();
            scalars.forEach((name, v) -> {
                if (!ScalarInfo.isGraphqlSpecifiedScalar(name)) {
                    wiring.scalar(createCustomIntrospectionScalar(name));
                }
            });
        });

        return new SchemaGenerator().makeExecutableSchema(registry, runtimeWiring);
    }

    private GraphQLScalarType createCustomIntrospectionScalar(String name) {
        return new GraphQLScalarType(name, name, new Coercing() {
            @Override
            public Object serialize(Object dataFetcherResult) {
                return dataFetcherResult;
            }

            @Override
            public Object parseValue(Object input) {
                return input;
            }

            @Override
            public Object parseLiteral(Object input) {
                if (input instanceof ScalarValue) {
                    if (input instanceof IntValue) {
                        return ((IntValue) input).getValue();
                    } else if (input instanceof FloatValue) {
                        return ((FloatValue) input).getValue();
                    } else if (input instanceof StringValue) {
                        return ((StringValue) input).getValue();
                    } else if (input instanceof BooleanValue) {
                        return ((BooleanValue) input).isValue();
                    }
                }
                return input;
            }
        });
    }

    public GraphQLIntrospectionTask getLatestIntrospection() {
        return latestIntrospection;
    }

    enum IntrospectionOutputFormat {
        JSON,
        SDL
    }

    void createOrUpdateIntrospectionOutputFile(@NotNull String schemaText,
                                               @NotNull IntrospectionOutputFormat format,
                                               @NotNull VirtualFile introspectionSourceFile,
                                               @NotNull String outputFileName) {
        try {
            final String header;
            switch (format) {
                case SDL:
                    header = "# This file was generated based on \"" + introspectionSourceFile.getName() + "\". Do not edit manually.\n\n";
                    break;
                case JSON:
                    header = "";
                    break;
                default:
                    throw new IllegalArgumentException("unsupported output format: " + format);
            }

            VirtualFile outputFile = createSchemaFile(introspectionSourceFile, FileUtil.toSystemIndependentName(outputFileName));

            final FileEditor[] fileEditors = FileEditorManager.getInstance(myProject).openFile(outputFile, true, true);
            if (fileEditors.length == 0) {
                showUnableToOpenEditorNotification(outputFile);
                return;
            }

            TextEditor textEditor = ObjectUtils.tryCast(fileEditors[0], TextEditor.class);
            if (textEditor == null) {
                showUnableToOpenEditorNotification(outputFile);
                return;
            }

            WriteCommandAction.runWriteCommandAction(myProject, () -> {
                com.intellij.openapi.editor.Document document = textEditor.getEditor().getDocument();
                document.setText(StringUtil.convertLineSeparators(header + schemaText));
                PsiDocumentManager.getInstance(myProject).commitDocument(document);

                PsiFile psiFile = PsiDocumentManager.getInstance(myProject).getPsiFile(document);
                if (psiFile != null) {
                    CodeStyleManager.getInstance(myProject).reformat(psiFile);
                }
            });
        } catch (IOException ioe) {
            Notifications.Bus.notify(new Notification(
                GraphQLNotificationUtil.NOTIFICATION_GROUP_ID,
                GraphQLBundle.message("graphql.notification.error.title"),
                GraphQLBundle.message("graphql.notification.unable.to.create.file",
                    outputFileName, introspectionSourceFile.getParent().getPath(), GraphQLNotificationUtil.formatExceptionMessage(ioe)),
                NotificationType.ERROR
            ));
        }
    }

    private void showUnableToOpenEditorNotification(@NotNull VirtualFile outputFile) {
        Notifications.Bus.notify(new Notification(GraphQLNotificationUtil.NOTIFICATION_GROUP_ID, GraphQLBundle.message("graphql.notification.error.title"),
            GraphQLBundle.message("graphql.notification.unable.to.open.editor", outputFile.getPath()), NotificationType.ERROR));
    }

    @NotNull
    private VirtualFile createSchemaFile(@NotNull VirtualFile introspectionSourceFile,
                                         @NotNull String relativeOutputFileName) throws IOException {
        VirtualFile outputFile = introspectionSourceFile.getParent().findFileByRelativePath(relativeOutputFileName);
        if (outputFile == null) {
            outputFile = WriteAction.compute(() -> {
                PsiDirectory directory = PsiDirectoryFactory.getInstance(myProject).createDirectory(introspectionSourceFile.getParent());
                CreateFileAction.MkDirs dirs = new CreateFileAction.MkDirs(relativeOutputFileName, directory);
                return dirs.directory.getVirtualFile().createChildData(introspectionSourceFile, dirs.newName);
            });
        }
        outputFile.putUserData(GraphQLSchemaKeys.IS_GRAPHQL_INTROSPECTION_JSON, true);
        return outputFile;
    }

    @Override
    public void dispose() {
    }

    private class IntrospectionQueryTask extends Task.Backgroundable {
        private final PostMethod method;
        private final String schemaPath;
        private final VirtualFile introspectionSourceFile;
        private final NotificationAction retry;
        private final GraphQLSettings graphQLSettings;
        private final GraphQLConfigVariableAwareEndpoint endpoint;
        private final String url;

        public IntrospectionQueryTask(@NotNull PostMethod method,
                                      @NotNull String schemaPath,
                                      @NotNull VirtualFile introspectionSourceFile,
                                      @NotNull NotificationAction retry,
                                      @NotNull GraphQLSettings graphQLSettings,
                                      @NotNull GraphQLConfigVariableAwareEndpoint endpoint,
                                      @NotNull String url) {
            super(GraphQLIntrospectionService.this.myProject, GraphQLBundle.message("graphql.progress.executing.introspection.query"), false);
            this.method = method;
            this.schemaPath = schemaPath;
            this.introspectionSourceFile = introspectionSourceFile;
            this.retry = retry;
            this.graphQLSettings = graphQLSettings;
            this.endpoint = endpoint;
            this.url = url;
        }

        @Override
        public void run(@NotNull ProgressIndicator indicator) {
            indicator.setIndeterminate(true);
            String responseJson;

            try {
                HttpClient httpClient = new HttpClient(new HttpClientParams());
                httpClient.executeMethod(method);
                responseJson = ObjectUtils.coalesce(method.getResponseBodyAsString(), "");
            } catch (IOException e) {
                GraphQLNotificationUtil.showGraphQLRequestErrorNotification(retry, url, e, NotificationType.WARNING, myProject);
                return;
            }

            Map<String, Object> introspection;
            try {
                introspection = parseIntrospectionJson(responseJson);
                if (getErrorCount(introspection) > 0) {
                    JSGraphQLLanguageUIProjectService.getService(myProject).showQueryResult(responseJson);
                }
            } catch (JsonSyntaxException exception) {
                handleIntrospectionError(exception, GraphQLBundle.message("graphql.notification.introspection.parse.error"), responseJson);
                return;
            }

            IntrospectionOutputFormat format = schemaPath.endsWith(".json") ? IntrospectionOutputFormat.JSON : IntrospectionOutputFormat.SDL;
            String schemaText;
            try {
                // always try to print the schema to validate it since that will be done in schema discovery of the JSON anyway
                final String schemaAsSDL = printIntrospectionAsGraphQL(introspection);
                schemaText = format == IntrospectionOutputFormat.SDL ? schemaAsSDL : responseJson;
            } catch (Exception exception) {
                handleIntrospectionError(exception, null, responseJson);
                return;
            }

            ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    createOrUpdateIntrospectionOutputFile(schemaText, format, introspectionSourceFile, schemaPath);
                } catch (Exception e) {
                    handleIntrospectionError(e, null, responseJson);
                }
            });
        }

        private int getErrorCount(@NotNull Map<String, Object> introspection) {
            Object errors = introspection.get("errors");
            return errors instanceof Collection ? ((Collection<?>) errors).size() : 0;
        }

        private void handleIntrospectionError(@NotNull Exception e,
                                              @Nullable String content,
                                              @NotNull String responseJson) {
            String body = content != null
                ? content
                : GraphQLBundle.message("graphql.notification.introspection.error.body", GraphQLNotificationUtil.formatExceptionMessage(e));

            Notification notification = new Notification(
                GraphQLNotificationUtil.NOTIFICATION_GROUP_ID,
                GraphQLBundle.message("graphql.notification.introspection.error.title"),
                body,
                NotificationType.WARNING
            ).addAction(retry).setImportant(true);

            GraphQLNotificationUtil.addRetryFailedSchemaIntrospectionAction(notification, graphQLSettings, e,
                () -> performIntrospectionQueryAndUpdateSchemaPathFile(endpoint, schemaPath, introspectionSourceFile));
            addIntrospectionStackTraceAction(notification, e);

            Notifications.Bus.notify(notification, myProject);

            JSGraphQLLanguageUIProjectService.getService(myProject).showQueryResult(responseJson);
        }
    }
}
