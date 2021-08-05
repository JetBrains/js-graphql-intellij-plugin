package com.intellij.lang.jsgraphql.ide.notifications;

import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.lang.jsgraphql.GraphQLConstants;
import com.intellij.lang.jsgraphql.GraphQLSettings;
import com.intellij.lang.jsgraphql.ide.editor.GraphQLIntrospectionService;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ObjectUtils;
import com.intellij.lang.jsgraphql.types.GraphQLException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLException;

public class GraphQLNotificationUtil {
    private static final Logger LOG = Logger.getInstance(GraphQLNotificationUtil.class);
    public static final String NOTIFICATION_GROUP_ID = GraphQLConstants.GraphQL;

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

    public static void showGraphQLRequestErrorNotification(@NotNull Project project,
                                                           @NotNull String url,
                                                           @NotNull Exception error,
                                                           @NotNull NotificationType notificationType,
                                                           @Nullable NotificationAction action) {
        LOG.info(error);

        boolean isSSLError = error instanceof SSLException;
        final String message = isSSLError
            ? GraphQLBundle.message("graphql.notification.ssl.cert.error.title")
            : GraphQLBundle.message("graphql.notification.error.title");

        Notification notification = new Notification(
            NOTIFICATION_GROUP_ID,
            message,
            url + ": " + GraphQLNotificationUtil.formatExceptionMessage(error),
            notificationType
        );

        if (isSSLError) {
            NotificationAction trustAction = GraphQLIntrospectionService.getInstance(project).createTrustAllHostsAction();
            if (trustAction != null) {
                notification.addAction(trustAction);
            }
        }

        if (action != null) {
            notification.addAction(action);
        }

        Notifications.Bus.notify(notification, project);
    }

    public static void addRetryFailedSchemaIntrospectionAction(@NotNull Notification notification,
                                                               @NotNull GraphQLSettings settings,
                                                               @NotNull Exception e,
                                                               @NotNull Runnable retry) {
        if (!(e instanceof GraphQLException)) return;

        notification.setContent(GraphQLBundle.message("graphql.notification.introspection.spec.error.body", GraphQLNotificationUtil.formatExceptionMessage(e)));
        addRetryWithoutDefaultValuesAction(notification, settings, retry);
    }

    private static void addRetryWithoutDefaultValuesAction(@NotNull Notification notification,
                                                           @NotNull GraphQLSettings settings,
                                                           @NotNull Runnable retry) {
        if (settings.isEnableIntrospectionDefaultValues()) {
            // suggest retrying without the default values as they're a common cause of spec compliance issues
            NotificationAction retryWithoutDefaultValues = new NotificationAction(GraphQLBundle.message("graphql.notification.retry.without.defaults")) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    settings.setEnableIntrospectionDefaultValues(false);
                    ApplicationManager.getApplication().saveSettings();
                    notification.expire();
                    retry.run();
                }
            };
            notification.addAction(retryWithoutDefaultValues);
        }
    }

    public static String formatExceptionMessage(@NotNull Exception exception) {
        return StringUtil.decapitalize(ObjectUtils.coalesce(exception.getMessage(), ""));
    }
}
