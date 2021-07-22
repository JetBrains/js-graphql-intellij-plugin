/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.editor;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.lang.jsgraphql.GraphQLSettings;
import com.intellij.lang.jsgraphql.ide.notifications.GraphQLNotificationUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Line marker which shows an action to turn a GraphQL Introspection JSON result into a GraphQL schema expressed in GraphQL SDL.
 */
public class GraphQLIntrospectionJsonToSDLLineMarkerProvider implements LineMarkerProvider {
    @Nullable
    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        final VirtualFile virtualFile = element.isValid() ? element.getContainingFile().getVirtualFile() : null;
        if (virtualFile != null && !virtualFile.isInLocalFileSystem()) {
            // skip in-memory JSON files such as the query result viewer
            return null;
        }
        if (!(element instanceof JsonProperty)) {
            return null;
        }
        final Project project = element.getProject();
        final JsonProperty parentProperty = PsiTreeUtil.getParentOfType(element, JsonProperty.class);
        if (parentProperty != null && !"data".equals(parentProperty.getName())) {
            return null;
        }

        // top level property or inside data property
        final JsonProperty jsonProperty = (JsonProperty) element;
        final String propertyName = jsonProperty.getName();
        if (!"__schema".equals(propertyName) || !(jsonProperty.getValue() instanceof JsonObject)) {
            return null;
        }

        for (JsonProperty property : ((JsonObject) jsonProperty.getValue()).getPropertyList()) {
            if ("types".equals(property.getName()) && property.getValue() instanceof JsonArray) {
                // likely a GraphQL schema with a { __schema: { types: [] } }
                final GraphQLIntrospectionService graphQLIntrospectionService = GraphQLIntrospectionService.getInstance(project);
                final Ref<Runnable> generateAction = Ref.create();
                generateAction.set(() -> {
                    try {
                        final String introspectionJson = element.getContainingFile().getText();
                        final String schemaAsSDL = graphQLIntrospectionService.printIntrospectionAsGraphQL(introspectionJson);

                        final VirtualFile jsonFile = element.getContainingFile().getVirtualFile();
                        final String outputFileName = jsonFile.getName() + ".graphql";

                        graphQLIntrospectionService.createOrUpdateIntrospectionOutputFile(schemaAsSDL, GraphQLIntrospectionService.IntrospectionOutputFormat.SDL, jsonFile, outputFileName);
                    } catch (ProcessCanceledException e) {
                        throw e;
                    } catch (Exception e) {
                        Notification notification = new Notification(
                            GraphQLNotificationUtil.NOTIFICATION_GROUP_ID,
                            GraphQLBundle.message("graphql.notification.introspection.error.title"),
                            GraphQLNotificationUtil.formatExceptionMessage(e),
                            NotificationType.ERROR
                        );

                        GraphQLNotificationUtil.addRetryFailedSchemaIntrospectionAction(notification, GraphQLSettings.getSettings(project), e, generateAction.get());
                        graphQLIntrospectionService.addIntrospectionStackTraceAction(notification, e);
                        Notifications.Bus.notify(notification.setImportant(true));
                    }
                });

                PsiElement anchor = jsonProperty.getNameElement().getFirstChild();
                if (anchor == null) return null;

                return new LineMarkerInfo<>(
                    anchor,
                    anchor.getTextRange(),
                    AllIcons.RunConfigurations.TestState.Run,
                    o -> GraphQLBundle.message("graphql.line.marker.generate.schema.file"),
                    (evt, elt) -> generateAction.get().run(),
                    GutterIconRenderer.Alignment.CENTER,
                    () -> GraphQLBundle.message("graphql.line.marker.generate.schema.file")
                );
            }
        }
        return null;
    }
}
