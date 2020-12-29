/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.frameworks.relay;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.lang.jsgraphql.GraphQLSettings;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.EditorNotifications;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

/**
 * Detects Relay Modern projects based on package.json and asks user to enable support for the directives.
 */
public class GraphQLRelayModernEnableStartupActivity implements StartupActivity {

    private static final Logger log = Logger.getInstance(GraphQLRelayModernEnableStartupActivity.class);

    @Override
    public void runActivity(@NotNull Project project) {
        final GraphQLSettings settings = GraphQLSettings.getSettings(project);
        if (settings.isEnableRelayModernFrameworkSupport()) {
            // already enabled Relay Modern
            return;
        }
        try {
            final GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
            for (VirtualFile virtualFile : FilenameIndex.getVirtualFilesByName(project, "package.json", scope)) {
                if (!virtualFile.isDirectory() && virtualFile.isInLocalFileSystem()) {
                    try (InputStream inputStream = virtualFile.getInputStream()) {
                        final String packageJson = IOUtils.toString(inputStream, virtualFile.getCharset());
                        if (packageJson.contains("\"react-relay\"") || packageJson.contains("\"relay-compiler\"")) {
                            final Notification enableRelayModern = new Notification("GraphQL", "Relay Modern project detected", "<a href=\"enable\">Enable Relay Modern</a> GraphQL tooling", NotificationType.INFORMATION, (notification, event) -> {
                                settings.setEnableRelayModernFrameworkSupport(true);
                                ApplicationManager.getApplication().saveSettings();
                                notification.expire();
                                DaemonCodeAnalyzer.getInstance(project).restart();
                                EditorNotifications.getInstance(project).updateAllNotifications();
                            });
                            enableRelayModern.setImportant(true);
                            Notifications.Bus.notify(enableRelayModern);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Unable to detect Relay Modern", e);
        }
    }
}
