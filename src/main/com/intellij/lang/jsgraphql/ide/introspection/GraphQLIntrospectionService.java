/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.introspection;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigListener;
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider;
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig;
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigEndpoint;
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig;
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLSchemaPointer;
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
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.advanced.AdvancedSettings;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.intellij.lang.jsgraphql.ide.notifications.GraphQLNotificationUtil.GRAPHQL_NOTIFICATION_GROUP_ID;
import static com.intellij.lang.jsgraphql.ide.notifications.GraphQLNotificationUtil.handleIntrospectionError;
import static com.intellij.lang.jsgraphql.ide.notifications.GraphQLNotificationUtil.showInvalidConfigurationNotification;

@Service(Service.Level.PROJECT)
public final class GraphQLIntrospectionService implements Disposable {
  private static final Logger LOG = Logger.getInstance(GraphQLIntrospectionService.class);

  public static final String GRAPHQL_TRUST_ALL_HOSTS = "graphql.trust.all.hosts";

  private volatile GraphQLIntrospectionTask latestIntrospection = null;
  private final AtomicBoolean myIntrospected = new AtomicBoolean();
  private final Project myProject;

  public static GraphQLIntrospectionService getInstance(@NotNull Project project) {
    return project.getService(GraphQLIntrospectionService.class);
  }

  public GraphQLIntrospectionService(@NotNull Project project) {
    myProject = project;

    MessageBusConnection connection = project.getMessageBus().connect(this);
    connection.subscribe(GraphQLConfigListener.TOPIC, () -> {
      latestIntrospection = null;

      if (myIntrospected.compareAndSet(false, true)) {
        introspectEndpoints();
      }
    });
  }

  public @Nullable GraphQLIntrospectionTask getLatestIntrospection() {
    return latestIntrospection;
  }

  public void performIntrospectionQuery(@NotNull GraphQLConfigEndpoint selectedEndpoint) {
    // store the endpoint before substituting environment variables
    GraphQLIntrospectionTask introspectionTask = new GraphQLIntrospectionTask(myProject, selectedEndpoint);
    latestIntrospection = introspectionTask;

    var preparedEndpoint = GraphQLIntrospectionUtil.promptForEnvVariables(myProject, selectedEndpoint);
    if (preparedEndpoint == null || !isEndpointConfigurationValid(preparedEndpoint)) return;

    GraphQLIntrospectionQueryExecutor.getInstance(myProject).runIntrospectionQuery(preparedEndpoint, introspectionTask);
  }

  private boolean isEndpointConfigurationValid(GraphQLConfigEndpoint endpoint) {
    GraphQLProjectConfig projectConfig = endpoint.getConfig();
    VirtualFile configFile = projectConfig != null ? projectConfig.getFile() : null;
    if (projectConfig == null || configFile == null) {
      showInvalidConfigurationNotification(
        GraphQLBundle.message("graphql.notification.introspection.endpoint.config.not.found"),
        endpoint.getFile(),
        myProject
      );
      return false;
    }

    if (StringUtil.isEmptyOrSpaces(endpoint.getUrl())) {
      showInvalidConfigurationNotification(
        GraphQLBundle.message("graphql.notification.introspection.empty.endpoint.url"),
        endpoint.getFile(),
        myProject
      );
      return false;
    }

    GraphQLSchemaPointer pointer = endpoint.getSchemaPointer();
    String schemaPath = pointer != null ? pointer.getOutputPath() : null;

    if (StringUtil.isEmptyOrSpaces(schemaPath)) {
      if (pointer == null || !pointer.isRemote()) {
        showInvalidConfigurationNotification(
          GraphQLBundle.message(
            "graphql.notification.introspection.empty.schema.path",
            pointer != null ? GraphQLBundle.message(
              "graphql.notification.introspection.empty.schema.path.provided",
              pointer.getPattern()
            ) : ""
          ),
          endpoint.getFile(),
          myProject
        );
      }
      else {
        showInvalidConfigurationNotification(
          GraphQLBundle.message("graphql.notification.introspection.unable.to.build.path"),
          endpoint.getFile(),
          myProject
        );
      }
      return false;
    }

    return true;
  }

