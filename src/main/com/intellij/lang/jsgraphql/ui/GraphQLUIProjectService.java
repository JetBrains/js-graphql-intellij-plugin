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
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.GraphQLParserDefinition;
import com.intellij.lang.jsgraphql.ide.actions.GraphQLExecuteEditorAction;
import com.intellij.lang.jsgraphql.ide.actions.GraphQLOpenConfigAction;
import com.intellij.lang.jsgraphql.ide.actions.GraphQLToggleVariablesAction;
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigListener;
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider;
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig;
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigEndpoint;
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigSecurity;
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig;
import com.intellij.lang.jsgraphql.ide.highlighting.query.GraphQLQueryContext;
import com.intellij.lang.jsgraphql.ide.highlighting.query.GraphQLQueryContextHighlightVisitor;
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLIntrospectionService;
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
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.impl.EditorHeaderComponent;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.CodeSmellDetector;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.source.codeStyle.CodeStyleManagerImpl;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.*;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.Alarm;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
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
import java.util.stream.Collectors;

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

    public final static Key<GraphQLEndpointsModel> GRAPH_QL_ENDPOINTS_MODEL = Key.create("graphql.endpoints.model");

    public final static Key<Boolean> GRAPH_QL_EDITOR_QUERYING = Key.create("graphql.editor.querying");

    private static final int UPDATE_MS = 500;
    private final Alarm myUpdateUIAlarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, this);

    @NotNull
    private final Project myProject;

    public GraphQLUIProjectService(@NotNull final Project project) {

        myProject = project;

        final MessageBusConnection messageBusConnection = project.getMessageBus().connect(this);

        // listen for editor file tab changes to update the list of current errors
        messageBusConnection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, this);

        // add editor headers to already open files since we've only just added the listener for fileOpened()
        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        for (VirtualFile virtualFile : fileEditorManager.getOpenFiles()) {
            UIUtil.invokeLaterIfNeeded(() -> insertEditorHeaderComponentIfApplicable(fileEditorManager, virtualFile));
        }

        // listen for configuration changes
        messageBusConnection.subscribe(GraphQLConfigListener.TOPIC, this);

        // and notify to configure the schema
        project.putUserData(GraphQLParserDefinition.JSGRAPHQL_ACTIVATED, true);
        EditorNotifications.getInstance(project).updateAllNotifications();
    }


    public static GraphQLUIProjectService getService(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLUIProjectService.class);
    }

    // ---- editor tabs listener ----

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        insertEditorHeaderComponentIfApplicable(source, file);
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
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
                () -> Arrays.stream(fileEditorManager.getOpenFiles())
                    .filter(f -> GraphQLFileType.isGraphQLFile(myProject, f))
                    .collect(Collectors.toList())
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
                                ((TextEditor) editor).getEditor().getUserData(GRAPH_QL_ENDPOINTS_MODEL);
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

    private void insertEditorHeaderComponentIfApplicable(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        if (!GraphQLFileType.isGraphQLFile(myProject, file)) {
            return;
        }
        if (ReadAction.compute(() -> GraphQLLibraryManager.getInstance(myProject).isLibraryRoot(file))) {
            return;
        }

        UIUtil.invokeLaterIfNeeded(() -> { // ensure components are created on the swing thread
            FileEditor fileEditor = source.getSelectedEditor(file);
            if (fileEditor instanceof TextEditor) {
                final Editor editor = ((TextEditor) fileEditor).getEditor();
                if (editor.getHeaderComponent() instanceof GraphQLEditorHeaderComponent) {
                    return;
                }
                final JComponent headerComponent = createEditorHeaderComponent(editor, file);
                editor.setHeaderComponent(headerComponent);
                if (editor instanceof EditorEx) {
                    ((EditorEx) editor).setPermanentHeaderComponent(headerComponent);
                }
            }
        });
    }

    private static class GraphQLEditorHeaderComponent extends EditorHeaderComponent {
    }

    private JComponent createEditorHeaderComponent(@NotNull Editor editor, @NotNull VirtualFile file) {
        GraphQLProjectConfig config = ReadAction.compute(() -> GraphQLConfigProvider.getInstance(myProject).resolveProjectConfig(file));
        List<GraphQLConfigEndpoint> endpoints = config != null ? config.getEndpoints() : Collections.emptyList();

        final GraphQLEditorHeaderComponent headerComponent = new GraphQLEditorHeaderComponent();

        // variables & settings actions
        final DefaultActionGroup settingsActions = new DefaultActionGroup();
        settingsActions.add(new GraphQLOpenConfigAction());
        settingsActions.add(new GraphQLToggleVariablesAction());

        final JComponent settingsToolbar = createToolbar(settingsActions, headerComponent);
        headerComponent.add(settingsToolbar, BorderLayout.WEST);

        // query execute
        final DefaultActionGroup queryActions = new DefaultActionGroup();
        final AnAction executeGraphQLAction = ActionManager.getInstance().getAction(GraphQLExecuteEditorAction.class.getName());
        queryActions.add(executeGraphQLAction);
        final JComponent queryToolbar = createToolbar(queryActions, headerComponent);

        // configured endpoints combo box

        final GraphQLEndpointsModel endpointsModel = new GraphQLEndpointsModel(endpoints, PropertiesComponent.getInstance(myProject));
        final ComboBox<?> endpointComboBox = new ComboBox(endpointsModel);
        endpointComboBox.setToolTipText("GraphQL endpoint");
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
        final FileEditor variablesFileEditor = PsiAwareTextEditorProvider.getInstance().createEditor(myProject, virtualFile);
        Disposer.register(this, variablesFileEditor);
        final EditorEx variablesEditor = (EditorEx) ((TextEditor) variablesFileEditor).getEditor();
        virtualFile.putUserData(IS_GRAPH_QL_VARIABLES_VIRTUAL_FILE, Boolean.TRUE);
        variablesEditor.setPlaceholder("{ variables }");
        variablesEditor.setShowPlaceholderWhenFocused(true);
        variablesEditor.getSettings().setRightMarginShown(false);
        variablesEditor.getSettings().setAdditionalLinesCount(0);
        variablesEditor.getSettings().setShowIntentionBulb(false);
        variablesEditor.getSettings().setFoldingOutlineShown(false);
        variablesEditor.getSettings().setLineNumbersShown(false);
        variablesEditor.getSettings().setLineMarkerAreaShown(false);
        variablesEditor.getSettings().setCaretRowShown(false);
        variablesEditor.putUserData(GRAPH_QL_ENDPOINTS_MODEL, endpointsModel);

        // hide variables by default
        variablesEditor.getComponent().setVisible(false);

        // link the query and variables editor together
        variablesEditor.putUserData(GRAPH_QL_QUERY_EDITOR, editor);
        editor.putUserData(GRAPH_QL_VARIABLES_EDITOR, variablesEditor);

        final NonOpaquePanel variablesPanel = new NonOpaquePanel(variablesFileEditor.getComponent());
        variablesPanel.setBorder(IdeBorderFactory.createBorder(SideBorder.TOP));

        headerComponent.add(variablesPanel, BorderLayout.SOUTH);

        return headerComponent;
    }

    private JComponent createToolbar(ActionGroup actionGroup, JComponent parent) {
        final ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.EDITOR_TOOLBAR, actionGroup, true);
        toolbar.setReservePlaceAutoPopupIcon(false); // don't want space after the last button
        toolbar.setTargetComponent(parent);
        final JComponent component = toolbar.getComponent();
        component.setBorder(BorderFactory.createEmptyBorder());
        return component;
    }

    public void executeGraphQL(Editor editor, VirtualFile virtualFile) {
        final GraphQLEndpointsModel endpointsModel = editor.getUserData(GRAPH_QL_ENDPOINTS_MODEL);
        if (endpointsModel == null) {
            return;
        }
        final GraphQLConfigEndpoint selectedEndpoint = endpointsModel.getSelectedItem();
        if (selectedEndpoint == null || selectedEndpoint.getUrl() == null) {
            return;
        }

        final GraphQLQueryContext context = GraphQLQueryContextHighlightVisitor.getQueryContextBufferAndHighlightUnused(editor);

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("query", context.query);
        try {
            requestData.put("variables", getQueryVariables(editor));
        } catch (JsonSyntaxException jse) {
            Editor errorEditor = editor.getUserData(GRAPH_QL_VARIABLES_EDITOR);
            String errorMessage = jse.getMessage();
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
            } else {
                errorEditor = editor;
            }
            final HintManagerImpl hintManager = HintManagerImpl.getInstanceImpl();
            final JComponent label = HintUtil.createErrorLabel("Failed to parse variables as JSON:\n" + errorMessage);
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
            final Task.Backgroundable task = new Task.Backgroundable(myProject, "Executing GraphQL", false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    indicator.setIndeterminate(true);
                    runQuery(editor, virtualFile, context, url, request);
                }
            };
            ProgressManager.getInstance().run(task);
        } catch (IllegalStateException | IllegalArgumentException e) {
            LOG.warn(e);
            GraphQLNotificationUtil.showGraphQLRequestErrorNotification(myProject, url, e, NotificationType.ERROR, null);
        }

    }

    private void runQuery(Editor editor, VirtualFile virtualFile, GraphQLQueryContext context, String url, HttpPost request) {
        GraphQLIntrospectionService introspectionService = GraphQLIntrospectionService.getInstance(myProject);
        try {
            GraphQLConfigSecurity sslConfig = ReadAction.compute(() -> {
                GraphQLConfig config = GraphQLConfigProvider.getInstance(myProject).resolveConfig(virtualFile);
                return config != null ? GraphQLConfigSecurity.getSecurityConfig(config.getDefault()) : null;
            });
            try (final CloseableHttpClient httpClient = introspectionService.createHttpClient(url, sslConfig)) {
                editor.putUserData(GRAPH_QL_EDITOR_QUERYING, true);

                String responseJson;
                Header contentType;
                StopWatch sw = new StopWatch();
                sw.start();
                try (final CloseableHttpResponse response = httpClient.execute(request)) {
                    responseJson = StringUtil.notNullize(EntityUtils.toString(response.getEntity()));
                    contentType = response.getFirstHeader("Content-Type");
                } finally {
                    sw.stop();
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
                    final StringBuilder queryResultText = new StringBuilder(virtualFile.getName()).
                        append(": ").
                        append(sw.getTime()).
                        append(" ms execution time, ").
                        append(bytesToDisplayString(responseJson.length())).
                        append(" response");

                    if (errorCount != null && errorCount > 0) {
                        queryResultText.append(", ").append(errorCount).append(" error").append(errorCount > 1 ? "s" : "");
                        if (context.onError != null) {
                            context.onError.run();
                        }
                    }

                    GraphQLToolWindow.GraphQLQueryResultHeaderComponent queryResultHeader =
                        GraphQLToolWindow.getQueryResultHeader(queryResultEditor);
                    if (queryResultHeader == null) return;

                    JBLabel queryResultLabel = queryResultHeader.getResultLabel();
                    queryResultLabel.setText(queryResultText.toString());
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
                        } else {
                            queryStatusLabel.setBorder(BorderFactory.createEmptyBorder(2, 12, 0, 4));
                            queryStatusLabel.setIcon(AllIcons.Ide.ErrorPoint);
                        }
                    }

                    GraphQLToolWindow.showQueryResultEditor(myProject);
                });
            } finally {
                editor.putUserData(GRAPH_QL_EDITOR_QUERYING, null);
            }
        } catch (IOException | GeneralSecurityException e) {
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
                CodeStyleManagerImpl.getInstance(myProject).reformat(jsonPsiFile);
                final Document document = jsonPsiFile.getViewProvider().getDocument();
                if (document != null) {
                    documentJson = document.getText();
                }
            }

            final Document document = textEditor.getEditor().getDocument();
            document.setText(documentJson);
        });
    }

    @NotNull
    private static Gson createQueryJsonSerializer() {
        return new GsonBuilder()
            .registerTypeAdapter(Double.class, (JsonSerializer<Double>) (number, type, jsonSerializationContext) -> {
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

    private Integer getErrorCount(String responseJson) {
        try {
            final Map res = new Gson().fromJson(responseJson, Map.class);
            if (res != null) {
                final Object errors = res.get("errors");
                if (errors instanceof Collection) {
                    return ((Collection) errors).size();
                }
                return 0;
            }
        } catch (JsonSyntaxException ignored) {
        }
        return null;
    }

    private Object getQueryVariables(Editor editor) {
        final Editor variablesEditor = editor.getUserData(GRAPH_QL_VARIABLES_EDITOR);
        if (variablesEditor != null) {
            final String variables = variablesEditor.getDocument().getText();
            if (!StringUtils.isBlank(variables)) {
                return new Gson().fromJson(variables, Map.class);
            }
        }
        return null;
    }

    private static String bytesToDisplayString(long bytes) {
        if (bytes < 1000) return bytes + " bytes";
        int exp = (int) (Math.log(bytes) / Math.log(1000));
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

            Editor editor = ((TextEditor) fileEditor).getEditor();
            if (!(editor.getHeaderComponent() instanceof GraphQLEditorHeaderComponent)) {
                continue;
            }

            if (editor instanceof EditorEx) {
                ((EditorEx) editor).setPermanentHeaderComponent(null);
            }
            editor.setHeaderComponent(null);
            editor.putUserData(GRAPH_QL_ENDPOINTS_MODEL, null);
            editor.putUserData(GRAPH_QL_VARIABLES_EDITOR, null);
        }
    }

}
