/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ui;

import com.google.gson.*;
import com.intellij.codeInsight.CodeSmellInfo;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.codeInsight.hint.HintUtil;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.json.JsonFileType;
import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.GraphQLParserDefinition;
import com.intellij.lang.jsgraphql.ide.actions.GraphQLExecuteEditorAction;
import com.intellij.lang.jsgraphql.ide.actions.GraphQLOpenConfigAction;
import com.intellij.lang.jsgraphql.ide.actions.GraphQLToggleVariablesAction;
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigListener;
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider;
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLEditEnvironmentVariablesAction;
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigEndpoint;
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigSecurity;
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig;
import com.intellij.lang.jsgraphql.ide.highlighting.query.GraphQLQueryContext;
import com.intellij.lang.jsgraphql.ide.highlighting.query.GraphQLQueryContextHighlightVisitor;
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLIntrospectionService;
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLIntrospectionUtil;
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLOpenIntrospectionSchemaAction;
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLRunIntrospectionQueryAction;
import com.intellij.lang.jsgraphql.ide.introspection.remote.GraphQLRemoteSchemasRegistry;
import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourcesManager;
import com.intellij.lang.jsgraphql.ide.notifications.GraphQLNotificationUtil;
import com.intellij.lang.jsgraphql.ide.project.schemastatus.GraphQLEndpointsModel;
import com.intellij.lang.jsgraphql.ide.project.toolwindow.GraphQLToolWindow;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.impl.EditorHeaderComponent;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.CodeSmellDetector;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.*;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.Alarm;
import com.intellij.util.concurrency.annotations.RequiresEdt;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.JBUI;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.*;

public class GraphQLUIProjectService implements Disposable, FileEditorManagerListener, GraphQLConfigListener {

  private static final Logger LOG = Logger.getInstance(GraphQLUIProjectService.class);

  public static final String GRAPH_QL_VARIABLES_JSON = "graphql.variables.json";

  /**
   * Indicates that this virtual file backs a GraphQL variables editor
   */
  public static final Key<Boolean> IS_GRAPH_QL_VARIABLES_VIRTUAL_FILE = Key.create(GRAPH_QL_VARIABLES_JSON);

  /**
   * Gets the variables editor associated with a .graphql query editor
   */
  public static final Key<Editor> GRAPH_QL_VARIABLES_EDITOR = Key.create(GRAPH_QL_VARIABLES_JSON + ".variables.editor");

  /**
   * Gets the query editor associated with a GraphQL variables editor
   */
  public static final Key<Editor> GRAPH_QL_QUERY_EDITOR = Key.create(GRAPH_QL_VARIABLES_JSON + ".query.editor");

  public static final Key<JPanel> GRAPH_QL_QUERY_COMPONENT = Key.create(GRAPH_QL_VARIABLES_JSON + ".query.component");

  public static final Key<GraphQLEndpointsModel> GRAPH_QL_ENDPOINTS_MODEL = Key.create("graphql.endpoints.model");

  public static final Key<Boolean> GRAPH_QL_EDITOR_QUERYING = Key.create("graphql.editor.querying");