  public @Nullable NotificationAction createTrustAllHostsAction() {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance(myProject);
    if (propertiesComponent.isTrueValue(GRAPHQL_TRUST_ALL_HOSTS)) return null;

    return NotificationAction.createSimpleExpiring(
      GraphQLBundle.message("graphql.notification.trust.all.hosts"),
      () -> propertiesComponent.setValue(GRAPHQL_TRUST_ALL_HOSTS, true));
  }

  public static @NotNull String printIntrospectionAsGraphQL(@NotNull Project project, @NotNull String introspectionJson) {
    return printIntrospectionAsGraphQL(project, GraphQLQueryClient.parseResponseJsonAsMap(introspectionJson));
  }

  public static @NotNull String printIntrospectionAsGraphQL(@NotNull Project project, @NotNull Map<String, Object> introspection) {
    introspection = getIntrospectionSchemaDataFromParsedResponse(introspection);

    if (AdvancedSettings.getBoolean("graphql.introspection.skip.default.values")) {
      // strip out the defaultValues that are potentially non-spec compliant
      Ref<Consumer<Object>> defaultValueVisitJson = Ref.create();
      defaultValueVisitJson.set((value) -> {
        if (value instanceof Collection) {
          ((Collection<?>)value).forEach(colValue -> defaultValueVisitJson.get().consume(colValue));
        }
        else if (value instanceof Map) {
          ((Map<?, ?>)value).remove("defaultValue");
          ((Map<?, ?>)value).values().forEach(mapValue -> defaultValueVisitJson.get().consume(mapValue));
        }
      });
      defaultValueVisitJson.get().consume(introspection);
    }

    Document schemaDefinition = new GraphQLIntrospectionResultToSchema(project).createSchemaDefinition(introspection);
    SchemaPrinter.Options options = SchemaPrinter.Options
      .defaultOptions()
      .includeScalarTypes(true)
      .includeSchemaDefinition(true)
      .includeEmptyTypes(AdvancedSettings.getBoolean("graphql.introspection.include.empty.types"))
      .includeDirectives(directive -> !GraphQLKnownTypes.DEFAULT_DIRECTIVES.contains(directive.getName()));

    GraphQLRegistryInfo registryInfo = new GraphQLRegistryInfo(new SchemaParser().buildRegistry(schemaDefinition), false);
    GraphQLSchemaInfo schemaInfo = new GraphQLSchemaInfo(
      UnExecutableSchemaGenerator.makeUnExecutableSchema(registryInfo.getTypeDefinitionRegistry()),
      Collections.emptyList(),
      registryInfo
    );

    List<GraphQLError> errors = schemaInfo.getErrors(project);
    if (!errors.isEmpty()) {
      for (GraphQLError error : errors) {
        LOG.warn(error.getMessage());
      }
    }

    try {
      return new SchemaPrinter(project, options).print(schemaInfo.getSchema());
    }
    catch (ProcessCanceledException e) {
      throw e;
    }
    catch (Exception e) {
      if (!errors.isEmpty()) {
        throw new SchemaProblem(errors);
      }
      else {
        throw e;
      }
    }
  }

  enum IntrospectionOutputFormat {
    JSON,
    SDL
  }

  record IntrospectionOutput(@NotNull String schemaText, @NotNull IntrospectionOutputFormat format) {
  }

  private static @Nullable Map<String, Object> parseIntrospectionResponse(@NotNull Project project,
                                                                          @NotNull GraphQLConfigEndpoint endpoint,
                                                                          @NotNull String introspectionResponse) {
    try {
      Map<String, Object> introspection = GraphQLQueryClient.parseResponseJsonAsMap(introspectionResponse);
      if (GraphQLIntrospectionUtil.getErrorCountFromResponse(introspection) > 0) {
        GraphQLUIProjectService.getInstance(project).showQueryResult(introspectionResponse);
      }
      return introspection;
    }
    catch (JsonParseException exception) {
      handleIntrospectionError(project, endpoint, exception,
                               GraphQLBundle.message("graphql.notification.introspection.parse.error"),
                               introspectionResponse);
      return null;
    }
  }

