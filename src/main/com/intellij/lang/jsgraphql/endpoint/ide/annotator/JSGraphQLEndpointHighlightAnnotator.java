/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.annotator;

import com.intellij.lang.jsgraphql.endpoint.ide.highlighting.JSGraphQLEndpointSyntaxHighlighter;
import org.jetbrains.annotations.NotNull;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointNamedType;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointNamedTypeDef;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointProperty;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;

public class JSGraphQLEndpointHighlightAnnotator implements Annotator {
	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		if(element instanceof JSGraphQLEndpointNamedTypeDef) {
			setTextAttributes(element, holder, JSGraphQLEndpointSyntaxHighlighter.DEF);
		} else if (element instanceof JSGraphQLEndpointProperty) {
			setTextAttributes(element, holder, JSGraphQLEndpointSyntaxHighlighter.PROPERTY);
		} else if (element instanceof JSGraphQLEndpointNamedType) {
			setTextAttributes(element, holder, JSGraphQLEndpointSyntaxHighlighter.ATOM);
		}
	}

	private void setTextAttributes(PsiElement element, AnnotationHolder holder, TextAttributesKey key) {
		final Annotation annotation = holder.createInfoAnnotation(element, null);
		annotation.setTextAttributes(key);
	}
}