  private static final int UPDATE_MS = 500;
  private static final @NlsSafe String VARIABLES_PLACEHOLDER = "{ variables }";
  private final Alarm myUpdateUIAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);

  private final @NotNull Project myProject;

  public GraphQLUIProjectService(final @NotNull Project project) {
    myProject = project;
  }

  public static GraphQLUIProjectService getInstance(@NotNull Project project) {
    return project.getService(GraphQLUIProjectService.class);
  }

  public void projectOpened() {
    MessageBusConnection connection = myProject.getMessageBus().connect(this);

    // listen for editor file tab changes to update the list of current errors
    connection.subscribe(FILE_EDITOR_MANAGER, this);
    // listen for configuration changes
    connection.subscribe(TOPIC, this);

    ApplicationManager.getApplication().invokeLater(
      () -> {
        // add editor headers to already open files since we've only just added the listener for fileOpened()
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(myProject);
        for (VirtualFile virtualFile : fileEditorManager.getOpenFiles()) {
          insertEditorHeaderComponentIfApplicable(virtualFile);
        }
      },
      ModalityState.nonModal(),
      myProject.getDisposed()
    );

    // and notify to configure the schema
    myProject.putUserData(GraphQLParserDefinition.GRAPHQL_ACTIVATED, true);
    EditorNotifications.getInstance(myProject).updateAllNotifications();
  }

  // ---- editor tabs listener ----

  @Override
  public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
    if (!isApplicableForToolbar(file)) {
      return;
    }

    ApplicationManager.getApplication().invokeLater(() -> {
      insertEditorHeaderComponentIfApplicable(file);
    }, myProject.getDisposed());
  }

  // ---- configuration listener ----

  @Override
  public void onConfigurationChanged() {
    reloadEndpoints();

    ApplicationManager.getApplication().invokeLater(
      () -> EditorNotifications.getInstance(myProject).updateAllNotifications(),
      ModalityState.defaultModalityState(),
      myProject.getDisposed()
    );
  }

  // ---- implementation ----


  // -- endpoints --

  private void reloadEndpoints() {
    if (ApplicationManager.getApplication().isHeadlessEnvironment()) return;

    myUpdateUIAlarm.cancelAllRequests();
    myUpdateUIAlarm.addRequest(() -> {
      if (myProject.isDisposed()) return;
      final FileEditorManager fileEditorManager = FileEditorManager.getInstance(myProject);
      final GraphQLConfigProvider configProvider = GraphQLConfigProvider.getInstance(myProject);
      List<VirtualFile> files = ReadAction.compute(
        () -> ContainerUtil.filter(fileEditorManager.getOpenFiles(), f -> GraphQLFileType.isGraphQLFile(f))
      );
      if (myProject.isDisposed()) return;

      for (VirtualFile file : files) {
        List<GraphQLConfigEndpoint> endpoints = ReadAction.compute(() -> {
          GraphQLProjectConfig config = configProvider.resolveProjectConfig(file);
          return config != null ? config.getEndpoints() : Collections.emptyList();
        });

        ApplicationManager.getApplication().invokeLater(() -> ReadAction.run(() -> {
          for (FileEditor editor : fileEditorManager.getEditors(file)) {
            if (editor instanceof TextEditor) {
              final GraphQLEndpointsModel endpointsModel =
                ((TextEditor)editor).getEditor().getUserData(GRAPH_QL_ENDPOINTS_MODEL);
              if (endpointsModel != null) {
                endpointsModel.reload(endpoints);
                EditorNotifications.getInstance(myProject).updateNotifications(file);
              }
            }
          }
        }), ModalityState.defaultModalityState(), myProject.getDisposed());
      }
    }, UPDATE_MS);
  }

  // -- editor header component --

  @RequiresEdt
  private void insertEditorHeaderComponentIfApplicable(@NotNull VirtualFile file) {
    if (!isApplicableForToolbar(file)) {
      return;
    }
    if (ReadAction.compute(() -> shouldSkipEditorHeaderCreation(file))) {
      return;
    }

    FileEditorManager fileEditorManager = FileEditorManager.getInstance(myProject);
    FileEditor fileEditor = fileEditorManager.getSelectedEditor(file);
    if (fileEditor instanceof TextEditor) {
      final Editor editor = ((TextEditor)fileEditor).getEditor();
      if (editor.getHeaderComponent() instanceof GraphQLEditorHeaderComponent) {
        return;
      }
      final JComponent headerComponent = createEditorHeaderComponent(fileEditor, editor);
      editor.setHeaderComponent(headerComponent);
      if (editor instanceof EditorEx) {
        ((EditorEx)editor).setPermanentHeaderComponent(headerComponent);
      }

      reloadEndpoints();
    }
  }

  private static boolean isApplicableForToolbar(@NotNull VirtualFile file) {
    return GraphQLFileType.isGraphQLFile(file);
  }

  private boolean shouldSkipEditorHeaderCreation(@NotNull VirtualFile file) {
    return GraphQLLibraryManager.getInstance(myProject).isLibraryRoot(file) ||
           GraphQLGeneratedSourcesManager.getInstance(myProject).isGeneratedFile(file) ||
           GraphQLRemoteSchemasRegistry.getInstance(myProject).isRemoteSchemaFile(file);
  }

  private static class GraphQLEditorHeaderComponent extends EditorHeaderComponent {
  }

  private JComponent createEditorHeaderComponent(@NotNull FileEditor fileEditor, @NotNull Editor editor) {
    final GraphQLEditorHeaderComponent headerComponent = new GraphQLEditorHeaderComponent();

    // variables & settings actions
    final DefaultActionGroup settingsActions = new DefaultActionGroup();
    settingsActions.add(new GraphQLOpenConfigAction());
    settingsActions.add(ActionManager.getInstance().getAction(GraphQLEditEnvironmentVariablesAction.ACTION_ID));
    settingsActions.addSeparator();
    settingsActions.add(new GraphQLToggleVariablesAction());

    final JComponent settingsToolbar = createToolbar(settingsActions, headerComponent);
    headerComponent.add(settingsToolbar, BorderLayout.WEST);

    // query execute
    final DefaultActionGroup queryActions = new DefaultActionGroup();
    final AnAction executeGraphQLAction = ActionManager.getInstance().getAction(GraphQLExecuteEditorAction.ACTION_ID);
    queryActions.add(executeGraphQLAction);
    queryActions.addSeparator();
    queryActions.add(new GraphQLRunIntrospectionQueryAction());
    queryActions.add(new GraphQLOpenIntrospectionSchemaAction());
    final JComponent queryToolbar = createToolbar(queryActions, headerComponent);

    // configured endpoints combo box

    final GraphQLEndpointsModel endpointsModel =
      new GraphQLEndpointsModel(Collections.emptyList(), PropertiesComponent.getInstance(myProject));
    final ComboBox<?> endpointComboBox = new ComboBox<>(endpointsModel);
    endpointComboBox.setToolTipText(GraphQLBundle.message("graphql.endpoint.tooltip"));
    editor.putUserData(GRAPH_QL_ENDPOINTS_MODEL, endpointsModel);
    final JPanel endpointComboBoxPanel = new JPanel(new BorderLayout());
    endpointComboBoxPanel.setBorder(BorderFactory.createEmptyBorder(1, 2, 2, 2));
    endpointComboBoxPanel.add(endpointComboBox);

    // splitter to resize endpoints
    final OnePixelSplitter splitter = new OnePixelSplitter(false, .25F);
    splitter.setBorder(BorderFactory.createEmptyBorder());
    splitter.setFirstComponent(endpointComboBoxPanel);
    splitter.setSecondComponent(queryToolbar);
    splitter.setHonorComponentsMinimumSize(true);
    splitter.setAndLoadSplitterProportionKey("JSGraphQLEndpointSplitterProportion");
    splitter.setOpaque(false);
    splitter.getDivider().setOpaque(false);

    headerComponent.add(splitter, BorderLayout.CENTER);

    // variables editor
    final LightVirtualFile virtualFile = new LightVirtualFile(GRAPH_QL_VARIABLES_JSON, JsonFileType.INSTANCE, "");
    final FileEditor variablesFileEditor = TextEditorProvider.getInstance().createEditor(myProject, virtualFile);
    Disposer.register(fileEditor, variablesFileEditor);

    final EditorEx variablesEditor = (EditorEx)((TextEditor)variablesFileEditor).getEditor();
    virtualFile.putUserData(IS_GRAPH_QL_VARIABLES_VIRTUAL_FILE, Boolean.TRUE);
    variablesEditor.setPlaceholder(VARIABLES_PLACEHOLDER);
    variablesEditor.setShowPlaceholderWhenFocused(true);
    variablesEditor.getSettings().setRightMarginShown(false);
    variablesEditor.getSettings().setAdditionalLinesCount(0);
    variablesEditor.getSettings().setShowIntentionBulb(false);
    variablesEditor.getSettings().setFoldingOutlineShown(false);
    variablesEditor.getSettings().setLineNumbersShown(false);
    variablesEditor.getSettings().setLineMarkerAreaShown(false);
    variablesEditor.getSettings().setCaretRowShown(false);
    variablesEditor.putUserData(GRAPH_QL_ENDPOINTS_MODEL, endpointsModel);

    variablesFileEditor.getComponent().setPreferredSize(JBUI.size(Integer.MAX_VALUE, 150));

    // link the query and variables editor together
    variablesEditor.putUserData(GRAPH_QL_QUERY_EDITOR, editor);
    editor.putUserData(GRAPH_QL_VARIABLES_EDITOR, variablesEditor);

    final NonOpaquePanel variablesPanel = new NonOpaquePanel(variablesFileEditor.getComponent());
    variablesPanel.setBorder(IdeBorderFactory.createBorder(SideBorder.TOP));
    // hide variables by default
    variablesPanel.setVisible(false);
    variablesEditor.putUserData(GRAPH_QL_QUERY_COMPONENT, variablesPanel);

    headerComponent.add(variablesPanel, BorderLayout.SOUTH);

    return headerComponent;
  }

  private static JComponent createToolbar(ActionGroup actionGroup, JComponent parent) {
    final ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.EDITOR_TOOLBAR, actionGroup, true);
    toolbar.setReservePlaceAutoPopupIcon(false); // don't want space after the last button
    toolbar.setTargetComponent(parent);
    final JComponent component = toolbar.getComponent();
    component.setBorder(BorderFactory.createEmptyBorder());
    return component;
  }

  public void executeGraphQL(@NotNull Editor editor, @NotNull VirtualFile virtualFile) {
    final GraphQLEndpointsModel endpointsModel = editor.getUserData(GRAPH_QL_ENDPOINTS_MODEL);
    if (endpointsModel == null) {
      return;
    }
    final GraphQLConfigEndpoint selectedEndpoint =
      GraphQLIntrospectionUtil.promptForEnvVariables(myProject, endpointsModel.getSelectedItem());
    if (selectedEndpoint == null || selectedEndpoint.getUrl() == null) {
      return;
    }

    final GraphQLQueryContext context = GraphQLQueryContextHighlightVisitor.getQueryContextBufferAndHighlightUnused(editor);

    Map<String, Object> requestData = new HashMap<>();
    requestData.put("query", context.query);
    try {
      requestData.put("variables", getQueryVariables(editor));
    }
    catch (JsonSyntaxException jse) {
      Editor errorEditor = editor.getUserData(GRAPH_QL_VARIABLES_EDITOR);
      @NlsSafe String errorMessage = jse.getMessage();
      if (errorEditor != null) {
        errorEditor.getContentComponent().grabFocus();
        final VirtualFile errorFile = FileDocumentManager.getInstance().getFile(errorEditor.getDocument());
        if (errorFile != null) {
          final List<CodeSmellInfo> errors = CodeSmellDetector.getInstance(myProject).findCodeSmells(
            Collections.singletonList(errorFile));
          for (CodeSmellInfo error : errors) {
            errorMessage = error.getDescription();
            errorEditor.getCaretModel().moveToOffset(error.getTextRange().getStartOffset());
            break;
          }
        }
      }
      else {
        errorEditor = editor;
      }
      final HintManagerImpl hintManager = HintManagerImpl.getInstanceImpl();
      final JComponent label = HintUtil.createErrorLabel(
        GraphQLBundle.message("graphql.hint.text.failed.to.parse.variables.as.json", errorMessage));
      final LightweightHint lightweightHint = new LightweightHint(label);
      final Point hintPosition = hintManager.getHintPosition(lightweightHint, errorEditor, HintManager.UNDER);
      hintManager.showEditorHint(lightweightHint, editor, hintPosition, 0, 10000, false, HintManager.UNDER);
      return;
    }
    String requestJson = createQueryJsonSerializer().toJson(requestData);
    final String url = selectedEndpoint.getUrl();
    try {
      final HttpPost request = GraphQLIntrospectionService.createRequest(selectedEndpoint, url, requestJson);
      //noinspection DialogTitleCapitalization
      final Task.Backgroundable task =
        new Task.Backgroundable(myProject, GraphQLBundle.message("graphql.progress.title.executing.graphql"), false) {
          @Override
          public void run(@NotNull ProgressIndicator indicator) {
            indicator.setIndeterminate(true);
            runQuery(editor, virtualFile, context, url, request, selectedEndpoint);
          }
        };
      ProgressManager.getInstance().run(task);
    }
    catch (IllegalStateException | IllegalArgumentException e) {
      LOG.warn(e);
      GraphQLNotificationUtil.showGraphQLRequestErrorNotification(myProject, url, e, NotificationType.ERROR, null);
    }
  }

  private void runQuery(@NotNull Editor editor,
                        @NotNull VirtualFile virtualFile,
                        @NotNull GraphQLQueryContext context,
                        @NotNull String url,
                        @NotNull HttpPost request,
                        @NotNull GraphQLConfigEndpoint endpoint) {
    GraphQLIntrospectionService introspectionService = GraphQLIntrospectionService.getInstance(myProject);
    try {
      GraphQLConfigSecurity sslConfig = GraphQLConfigSecurity.getSecurityConfig(endpoint.getConfig());
      try (final CloseableHttpClient httpClient = introspectionService.createHttpClient(url, sslConfig)) {
        editor.putUserData(GRAPH_QL_EDITOR_QUERYING, true);

        String responseJson;
        Header contentType;
        long start = System.currentTimeMillis();
        long end;
        try (final CloseableHttpResponse response = httpClient.execute(request)) {
          responseJson = StringUtil.notNullize(EntityUtils.toString(response.getEntity()));
          contentType = response.getFirstHeader("Content-Type");
        }
        finally {
          end = System.currentTimeMillis();
        }

        final boolean reformatJson = contentType != null && contentType.getValue() != null &&
                                     contentType.getValue().startsWith("application/json");
        final Integer errorCount = getErrorCount(responseJson);
        ApplicationManager.getApplication().invokeLater(() -> {
          TextEditor queryResultEditor = GraphQLToolWindow.getQueryResultEditor(myProject);
          if (queryResultEditor == null) {
            return;
          }

          updateQueryResultEditor(responseJson, queryResultEditor, reformatJson);
          String queryResultText = GraphQLBundle.message(
            "graphql.query.result.statistics",
            virtualFile.getName(),
            end - start,
            bytesToDisplayString(responseJson.length())
          );

          if (errorCount != null && errorCount > 0) {
            queryResultText += GraphQLBundle.message(
              "graphql.query.result.statistics.error",
              errorCount,
              errorCount > 1
              ? GraphQLBundle.message("graphql.query.result.statistics.multiple.errors")
              : GraphQLBundle.message("graphql.query.result.statistics.single.error")
            );

            if (context.onError != null) {
              context.onError.run();
            }
          }

          GraphQLToolWindow.GraphQLQueryResultHeaderComponent queryResultHeader =
            GraphQLToolWindow.getQueryResultHeader(queryResultEditor);
          if (queryResultHeader == null) return;

          JBLabel queryResultLabel = queryResultHeader.getResultLabel();
          @NlsSafe String resultTextString = queryResultText;
          queryResultLabel.setText(resultTextString);
          queryResultLabel.putClientProperty(GraphQLToolWindow.FILE_URL_PROPERTY, virtualFile.getUrl());
          if (!queryResultLabel.isVisible()) {
            queryResultLabel.setVisible(true);
          }

          JBLabel queryStatusLabel = queryResultHeader.getStatusLabel();
          queryStatusLabel.setVisible(errorCount != null);
          if (queryStatusLabel.isVisible() && errorCount != null) {
            if (errorCount == 0) {
              queryStatusLabel.setBorder(BorderFactory.createEmptyBorder(2, 8, 0, 0));
              queryStatusLabel.setIcon(AllIcons.General.InspectionsOK);
            }
            else {
              queryStatusLabel.setBorder(BorderFactory.createEmptyBorder(2, 12, 0, 4));
              queryStatusLabel.setIcon(AllIcons.Ide.ErrorPoint);
            }
          }

          GraphQLToolWindow.showQueryResultEditor(myProject);
        });
      }
      finally {
        editor.putUserData(GRAPH_QL_EDITOR_QUERYING, null);
      }
    }
    catch (IOException | GeneralSecurityException e) {
      LOG.warn(e);
      GraphQLNotificationUtil.showGraphQLRequestErrorNotification(myProject, url, e, NotificationType.WARNING, null);
    }
  }

  public void showQueryResult(@NotNull String jsonResponse) {
    ApplicationManager.getApplication().invokeLater(() -> {
      TextEditor textEditor = GraphQLToolWindow.getQueryResultEditor(myProject);
      if (textEditor == null) return;

      updateQueryResultEditor(jsonResponse, textEditor, true);
      GraphQLToolWindow.showQueryResultEditor(myProject);
    });
  }

  private void updateQueryResultEditor(final String responseJson, TextEditor textEditor, boolean reformatJson) {
    ApplicationManager.getApplication().runWriteAction(() -> {
      String documentJson = StringUtil.convertLineSeparators(responseJson);
      if (reformatJson) {
        final PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(myProject);
        final PsiFile jsonPsiFile = psiFileFactory.createFileFromText("", JsonFileType.INSTANCE, documentJson);
        CodeStyleManager.getInstance(myProject).reformat(jsonPsiFile);
        final Document document = jsonPsiFile.getViewProvider().getDocument();
        if (document != null) {
          documentJson = document.getText();
        }
      }

      final Document document = textEditor.getEditor().getDocument();
      document.setText(documentJson);
    });
  }

  private static @NotNull Gson createQueryJsonSerializer() {
    return new GsonBuilder()
      .registerTypeAdapter(Double.class, (JsonSerializer<Double>)(number, type, jsonSerializationContext) -> {
        if (!Double.isFinite(number)) {
          throw new IllegalArgumentException(String.format("'%s' is not a valid number", number));
        }

        // convert `12.0` to `12` to conform Int types
        if (number == Math.rint(number)) {
          return new JsonPrimitive(number.intValue());
        }

        return new JsonPrimitive(number);
      })
      // explicit nulls could be a part of a service api
      .serializeNulls()
      .create();
  }

  private static Integer getErrorCount(String responseJson) {
    try {
      final Map<?, ?> res = new Gson().fromJson(responseJson, Map.class);
      if (res != null) {
        final Object errors = res.get("errors");
        if (errors instanceof Collection) {
          return ((Collection<?>)errors).size();
        }
        return 0;
      }
    }
    catch (JsonSyntaxException ignored) {
    }
    return null;
  }

  private static Object getQueryVariables(Editor editor) {
    final Editor variablesEditor = editor.getUserData(GRAPH_QL_VARIABLES_EDITOR);
    if (variablesEditor != null) {
      final String variables = variablesEditor.getDocument().getText();
      if (!variables.isBlank()) {
        return new Gson().fromJson(variables, Map.class);
      }
    }
    return null;
  }

  private static String bytesToDisplayString(long bytes) {
    if (bytes < 1000) return GraphQLBundle.message("graphql.query.result.window.bytes.count", bytes);
    int exp = (int)(Math.log(bytes) / Math.log(1000));
    String pre = ("kMGTPE").charAt(exp - 1) + "";
    return String.format("%.1f %sb", bytes / Math.pow(1000, exp), pre);
  }

  public static void setHeadersFromOptions(GraphQLConfigEndpoint endpoint, HttpRequest request) {
    final Map<String, Object> headers = endpoint.getHeaders();
    for (Map.Entry<String, Object> entry : headers.entrySet()) {
      Object value = entry.getValue();
      if (value == null) continue;

      request.setHeader(entry.getKey(), String.valueOf(value));
    }
  }

  @Override
  public void dispose() {
    removeHeaderComponents();
  }

  private void removeHeaderComponents() {
    for (FileEditor fileEditor : FileEditorManager.getInstance(myProject).getAllEditors()) {
      if (!(fileEditor instanceof TextEditor)) {
        continue;
      }

      Editor editor = ((TextEditor)fileEditor).getEditor();
      if (!(editor.getHeaderComponent() instanceof GraphQLEditorHeaderComponent)) {
        continue;
      }

      if (editor instanceof EditorEx) {
        ((EditorEx)editor).setPermanentHeaderComponent(null);
      }
      editor.setHeaderComponent(null);
      editor.putUserData(GRAPH_QL_ENDPOINTS_MODEL, null);
      editor.putUserData(GRAPH_QL_VARIABLES_EDITOR, null);
    }
  }
}
