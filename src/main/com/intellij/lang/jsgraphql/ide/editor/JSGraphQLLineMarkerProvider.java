/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.editor;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.ide.BrowserUtil;
import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons;
import com.intellij.lang.jsgraphql.ide.injection.JSGraphQLLanguageInjectionUtil;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

import static com.intellij.lang.jsgraphql.ide.injection.JSGraphQLLanguageInjectionUtil.*;

public class JSGraphQLLineMarkerProvider implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        final Ref<String> envRef  = new Ref<>();
        if(JSGraphQLLanguageInjectionUtil.isJSGraphQLLanguageInjectionTarget(element, envRef)) {
            return createLineMarkerInfo(element, envRef);
        }
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {

    }

    private LineMarkerInfo createLineMarkerInfo(PsiElement element, Ref<String> envRef) {
        final Icon icon;
        final String tooltip;
        final String url;
        boolean configureGQL = false;
        final String environment = envRef.get();
        if(RELAY_ENVIRONMENT.equals(environment)) {
            icon = JSGraphQLIcons.Logos.Relay;
            tooltip = "Relay GraphQL Template";
            url = "https://facebook.github.io/relay/docs/api-reference-relay-ql.html";
        } else if(GRAPHQL_ENVIRONMENT.equals(environment) || GRAPHQL_TEMPLATE_ENVIRONMENT.equals(environment)) {
            icon = JSGraphQLIcons.Logos.GraphQL;
            tooltip = "GraphQL";
            url = "http://graphql.org/";
        } else if(APOLLO_ENVIRONMENT.equals(environment)) {
            icon = JSGraphQLIcons.Logos.Apollo;
            tooltip = "Apollo Client GraphQL Template";
            url = "http://docs.apollostack.com/apollo-client/core.html";
            configureGQL = true;
        } else if(LOKKA_ENVIRONMENT.equals(environment)) {
            icon = JSGraphQLIcons.Logos.Lokka;
            tooltip = "Lokka GraphQL Template";
            url = "https://github.com/kadirahq/lokka";
            configureGQL = true;
        } else {
            return null;
        }
        if(configureGQL && !JSGraphQLLanguageInjectionUtil.isGQLEnvironmentConfigured(element.getProject())) {
            EditorNotifications.getInstance(element.getProject()).updateNotifications(element.getContainingFile().getVirtualFile());
        }
        return new LineMarkerInfo<>(element, element.getTextRange(), icon, Pass.UPDATE_ALL, o -> tooltip, (e, elt) -> {
            BrowserUtil.browse(url);
        }, GutterIconRenderer.Alignment.CENTER);
    }
}
