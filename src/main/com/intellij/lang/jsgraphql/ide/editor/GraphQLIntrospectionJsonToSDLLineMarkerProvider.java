/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.editor;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Line marker which shows an action to turn a GraphQL Introspection JSON result into a GraphQL schema expressed in GraphQL SDL.
 */
public class GraphQLIntrospectionJsonToSDLLineMarkerProvider implements LineMarkerProvider {
    @Nullable
    @Override
    @SuppressWarnings(value = "unchecked")
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        final VirtualFile virtualFile = element.isValid() ? element.getContainingFile().getVirtualFile() : null;
        if (virtualFile != null && !virtualFile.isInLocalFileSystem()) {
            // skip in-memory JSON files such as the query result viewer
            return null;
        }
        if (element instanceof JsonProperty) {
            final JsonProperty parentProperty = PsiTreeUtil.getParentOfType(element, JsonProperty.class);
            if (parentProperty == null || "data".equals(parentProperty.getName())) {
                // top level property or inside data property
                final JsonProperty jsonProperty = (JsonProperty) element;
                final String propertyName = jsonProperty.getName();
                if ("__schema".equals(propertyName) && jsonProperty.getValue() instanceof JsonObject) {
                    for (JsonProperty property : ((JsonObject) jsonProperty.getValue()).getPropertyList()) {
                        if ("types".equals(property.getName()) && property.getValue() instanceof JsonArray) {
                            // likely a GraphQL schema with a { __schema: { types: [] } }
                            return new LineMarkerInfo<>(jsonProperty, jsonProperty.getTextRange(), AllIcons.General.Run, Pass.UPDATE_ALL, o -> "Generate GraphQL SDL schema file", (e, elt) -> {
                                try {
                                    final GraphQLIntrospectionHelper graphQLIntrospectionHelper = GraphQLIntrospectionHelper.getService(element.getProject());
                                    final String introspectionJson = element.getContainingFile().getText();
                                    final String schemaAsSDL = graphQLIntrospectionHelper.printIntrospectionJsonAsGraphQL(introspectionJson);

                                    final VirtualFile jsonFile = element.getContainingFile().getVirtualFile();
                                    final String outputFileName = jsonFile.getName() + ".graphql";
                                    final Project project = element.getProject();

                                    graphQLIntrospectionHelper.createOrUpdateIntrospectionOutputFile(schemaAsSDL, GraphQLIntrospectionHelper.IntrospectionOutputFormat.SDL, jsonFile, outputFileName, project);

                                } catch (Exception exception) {
                                    Notifications.Bus.notify(new Notification("GraphQL", "Unable to create GraphQL SDL", exception.getMessage(), NotificationType.ERROR));
                                }
                            }, GutterIconRenderer.Alignment.CENTER);
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {

    }
}
