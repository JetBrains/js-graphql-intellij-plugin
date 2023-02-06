/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.introspection;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.ide.actions.CreateFileAction;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.lang.jsgraphql.GraphQLSettings;
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigListener;
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider;
import com.intellij.lang.jsgraphql.ide.config.model.*;
import com.intellij.lang.jsgraphql.ide.notifications.GraphQLNotificationUtil;
import com.intellij.lang.jsgraphql.schema.GraphQLKnownTypes;
import com.intellij.lang.jsgraphql.schema.GraphQLRegistryInfo;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.language.Document;
import com.intellij.lang.jsgraphql.types.schema.idl.SchemaParser;
import com.intellij.lang.jsgraphql.types.schema.idl.SchemaPrinter;
import com.intellij.lang.jsgraphql.types.schema.idl.UnExecutableSchemaGenerator;
import com.intellij.lang.jsgraphql.types.schema.idl.errors.SchemaProblem;
import com.intellij.lang.jsgraphql.ui.GraphQLUIProjectService;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
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
import com.intellij.util.concurrency.annotations.RequiresWriteLock;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.net.IdeHttpClientHelpers;
import com.intellij.util.net.ssl.CertificateManager;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HostnameVerifier;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.intellij.lang.jsgraphql.ide.notifications.GraphQLNotificationUtil.GRAPHQL_NOTIFICATION_GROUP_ID;
import static com.intellij.lang.jsgraphql.ui.GraphQLUIProjectService.setHeadersFromOptions;

@Service
public final class GraphQLIntrospectionService implements Disposable {
    private static final Logger LOG = Logger.getInstance(GraphQLIntrospectionService.class);

    private static final String DISABLE_EMPTY_ERRORS_WARNING_KEY = "graphql.empty.errors.warning.disabled";
    public static final String GRAPHQL_TRUST_ALL_HOSTS = "graphql.trust.all.hosts";

    private GraphQLIntrospectionTask latestIntrospection = null;
    private final AtomicBoolean myIntrospected = new AtomicBoolean();
    private final Project myProject;

