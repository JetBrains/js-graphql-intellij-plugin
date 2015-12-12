/**
 *  Copyright (c) 2015, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.languageservice;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.intellij.lang.jsgraphql.languageservice.api.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class JSGraphQLNodeLanguageServiceClient {

    private static final Logger log = Logger.getInstance(JSGraphQLNodeLanguageServiceClient.class);
    private static Map<Project, JSGraphQLNodeLanguageServiceInstance> languageServiceInstances = Maps.newConcurrentMap();

    public static TokensResponse getTokens(String buffer, Project project) {
        final BufferRequest request = BufferRequest.getTokens(buffer);
        return executeRequest(request, TokensResponse.class, project);
    }

    public static HintsResponse getHints(String buffer, int line, int ch, Project project, boolean relay) {
        final BufferRequest request = BufferRequest.getHints(buffer, line, ch, relay);
        return executeRequest(request, HintsResponse.class, project);
    }

    public static TokenDocumentationResponse getTokenDocumentation(String buffer, int line, int ch, Project project, boolean relay) {
        final BufferRequest request = BufferRequest.getTokenDocumentation(buffer, line, ch, relay);
        return executeRequest(request, TokenDocumentationResponse.class, project);
    }

    public static TypeDocumentationResponse getTypeDocumentation(String type, Project project) {
        final DocumentationRequest request = DocumentationRequest.getTypeDocumentation(type);
        return executeRequest(request, TypeDocumentationResponse.class, project);
    }

    public static AnnotationsResponse getAnnotations(String buffer, Project project, boolean relay) {
        final BufferRequest request = BufferRequest.getAnnotations(buffer, relay);
        return executeRequest(request, AnnotationsResponse.class, project);
    }

    public static ASTResponse getAST(String buffer, Project project, boolean relay) {
        final BufferRequest request = BufferRequest.getAST(buffer, relay);
        return executeRequest(request, ASTResponse.class, project);
    }

    private static <R> R executeRequest(Request request, Class<R> responseClass, @NotNull Project project) {
        return executeRequest(request, responseClass, project, true);
    }

    private static <R> R executeRequest(Request request, Class<R> responseClass, @NotNull Project project, boolean setProjectDir) {

        URL url = getJSGraphQLNodeLanguageServiceInstance(project, setProjectDir);
        if(url == null) {
            return null;
        }
        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection)url.openConnection();
            httpConnection.setConnectTimeout(50);
            httpConnection.setReadTimeout(1000);
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);
            httpConnection.setRequestProperty("Content-Type", "application/json");
            httpConnection.connect();
            try(OutputStreamWriter writer = new OutputStreamWriter(httpConnection.getOutputStream())) {
                final String jsonRequest = new Gson().toJson(request);
                writer.write(jsonRequest);
                writer.flush();
                writer.close();
            }
            if(httpConnection.getResponseCode() == 200) {
                if(responseClass == null) {
                    return null;
                }
                try(InputStream inputStream = httpConnection.getInputStream()) {
                    String jsonResponse = IOUtils.toString(inputStream, "UTF-8");
                    R response = new Gson().fromJson(jsonResponse, responseClass);
                    return response;
                }
            } else {
                log.warn("Got error from JS GraphQL Language Service: HTTP " + httpConnection.getResponseCode() + ": " + httpConnection.getResponseMessage());
            }
        } catch (IOException e) {
            log.warn("Unable to connect to dev server", e);
        } finally {
            if(httpConnection != null) {
                httpConnection.disconnect();
            }
        }
        return null;
    }

    public static JSGraphQLNodeLanguageServiceInstance getLanguageServiceInstance(Project project) {
        return languageServiceInstances.get(project);
    }

    private static URL getJSGraphQLNodeLanguageServiceInstance(@NotNull Project project, boolean setProjectDir) {

        final JSGraphQLNodeLanguageServiceInstance instance = languageServiceInstances.computeIfAbsent(project, JSGraphQLNodeLanguageServiceInstance::new);

        final String projectBasePath = project.getBasePath();
        if(instance.getSchemaProjectDir() == null && setProjectDir && projectBasePath != null) {
            executeRequest(new SetProjectDirRequest(projectBasePath), null, project, false);
            instance.setSchemaProjectDir(projectBasePath);
        }

        return instance.getUrl();

    }

    static void onProjectClosing(JSGraphQLNodeLanguageServiceInstance instance) {
        languageServiceInstances.remove(instance.getProject());
    }


    public static void onInstanceRestarted(@NotNull JSGraphQLNodeLanguageServiceInstance instance) {
        final String projectDir = instance.getProject().getBasePath();
        if(projectDir != null) {
            executeRequest(new SetProjectDirRequest(projectDir), null, instance.getProject(), false);
        }
    }
}
