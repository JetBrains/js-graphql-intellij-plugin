package com.intellij.lang.jsgraphql.ide.notifications;

import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraphQLNotificationUtil {
    public static final String NOTIFICATION_GROUP_ID = "GraphQL";

    public static void showInvalidConfigurationNotification(@NotNull String message,
                                                            @Nullable VirtualFile introspectionSourceFile,
                                                            @NotNull Project project) {
        Notification notification = new Notification(
            NOTIFICATION_GROUP_ID,
            GraphQLBundle.message("graphql.notification.invalid.config.file"),
            message,
            NotificationType.WARNING
        );

        if (introspectionSourceFile != null) {
            notification.addAction(new NotificationAction(introspectionSourceFile.getName()) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    FileEditorManager.getInstance(project).openFile(introspectionSourceFile, true);
                }
            });
        }

        Notifications.Bus.notify(notification);
    }

    public static void showRequestExceptionNotification(@NotNull NotificationAction retry,
                                                        @NotNull String url,
                                                        @NotNull String error,
                                                        @NotNull NotificationType notificationType,
                                                        @NotNull Project project) {
        Notifications.Bus.notify(
            new Notification(
                NOTIFICATION_GROUP_ID,
                GraphQLBundle.message("graphql.notification.error.title"),
                url + ": " + error,
                notificationType
            ).addAction(retry),
            project
        );
    }

    public static String formatExceptionMessage(@NotNull Exception exception) {
        return StringUtil.decapitalize(ObjectUtils.coalesce(exception.getMessage(), ""));
    }
}
