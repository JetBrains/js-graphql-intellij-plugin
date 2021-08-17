package com.intellij.lang.jsgraphql.ide.notifications;

import com.intellij.json.JsonFileType;
import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications.Provider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class GraphQLConfigInvalidFileTypeEditorNotificationProvider extends Provider<EditorNotificationPanel> {

    private static final Key<EditorNotificationPanel> KEY = Key.create("graphql.config.invalid.file.type");

    public static final String GRAPHQLCONFIG_EXT = "graphqlconfig";

    @NotNull
    public Key<EditorNotificationPanel> getKey() {
        return KEY;
    }

    @Override
    public @Nullable EditorNotificationPanel createNotificationPanel(@NotNull VirtualFile file,
                                                                     @NotNull FileEditor fileEditor,
                                                                     @NotNull Project project) {
        return showNotification(file) ? createPanel(fileEditor) : null;
    }

    @NotNull
    private EditorNotificationPanel createPanel(@NotNull FileEditor fileEditor) {
        EditorNotificationPanel panel = new EditorNotificationPanel(fileEditor);
        panel.setText(GraphQLBundle.message("graphql.notification.config.invalid.file.type"));
        panel.createActionLabel(
            GraphQLBundle.message("graphql.notification.config.invalid.file.type.associate"),
            () -> WriteAction.run(() -> FileTypeManager.getInstance().associateExtension(JsonFileType.INSTANCE, GRAPHQLCONFIG_EXT))
        );
        return panel;
    }

    private boolean showNotification(@NotNull VirtualFile file) {
        return Objects.equals(file.getExtension(), GRAPHQLCONFIG_EXT) && file.getFileType() == PlainTextFileType.INSTANCE;
    }
}
