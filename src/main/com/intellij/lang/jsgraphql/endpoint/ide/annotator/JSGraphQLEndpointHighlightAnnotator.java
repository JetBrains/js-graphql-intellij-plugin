/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.jsgraphql.endpoint.ide.highlighting.JSGraphQLEndpointSyntaxHighlighter;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointNamedType;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointNamedTypeDef;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointProperty;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class JSGraphQLEndpointHighlightAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof JSGraphQLEndpointNamedTypeDef) {
            setTextAttributes(element, holder, JSGraphQLEndpointSyntaxHighlighter.DEF);
        } else if (element instanceof JSGraphQLEndpointProperty) {
            setTextAttributes(element, holder, JSGraphQLEndpointSyntaxHighlighter.PROPERTY);
        } else if (element instanceof JSGraphQLEndpointNamedType) {
            setTextAttributes(element, holder, JSGraphQLEndpointSyntaxHighlighter.ATOM);
        }
    }

    private void setTextAttributes(PsiElement element, AnnotationHolder holder, TextAttributesKey key) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element).textAttributes(key).create();
    }
}
