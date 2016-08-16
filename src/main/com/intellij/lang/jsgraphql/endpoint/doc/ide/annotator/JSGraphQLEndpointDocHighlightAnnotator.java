/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.doc.ide.annotator;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.jsgraphql.endpoint.doc.JSGraphQLEndpointDocTokenTypes;
import com.intellij.lang.jsgraphql.endpoint.doc.psi.JSGraphQLEndpointDocPsiUtil;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * Highlights GraphQL Endpoint documentation comments
 */
public class JSGraphQLEndpointDocHighlightAnnotator implements Annotator {

	private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<>();

	static {
		ATTRIBUTES.put(JSGraphQLEndpointDocTokenTypes.DOCTEXT, TextAttributesKey.find("JS.DOC_COMMENT"));
		ATTRIBUTES.put(JSGraphQLEndpointDocTokenTypes.DOCNAME, TextAttributesKey.find("JS.DOC_TAG"));
		ATTRIBUTES.put(JSGraphQLEndpointDocTokenTypes.DOCVALUE, DefaultLanguageHighlighterColors.DOC_COMMENT_TAG_VALUE);
	}


	@SuppressWarnings("unchecked")
	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
		final PsiComment comment = PsiTreeUtil.getContextOfType(element, PsiComment.class);
		if (comment != null && JSGraphQLEndpointDocPsiUtil.isDocumentationComment(comment)) {
			final IElementType elementType = element.getNode().getElementType();
			final TextAttributesKey textAttributesKey = ATTRIBUTES.get(elementType);
			if (textAttributesKey != null) {
				setTextAttributes(element, holder, textAttributesKey);
			}
		}
	}

	private void setTextAttributes(PsiElement element, AnnotationHolder holder, TextAttributesKey key) {
		final Annotation annotation = holder.createInfoAnnotation(element, null);
		annotation.setTextAttributes(key);
	}
}
