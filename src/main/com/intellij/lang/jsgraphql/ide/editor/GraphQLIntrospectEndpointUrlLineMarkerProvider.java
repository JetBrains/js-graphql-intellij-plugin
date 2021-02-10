/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.editor;

import com.google.gson.Gson;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.json.psi.JsonValue;
import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.lang.jsgraphql.ide.notifications.GraphQLNotificationUtil;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigEndpoint;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigVariableAwareEndpoint;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Line marker for running an introspection against a configured endpoint url in a .graphqlconfig file
 */
public class GraphQLIntrospectEndpointUrlLineMarkerProvider implements LineMarkerProvider {
    private String pathOfConfigFileParent;

    @Nullable
    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!GraphQLConfigManager.GRAPHQLCONFIG.equals(element.getContainingFile().getName())) {
            return null;
        }
        if (element instanceof JsonProperty) {
            final JsonProperty jsonProperty = (JsonProperty) element;
            final Ref<String> urlRef = Ref.create();
            if (isEndpointUrl(jsonProperty, urlRef) && !hasErrors(jsonProperty.getContainingFile())) {
                return new LineMarkerInfo<>(jsonProperty, jsonProperty.getTextRange(), AllIcons.RunConfigurations.TestState.Run, Pass.UPDATE_ALL, o -> "Run introspection query to generate GraphQL SDL schema file", (evt, jsonUrl) -> {

                    String introspectionUrl;
                    if (jsonUrl.getValue() instanceof JsonStringLiteral) {
                        introspectionUrl = ((JsonStringLiteral) jsonUrl.getValue()).getValue();
                    } else {
                        return;
                    }

                    final GraphQLConfigVariableAwareEndpoint endpoint = getEndpoint(introspectionUrl, jsonProperty, element.getContainingFile().getVirtualFile());
                    if (endpoint == null) {
                        return;
                    }

                    String schemaPath = getSchemaPath(jsonProperty, true);
                    if (StringUtil.isEmptyOrSpaces(schemaPath)) {
                        return;
                    }

                    final Project project = element.getProject();
                    final VirtualFile introspectionSourceFile = element.getContainingFile().getVirtualFile();

                    GraphQLIntrospectionService.getInstance(project).performIntrospectionQueryAndUpdateSchemaPathFile(endpoint, schemaPath, introspectionSourceFile);

                }, GutterIconRenderer.Alignment.CENTER);
            }
        }
        return null;
    }

    private boolean hasErrors(PsiFile file) {
        return PsiTreeUtil.findChildOfType(file, PsiErrorElement.class) != null;
    }

    private boolean isEndpointUrl(JsonProperty jsonProperty, Ref<String> urlRef) {
        if (jsonProperty.getValue() instanceof JsonStringLiteral) {
            final String url = ((JsonStringLiteral) jsonProperty.getValue()).getValue();
            if (isUrlOrVariable(url)) {
                final JsonProperty parentProperty = PsiTreeUtil.getParentOfType(jsonProperty, JsonProperty.class);
                final JsonProperty grandParentProperty = PsiTreeUtil.getParentOfType(parentProperty, JsonProperty.class);
                if ("url".equals(jsonProperty.getName()) && grandParentProperty != null && "endpoints".equals(grandParentProperty.getName())) {
                    // "endpoints": {
                    //      "<name>": {
                    //          "url": "url" <---
                    //      }
                    // }
                    urlRef.set(url);
                    return true;
                }
                if (parentProperty != null && "endpoints".equals(parentProperty.getName())) {
                    // "endpoints": {
                    //      "<name>": "url" <---
                    // }
                    urlRef.set(url);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isUrlOrVariable(String jsonValue) {
        try {
            new URL(jsonValue);
            return true;
        } catch (MalformedURLException e) {
            return GraphQLConfigVariableAwareEndpoint.containsVariable(jsonValue);
        }
    }

    @Nullable
    private String getSchemaPath(@NotNull JsonProperty urlElement, boolean showNotificationOnMissingPath) {
        JsonObject jsonObject = PsiTreeUtil.getParentOfType(urlElement, JsonObject.class);
        while (jsonObject != null) {
            JsonProperty schemaPathElement = jsonObject.findProperty("schemaPath");
            if (schemaPathElement != null) {
                if (schemaPathElement.getValue() instanceof JsonStringLiteral) {
                    String schemaPath = ((JsonStringLiteral) schemaPathElement.getValue()).getValue();
                    if (StringUtil.isEmptyOrSpaces(schemaPath)) {
                        GraphQLNotificationUtil.showInvalidConfigurationNotification(
                            GraphQLBundle.message("graphql.notification.empty.schema.path"),
                            null,
                            urlElement.getProject()
                        );
                    }
                    return schemaPath;
                } else {
                    break;
                }
            }
            jsonObject = PsiTreeUtil.getParentOfType(jsonObject, JsonObject.class);
        }
        if (showNotificationOnMissingPath) {
            GraphQLNotificationUtil.showInvalidConfigurationNotification(
                GraphQLBundle.message("graphql.notification.empty.schema.path"),
                null,
                urlElement.getProject()
            );
        }
        return null;
    }

    private GraphQLConfigVariableAwareEndpoint getEndpoint(String url, JsonProperty urlJsonProperty, VirtualFile configFile) {
        try {
            // if the endpoint is just the url string, headers are not supported
            final JsonProperty parent = PsiTreeUtil.getParentOfType(urlJsonProperty, JsonProperty.class);
            final boolean supportsHeaders = parent != null && !"endpoints".equals(parent.getName());

            final String name = supportsHeaders ? parent.getName() : url;

            final GraphQLConfigEndpoint endpointConfig = new GraphQLConfigEndpoint(null, name, url);

            if (supportsHeaders) {
                final Stream<JsonProperty> jsonPropertyStream = PsiTreeUtil.getChildrenOfTypeAsList(urlJsonProperty.getParent(), JsonProperty.class).stream();
                final Optional<JsonProperty> headers = jsonPropertyStream.filter(p -> "headers".equals(p.getName())).findFirst();
                headers.ifPresent(headersProp -> {
                    final JsonValue jsonValue = headersProp.getValue();
                    if (jsonValue != null) {
                        endpointConfig.headers = new Gson().<Map<String, Object>>fromJson(jsonValue.getText(), Map.class);
                    }
                });
            }


            return new GraphQLConfigVariableAwareEndpoint(endpointConfig, urlJsonProperty.getProject(), configFile);

        } catch (Exception e) {
            Notifications.Bus.notify(new Notification("GraphQL", "GraphQL Configuration Error", e.getMessage(), NotificationType.ERROR), urlJsonProperty.getProject());
        }
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements,
                                       @NotNull Collection<? super LineMarkerInfo<?>> result) {
        // compatibility with 182
    }
}
