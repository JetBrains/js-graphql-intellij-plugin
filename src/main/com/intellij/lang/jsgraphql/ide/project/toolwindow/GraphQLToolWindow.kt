package com.intellij.lang.jsgraphql.ide.project.toolwindow;

import com.intellij.json.JsonFileType;
import com.intellij.lang.jsgraphql.GraphQLConstants;
import com.intellij.lang.jsgraphql.ide.project.schemastatus.GraphQLSchemasPanel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.impl.EditorHeaderComponent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GraphQLToolWindow implements ToolWindowFactory, DumbAware {

    public final static String GRAPH_QL_TOOL_WINDOW = GraphQLConstants.GraphQL;
    public static final String FILE_URL_PROPERTY = "fileUrl";

    public static final String CONTENT_QUERY_RESULT = "Query Result";
    public static final String CONTENT_SCHEMAS_AND_PROJECT_STRUCTURE = "Schemas and Project Structure";

    private static final Key<TextEditor> QUERY_RESULT_EDITOR = Key.create("graphql.query.result.editor");

    public static @Nullable TextEditor getQueryResultEditor(@NotNull Project project) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        Content content = getQueryResultContent(project);
        if (content == null) return null;
        return content.getUserData(QUERY_RESULT_EDITOR);
    }

    private static @Nullable Content getQueryResultContent(@NotNull Project project) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(GRAPH_QL_TOOL_WINDOW);
        if (toolWindow == null) return null;
        return toolWindow.getContentManager().findContent(CONTENT_QUERY_RESULT);
    }

    public static @Nullable GraphQLQueryResultHeaderComponent getQueryResultHeader(@NotNull TextEditor textEditor) {
        Editor editor = textEditor.getEditor();
        return ObjectUtils.tryCast(editor.getHeaderComponent(), GraphQLQueryResultHeaderComponent.class);
    }

    public static void showQueryResultEditor(@NotNull Project project) {
        UIUtil.invokeLaterIfNeeded(() -> {
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(GRAPH_QL_TOOL_WINDOW);
            if (toolWindow == null) return;
            Content content = toolWindow.getContentManager().findContent(CONTENT_QUERY_RESULT);
            if (content == null) return;
            toolWindow.show(() -> toolWindow.getContentManager().setSelectedContent(content));

            TextEditor textEditor = content.getUserData(QUERY_RESULT_EDITOR);
            if (textEditor != null) {
                textEditor.getEditor().getScrollingModel().scrollVertically(0);
            }
        });
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        createSchemasPanel(project, toolWindow);
        createToolWindowResultEditor(project, toolWindow);
    }

    private void createSchemasPanel(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        GraphQLSchemasPanel schemasPanel = new GraphQLSchemasPanel(project);
        ContentManager contentManager = toolWindow.getContentManager();
        Content schemasContent = contentManager.getFactory()
            .createContent(schemasPanel, CONTENT_SCHEMAS_AND_PROJECT_STRUCTURE, false);
        schemasContent.setCloseable(false);
        toolWindow.getContentManager().addContent(schemasContent);
    }

    private void createToolWindowResultEditor(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        final LightVirtualFile virtualFile = new LightVirtualFile("GraphQL.result.json", JsonFileType.INSTANCE, "");
        FileEditor fileEditor = PsiAwareTextEditorProvider.getInstance().createEditor(project, virtualFile);
        if (!(fileEditor instanceof TextEditor)) {
            return;
        }

        final Editor editor = ((TextEditor) fileEditor).getEditor();
        final EditorEx editorEx = (EditorEx) editor;

        // set read-only mode
        editorEx.setViewer(true);
        editorEx.getSettings().setShowIntentionBulb(false);
        editor.getSettings().setAdditionalLinesCount(0);
        editor.getSettings().setCaretRowShown(false);
        editor.getSettings().setBlinkCaret(false);

        final GraphQLQueryResultHeaderComponent header = new GraphQLQueryResultHeaderComponent(project);
        // finally, set the header as permanent such that it's restored after searches
        editor.setHeaderComponent(header);
        editorEx.setPermanentHeaderComponent(header);

        ContentManager contentManager = toolWindow.getContentManager();
        Content content = contentManager.getFactory()
            .createContent(fileEditor.getComponent(), CONTENT_QUERY_RESULT, true);
        content.setCloseable(false);
        content.setShouldDisposeContent(false); // fileEditor will dispose the component itself
        content.setDisposer(fileEditor);
        content.putUserData(QUERY_RESULT_EDITOR, ((TextEditor) fileEditor));
        contentManager.addContent(content);
    }

    public static class GraphQLQueryResultHeaderComponent extends EditorHeaderComponent {
        private final JBLabel queryStatusLabel;
        private final JBLabel queryResultLabel;

        public GraphQLQueryResultHeaderComponent(@NotNull Project project) {
            super();

            queryStatusLabel = new JBLabel();
            queryStatusLabel.setVisible(false);
            queryStatusLabel.setIconTextGap(0);
            add(queryStatusLabel, BorderLayout.WEST);

            queryResultLabel = new JBLabel("", null, SwingConstants.LEFT);
            queryResultLabel.setBorder(JBUI.Borders.empty(4, 6));
            queryResultLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            queryResultLabel.setVisible(false);
            queryResultLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    final String fileUrl = (String) queryResultLabel.getClientProperty(FILE_URL_PROPERTY);
                    if (fileUrl != null) {
                        final VirtualFile queryFile = VirtualFileManager.getInstance().findFileByUrl(fileUrl);
                        if (queryFile != null) {
                            final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                            fileEditorManager.openFile(queryFile, true, true);
                        }
                    }
                }
            });
            add(queryResultLabel, BorderLayout.CENTER);
        }

        public @NotNull JBLabel getStatusLabel() {
            return queryStatusLabel;
        }

        public @NotNull JBLabel getResultLabel() {
            return queryResultLabel;
        }
    }
}