  static @Nullable IntrospectionOutput parseIntrospectionOutput(@NotNull Project project,
                                                                @NotNull GraphQLConfigEndpoint endpoint,
                                                                @NotNull String schemaPath,
                                                                @NotNull String rawIntrospectionResponse) {
    var parsedIntrospection = parseIntrospectionResponse(project, endpoint, rawIntrospectionResponse);
    if (parsedIntrospection == null) return null;

    IntrospectionOutputFormat format = schemaPath.endsWith(".json") ? IntrospectionOutputFormat.JSON : IntrospectionOutputFormat.SDL;
    try {
      // always try to print the schema to validate it since that will be done in schema discovery of the JSON anyway
      String schemaAsSDL = printIntrospectionAsGraphQL(project, parsedIntrospection);
      String schemaText = format == IntrospectionOutputFormat.SDL ? schemaAsSDL : rawIntrospectionResponse;
      return new IntrospectionOutput(schemaText, format);
    }
    catch (CancellationException exception) {
      throw exception;
    }
    catch (Exception exception) {
      handleIntrospectionError(project, endpoint, exception, null, rawIntrospectionResponse);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private static @NotNull Map<String, Object> getIntrospectionSchemaDataFromParsedResponse(@NotNull Map<String, Object> introspection) {
    if (introspection.containsKey("__schema")) {
      return introspection;
    }

    // possibly a full query result
    if (introspection.containsKey("errors")) {
      Object errorsValue = introspection.get("errors");
      if (errorsValue instanceof Collection<?> && !((Collection<?>)errorsValue).isEmpty()) {
        throw new IllegalArgumentException(
          GraphQLBundle.message("graphql.introspection.errors", new Gson().toJson(errorsValue)));
      }
    }
    if (!introspection.containsKey("data")) {
      throw new IllegalArgumentException(GraphQLBundle.message("graphql.introspection.missing.data"));
    }
    introspection = (Map<String, Object>)introspection.get("data");
    if (!introspection.containsKey("__schema")) {
      throw new IllegalArgumentException(GraphQLBundle.message("graphql.introspection.missing.schema"));
    }
    return introspection;
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
            if (!Boolean.TRUE.equals(endpoint.getIntrospect()) || schemaPath.isBlank()) {
              continue;
            }
            String url = endpoint.getUrl();
            if (!visitedUrls.add(url)) {
              continue;
            }

            Notification introspect = new Notification(
              GRAPHQL_NOTIFICATION_GROUP_ID,
              GraphQLBundle.message("graphql.notification.load.schema.from.endpoint.title"),
              GraphQLBundle.message("graphql.notification.load.schema.from.endpoint.body", endpoint.getDisplayName()),
              NotificationType.INFORMATION
            ).setImportant(true);


            introspect.addAction(new NotificationAction(
              GraphQLBundle.message("graphql.notification.load.schema.from.endpoint.action", url)) {
              @Override
              public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                performIntrospectionQuery(endpoint);
              }
            });

            VirtualFile schemaFile = ReadAction.compute(() -> LocalFileSystem.getInstance().findFileByPath(schemaPath));
            if (schemaFile != null) {
              introspect.addAction(new NotificationAction(GraphQLBundle.message("graphql.notification.content.open.schema.file")) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                  if (schemaFile.isValid()) {
                    FileEditorManager.getInstance(myProject).openFile(schemaFile, true);
                  }
                  else {
                    notification.expire();
                  }
                }
              });
            }
            Notifications.Bus.notify(introspect);
          }
        }
      });
    }, ModalityState.nonModal());
  }

  @Override
  public void dispose() {
  }
}
