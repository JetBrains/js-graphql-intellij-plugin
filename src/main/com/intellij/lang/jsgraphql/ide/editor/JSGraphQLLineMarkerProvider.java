/**
 *  Copyright (c) 2015, Jim Kynde Meyer
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

public class JSGraphQLLineMarkerProvider implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        Ref<JSGraphQLLanguageInjectionUtil.JSGraphQLInjectionTag> tagRef  = new Ref<>();
        if(JSGraphQLLanguageInjectionUtil.isJSGraphQLLanguageInjectionTarget(element, tagRef)) {
            return createLineMarkerInfo(element, tagRef);
        }
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {

    }

    private LineMarkerInfo createLineMarkerInfo(PsiElement element, Ref<JSGraphQLLanguageInjectionUtil.JSGraphQLInjectionTag> tagRef) {
        final Icon icon = tagRef.get() == JSGraphQLLanguageInjectionUtil.JSGraphQLInjectionTag.RelayQL ? JSGraphQLIcons.Logos.Relay : JSGraphQLIcons.Logos.GraphQL;
        return new LineMarkerInfo<>(element, element.getTextRange(), icon, Pass.UPDATE_ALL, o -> "Relay GraphQL Template", (e, elt) -> {
            BrowserUtil.browse("https://facebook.github.io/relay/docs/api-reference-relay-ql.html");
        }, GutterIconRenderer.Alignment.CENTER);
    }
}
