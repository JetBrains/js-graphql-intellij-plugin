/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.frameworks.relay;

import com.intellij.lang.jsgraphql.GraphQLSettings;
import com.intellij.lang.jsgraphql.ide.notifications.GraphQLNotificationUtil;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.concurrency.NonUrgentExecutor;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Detects Relay Modern projects based on package.json and asks user to enable support for the directives.
 */
public class GraphQLRelayModernEnableStartupActivity implements StartupActivity {

    private static final Logger LOG = Logger.getInstance(GraphQLRelayModernEnableStartupActivity.class);
    private final AtomicBoolean isDisplayed = new AtomicBoolean();

    @Override
    public void runActivity(@NotNull Project project) {
        ReadAction.nonBlocking(() -> {
            final GraphQLSettings settings = GraphQLSettings.getSettings(project);
            if (isDisplayed.get() || settings.isRelaySupportEnabled()) {
                // already enabled Relay Modern
                return;
            }
            try {
                final GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
                for (VirtualFile virtualFile : FilenameIndex.getVirtualFilesByName(project, "package.json", true, scope)) {
                    ProgressManager.checkCanceled();
                    if (!virtualFile.isDirectory() && virtualFile.isInLocalFileSystem()) {
                        try (InputStream inputStream = virtualFile.getInputStream()) {
                            final String packageJson = IOUtils.toString(inputStream, virtualFile.getCharset());
                            if (packageJson.contains("\"react-relay\"") || packageJson.contains("\"relay-compiler\"")) {
                                final Notification enableRelayModern = new Notification(
                                    GraphQLNotificationUtil.GRAPHQL_NOTIFICATION_GROUP_ID,
                                    "Relay Modern project detected",
                                    "<a href=\"enable\">Enable Relay Modern</a> GraphQL tooling",
                                    NotificationType.INFORMATION,
                                    (notification, event) -> {
                                        settings.setRelaySupportEnabled(true);
                                        ApplicationManager.getApplication().saveSettings();
                                        notification.expire();
                                        GraphQLLibraryManager.getInstance(project).notifyLibrariesChanged();
                                    });
                                enableRelayModern.setImportant(true);
                                if (isDisplayed.compareAndSet(false, true)) {
                                    Notifications.Bus.notify(enableRelayModern);
                                }
                                break;
                            }
                        }
                    }
                }
            } catch (ProcessCanceledException e) {
                throw e;
            } catch (Exception e) {
                LOG.error("Unable to detect Relay Modern", e);
            }
        }).inSmartMode(project).submit(NonUrgentExecutor.getInstance());
    }
}