    public static GraphQLIntrospectionService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLIntrospectionService.class);
    }

    public GraphQLIntrospectionService(Project project) {
        myProject = project;

        MessageBusConnection connection = project.getMessageBus().connect(this);
        connection.subscribe(GraphQLConfigListener.TOPIC, (GraphQLConfigListener) () -> {
            latestIntrospection = null;

            if (myIntrospected.compareAndSet(false, true)) {
                introspectEndpoints();
            }
        });
    }

    public void performIntrospectionQueryAndUpdateSchemaPathFile(@NotNull GraphQLConfigEndpoint endpoint) {
        GraphQLProjectConfig projectConfig = endpoint.findConfig();
        VirtualFile configFile = projectConfig != null ? projectConfig.getFile() : null;
        if (projectConfig == null || configFile == null) {
            GraphQLNotificationUtil.showInvalidConfigurationNotification(
                GraphQLBundle.message("graphql.notification.endpoint.config.not.found"),
                endpoint.getFile(),
                myProject
            );
            return;
        }

        String schemaPath = projectConfig.getSchema().stream()
            .map((pointer) -> pointer.withUIContext(endpoint.isUIContext()).getFilePath())
            .filter(Objects::nonNull)
            .findFirst().orElse(null);

        if (StringUtil.isEmptyOrSpaces(schemaPath)) {
            GraphQLNotificationUtil.showInvalidConfigurationNotification(
                GraphQLBundle.message("graphql.notification.empty.schema.path"),
                endpoint.getFile(),
                myProject
            );
            return;
        }
        performIntrospectionQueryAndUpdateSchemaPathFile(endpoint, schemaPath, configFile);
    }

    public void performIntrospectionQueryAndUpdateSchemaPathFile(@NotNull GraphQLConfigEndpoint endpoint,
                                                                 @NotNull String schemaPath,
                                                                 @NotNull VirtualFile introspectionSourceFile) {
        latestIntrospection = new GraphQLIntrospectionTask(endpoint,
            () -> performIntrospectionQueryAndUpdateSchemaPathFile(endpoint, schemaPath, introspectionSourceFile));

        final NotificationAction retry = new NotificationAction(GraphQLBundle.message("graphql.notification.retry")) {

            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                notification.expire();
                performIntrospectionQueryAndUpdateSchemaPathFile(endpoint, schemaPath, introspectionSourceFile);
            }
        };

        String url = endpoint.getUrl();
        if (StringUtil.isEmptyOrSpaces(url)) {
            GraphQLNotificationUtil.showInvalidConfigurationNotification(
                GraphQLBundle.message("graphql.notification.empty.endpoint.url"),
                introspectionSourceFile,
                myProject
            );
            return;
        }

        try {
            final GraphQLSettings settings = GraphQLSettings.getSettings(myProject);
            String query = buildIntrospectionQuery(settings);

            final String requestJson = "{\"query\":\"" + StringEscapeUtils.escapeJavaScript(query) + "\"}";
            HttpPost request = createRequest(endpoint, url, requestJson);
            Task.Backgroundable task = new IntrospectionQueryTask(
                request, schemaPath, introspectionSourceFile, retry, settings, endpoint, url);
            ProgressManager.getInstance().run(task);
        } catch (IllegalStateException | IllegalArgumentException e) {
            LOG.warn(e);
            GraphQLNotificationUtil.showGraphQLRequestErrorNotification(myProject, url, e, NotificationType.ERROR, retry);
        }
    }

    @NotNull
    private String buildIntrospectionQuery(@NotNull GraphQLSettings settings) {
        String query = settings.getIntrospectionQuery();
        if (!StringUtil.isEmptyOrSpaces(query)) {
            return query;
        }

        query = GraphQLIntrospectionQuery.INTROSPECTION_QUERY;
        if (!settings.isEnableIntrospectionRepeatableDirectives()) {
            query = query.replace("isRepeatable", "");
        }
        if (!settings.isEnableIntrospectionDefaultValues()) {
            query = query.replace("defaultValue", "");
        }
        return query;
    }

    @NotNull
    public static HttpPost createRequest(@NotNull GraphQLConfigEndpoint endpoint,
                                         @NotNull String url,
                                         @NotNull String requestJson) {
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(requestJson, ContentType.APPLICATION_JSON));
        setHeadersFromOptions(endpoint, request);
        return request;
    }

    public @NotNull CloseableHttpClient createHttpClient(@NotNull String url, @Nullable GraphQLConfigSecurity sslConfig)
        throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, IOException, UnrecoverableKeyException,
        CertificateException {
        HttpClientBuilder builder = HttpClients.custom()
            .setDefaultRequestConfig(createRequestConfig(url))
            .setSSLContext(CertificateManager.getInstance().getSslContext())
            .setDefaultCredentialsProvider(createCredentialsProvider(url))
            .setRedirectStrategy(LaxRedirectStrategy.INSTANCE)
            .setSSLHostnameVerifier(createHostnameVerifier());
        GraphQLIntrospectionSSLBuilder.loadCustomSSLConfiguration(sslConfig, builder);
        return builder.build();
    }

    private @NotNull RequestConfig createRequestConfig(@NotNull String url) {
        RequestConfig.Builder builder = RequestConfig.custom()
            .setConnectTimeout(Registry.intValue("graphql.request.connect.timeout", 5000))
            .setSocketTimeout(Registry.intValue("graphql.request.timeout", 15000));
        IdeHttpClientHelpers.ApacheHttpClient4.setProxyForUrlIfEnabled(builder, url);
        return builder.build();
    }

    private @NotNull CredentialsProvider createCredentialsProvider(@NotNull String url) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        IdeHttpClientHelpers.ApacheHttpClient4.setProxyCredentialsForUrlIfEnabled(provider, url);
        return provider;
    }

    private @NotNull HostnameVerifier createHostnameVerifier() {
        return PropertiesComponent.getInstance(myProject).isTrueValue(GRAPHQL_TRUST_ALL_HOSTS)
            ? NoopHostnameVerifier.INSTANCE
            : new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault());
    }

    @Nullable
    public NotificationAction createTrustAllHostsAction() {
        final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(myProject);
        if (propertiesComponent.isTrueValue(GRAPHQL_TRUST_ALL_HOSTS)) return null;

        return NotificationAction.createSimpleExpiring(
            GraphQLBundle.message("graphql.notification.trust.all.hosts"),
            () -> propertiesComponent.setValue(GRAPHQL_TRUST_ALL_HOSTS, true));
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
        var result = new Gson().fromJson(sanitizeIntrospectionJson(introspectionJson), Map.class);
        if (result == null) {
            throw new JsonSyntaxException("Invalid introspection JSON value");
        }
        return result;
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

        final Document schemaDefinition = new GraphQLIntrospectionResultToSchema(myProject)
            .createSchemaDefinition(introspection);
        final SchemaPrinter.Options options = SchemaPrinter.Options
            .defaultOptions()
            .includeScalarTypes(true)
            .includeSchemaDefinition(true)
            .includeDirectives(directive -> !GraphQLKnownTypes.DEFAULT_DIRECTIVES.contains(directive.getName()));

        GraphQLRegistryInfo registryInfo = new GraphQLRegistryInfo(
            new SchemaParser().buildRegistry(schemaDefinition), Collections.emptyList(), true);
        GraphQLSchemaInfo schemaInfo = new GraphQLSchemaInfo(
            UnExecutableSchemaGenerator.makeUnExecutableSchema(registryInfo.getTypeDefinitionRegistry()),
            Collections.emptyList(),
            registryInfo
        );

        List<GraphQLError> errors = schemaInfo.getErrors(myProject);
        if (!errors.isEmpty()) {
            for (GraphQLError error : errors) {
                LOG.warn(error.getMessage());
            }
        }

        try {
            return new SchemaPrinter(myProject, options).print(schemaInfo.getSchema());
        } catch (ProcessCanceledException e) {
            throw e;
        } catch (Exception e) {
            if (!errors.isEmpty()) {
                throw new SchemaProblem(errors);
            } else {
                throw e;
            }
        }
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
                throw new IllegalArgumentException(
                    GraphQLBundle.message("graphql.introspection.errors", new Gson().toJson(introspection.get("errors"))));
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
                GRAPHQL_NOTIFICATION_GROUP_ID,
                GraphQLBundle.message("graphql.notification.introspection.error.title"),
                GraphQLBundle.message("graphql.notification.introspection.empty.errors"),
                NotificationType.WARNING
            );

            final AnAction dontShowAgainAction = new NotificationAction(
                GraphQLBundle.message("graphql.notification.dont.show.again.message")) {
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

    public GraphQLIntrospectionTask getLatestIntrospection() {
        return latestIntrospection;
    }

    enum IntrospectionOutputFormat {
        JSON,
        SDL
    }

    void createOrUpdateIntrospectionOutputFile(@NotNull String schemaText,
                                               @NotNull IntrospectionOutputFormat format,
                                               @NotNull VirtualFile configFile,
                                               @NotNull String outputFileName) {
        final String header;
        switch (format) {
            case SDL:
                header = "# This file was generated based on \"" + configFile.getName() + "\". Do not edit manually.\n\n";
                break;
            case JSON:
                header = "";
                break;
            default:
                throw new IllegalArgumentException("unsupported output format: " + format);
        }

        WriteCommandAction.runWriteCommandAction(myProject, () -> {
            try {
                VirtualFile outputFile =
                    createOrUpdateSchemaFile(configFile, FileUtil.toSystemIndependentName(outputFileName));
                com.intellij.openapi.editor.Document document = FileDocumentManager.getInstance().getDocument(outputFile);
                if (document == null) {
                    throw new IllegalStateException("Document not found");
                }
                document.setText(StringUtil.convertLineSeparators(header + schemaText));
                PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(myProject);
                psiDocumentManager.commitDocument(document);
                PsiFile psiFile = psiDocumentManager.getPsiFile(document);
                if (psiFile != null) {
                    CodeStyleManager.getInstance(myProject).reformat(psiFile);
                }
                openSchemaInEditor(outputFile);
            } catch (ProcessCanceledException e) {
                throw e;
            } catch (IOException e) {
                LOG.info(e);
                Notifications.Bus.notify(new Notification(
                    GRAPHQL_NOTIFICATION_GROUP_ID,
                    GraphQLBundle.message("graphql.notification.error.title"),
                    GraphQLBundle.message("graphql.notification.unable.to.create.file",
                        outputFileName, configFile.getParent().getPath(), GraphQLNotificationUtil.formatExceptionMessage(e)),
                    NotificationType.ERROR
                ));
            } catch (Exception e) {
                LOG.error(e);
            }
        });
    }

    private void openSchemaInEditor(@NotNull VirtualFile file) {
        if (!GraphQLSettings.getSettings(myProject).isOpenEditorWithIntrospectionResult()) {
            return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            final FileEditor[] fileEditors = FileEditorManager.getInstance(myProject).openFile(file, true, true);
            if (fileEditors.length == 0) {
                showUnableToOpenEditorNotification(file);
                return;
            }

            TextEditor textEditor = ObjectUtils.tryCast(fileEditors[0], TextEditor.class);
            if (textEditor == null) {
                showUnableToOpenEditorNotification(file);
            }
        });
    }

    private void showUnableToOpenEditorNotification(@NotNull VirtualFile outputFile) {
        Notifications.Bus.notify(
            new Notification(
                GRAPHQL_NOTIFICATION_GROUP_ID,
                GraphQLBundle.message("graphql.notification.error.title"),
                GraphQLBundle.message("graphql.notification.unable.to.open.editor", outputFile.getPath()),
                NotificationType.ERROR)
        );
    }

    @RequiresWriteLock
    @NotNull
    private VirtualFile createOrUpdateSchemaFile(@NotNull VirtualFile configFile,
                                                 @NotNull String relativeOutputFileName) throws IOException {
        VirtualFile outputFile = configFile.getParent().findFileByRelativePath(relativeOutputFileName);
        if (outputFile == null) {
            PsiDirectory directory = PsiDirectoryFactory.getInstance(myProject).createDirectory(configFile.getParent());
            CreateFileAction.MkDirs dirs = new CreateFileAction.MkDirs(relativeOutputFileName, directory);
            outputFile = dirs.directory.getVirtualFile().createChildData(configFile, dirs.newName);
        }
        return outputFile;
    }

    private void introspectEndpoints() {
        DumbService.getInstance(myProject).smartInvokeLater(() -> {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                Set<String> visitedUrls = new HashSet<>();

                var configList = GraphQLConfigProvider.getInstance(myProject).getAllConfigs().stream()
                    .map(GraphQLConfig::getDefault)
                    .filter(Objects::nonNull)
                    .toList();

                for (var config : configList) {
                    GraphQLSchemaPointer schemaPointer = ContainerUtil.getFirstItem(config.getSchema());
                    if (schemaPointer == null) {
                        continue;
                    }

                    var schemaPath = schemaPointer.getFilePath();
                    if (schemaPath == null) {
                        continue;
                    }

                    List<GraphQLConfigEndpoint> endpoints = config.getEndpoints();
                    for (GraphQLConfigEndpoint endpoint : endpoints) {
                        if (!endpoint.getIntrospect() || schemaPath.isBlank()) {
                            continue;
                        }
                        String url = endpoint.getUrl();
                        if (!visitedUrls.add(url)) {
                            continue;
                        }

                        final Notification introspect = new Notification(
                            GRAPHQL_NOTIFICATION_GROUP_ID,
                            GraphQLBundle.message("graphql.notification.load.schema.from.endpoint.title"),
                            GraphQLBundle.message("graphql.notification.load.schema.from.endpoint.body", endpoint.getName()),
                            NotificationType.INFORMATION
                        ).setImportant(true);


                        introspect.addAction(new NotificationAction(
                            GraphQLBundle.message("graphql.notification.load.schema.from.endpoint.action", url)) {
                            @Override
                            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                                GraphQLIntrospectionService.getInstance(myProject)
                                    .performIntrospectionQueryAndUpdateSchemaPathFile(endpoint);
                            }
                        });

                        final VirtualFile schemaFile = ReadAction.compute(() -> LocalFileSystem.getInstance().findFileByPath(schemaPath));
                        if (schemaFile != null) {
                            introspect.addAction(new NotificationAction("Open schema file") {
                                @Override
                                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                                    if (schemaFile.isValid()) {
                                        FileEditorManager.getInstance(myProject).openFile(schemaFile, true);
                                    } else {
                                        notification.expire();
                                    }
                                }
                            });
                        }
                        Notifications.Bus.notify(introspect);
                    }
                }
            });
        }, ModalityState.NON_MODAL);
    }

    @Override
    public void dispose() {
    }

    private class IntrospectionQueryTask extends Task.Backgroundable {
        private final HttpUriRequest request;
        private final String schemaPath;
        private final VirtualFile introspectionSourceFile;
        private final NotificationAction retry;
        private final GraphQLSettings graphQLSettings;
        private final GraphQLConfigEndpoint endpoint;
        private final String url;

        public IntrospectionQueryTask(@NotNull HttpUriRequest request,
                                      @NotNull String schemaPath,
                                      @NotNull VirtualFile introspectionSourceFile,
                                      @NotNull NotificationAction retry,
                                      @NotNull GraphQLSettings graphQLSettings,
                                      @NotNull GraphQLConfigEndpoint endpoint,
                                      @NotNull String url) {
            super(
                GraphQLIntrospectionService.this.myProject,
                GraphQLBundle.message("graphql.progress.executing.introspection.query"),
                false
            );
            this.request = request;
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
            GraphQLConfig config = GraphQLConfigProvider.getInstance(myProject).getConfig(introspectionSourceFile);
            GraphQLConfigSecurity sslConfig = config != null ? GraphQLConfigSecurity.getSecurityConfig(config.getDefault()) : null;
            try (final CloseableHttpClient httpClient = createHttpClient(url, sslConfig);
                 final CloseableHttpResponse response = httpClient.execute(request)) {
                responseJson = ObjectUtils.coalesce(EntityUtils.toString(response.getEntity()), "");
            } catch (IOException | GeneralSecurityException e) {
                LOG.warn(e);
                GraphQLNotificationUtil.showGraphQLRequestErrorNotification(myProject, url, e, NotificationType.WARNING, retry);
                return;
            }

            Map<String, Object> introspection;
            try {
                introspection = parseIntrospectionJson(responseJson);
                if (getErrorCount(introspection) > 0) {
                    GraphQLUIProjectService.getService(myProject).showQueryResult(responseJson);
                }
            } catch (JsonSyntaxException exception) {
                handleIntrospectionError(exception, GraphQLBundle.message("graphql.notification.introspection.parse.error"), responseJson);
                return;
            }

            IntrospectionOutputFormat format = schemaPath.endsWith(".json")
                ? IntrospectionOutputFormat.JSON : IntrospectionOutputFormat.SDL;
            String schemaText;
            try {
                // always try to print the schema to validate it since that will be done in schema discovery of the JSON anyway
                final String schemaAsSDL = printIntrospectionAsGraphQL(introspection);
                schemaText = format == IntrospectionOutputFormat.SDL ? schemaAsSDL : responseJson;
            } catch (ProcessCanceledException exception) {
                throw exception;
            } catch (Exception exception) {
                handleIntrospectionError(exception, null, responseJson);
                return;
            }

            ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    createOrUpdateIntrospectionOutputFile(schemaText, format, introspectionSourceFile, schemaPath);
                } catch (ProcessCanceledException exception) {
                    throw exception;
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
                GRAPHQL_NOTIFICATION_GROUP_ID,
                GraphQLBundle.message("graphql.notification.introspection.error.title"),
                body,
                NotificationType.WARNING
            ).addAction(retry).setImportant(true);

            GraphQLNotificationUtil.addRetryFailedSchemaIntrospectionAction(notification, graphQLSettings, e,
                () -> performIntrospectionQueryAndUpdateSchemaPathFile(endpoint, schemaPath, introspectionSourceFile));
            addIntrospectionStackTraceAction(notification, e);

            Notifications.Bus.notify(notification, myProject);

            if (myProject != null) {
                GraphQLUIProjectService.getService(myProject).showQueryResult(responseJson);
            }
        }
    }
}
