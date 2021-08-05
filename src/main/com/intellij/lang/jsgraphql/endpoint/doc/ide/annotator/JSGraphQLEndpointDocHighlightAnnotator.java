/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.doc.ide.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.jsgraphql.endpoint.doc.JSGraphQLEndpointDocTokenTypes;
import com.intellij.lang.jsgraphql.endpoint.doc.psi.JSGraphQLEndpointDocPsiUtil;
import com.intellij.lang.jsgraphql.endpoint.doc.psi.JSGraphQLEndpointDocTag;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointFieldDefinition;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointInputValueDefinition;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointInputValueDefinitions;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

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

    private static final Key<PsiElement> TODO_ELEMENT = Key.create("JSGraphQLEndpointDocHighlightAnnotator.Todo");

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {

		final IElementType elementType = element.getNode().getElementType();

        // highlight TO-DO items
        if(elementType == JSGraphQLEndpointDocTokenTypes.DOCTEXT) {
			final String elementText = element.getText().toLowerCase();
			if(isTodoToken(elementText)) {
				setTextAttributes(holder, CodeInsightColors.TODO_DEFAULT_ATTRIBUTES);
                holder.getCurrentAnnotationSession().putUserData(TODO_ELEMENT, element);
				return;
			} else {
                PsiElement prevSibling = element.getPrevSibling();
                while (prevSibling != null) {
                    if(prevSibling == holder.getCurrentAnnotationSession().getUserData(TODO_ELEMENT)) {
                        setTextAttributes(holder, CodeInsightColors.TODO_DEFAULT_ATTRIBUTES);
                        return;
                    }
                    prevSibling = prevSibling.getPrevSibling();
                }
            }
		}

		final PsiComment comment = PsiTreeUtil.getContextOfType(element, PsiComment.class);
		if (JSGraphQLEndpointDocPsiUtil.isDocumentationComment(comment)) {
			final TextAttributesKey textAttributesKey = ATTRIBUTES.get(elementType);
			if (textAttributesKey != null) {
				setTextAttributes(holder, textAttributesKey);
			}
			// highlight invalid argument names after @param
			if(elementType == JSGraphQLEndpointDocTokenTypes.DOCVALUE) {
				final JSGraphQLEndpointFieldDefinition field = PsiTreeUtil.getNextSiblingOfType(comment, JSGraphQLEndpointFieldDefinition.class);
				if(field != null) {
					final JSGraphQLEndpointDocTag tag = PsiTreeUtil.getParentOfType(element, JSGraphQLEndpointDocTag.class);
					if(tag != null && tag.getDocName().getText().equals("@param")) {
						final String paramName = element.getText();
						final JSGraphQLEndpointInputValueDefinitions arguments = PsiTreeUtil.findChildOfType(field, JSGraphQLEndpointInputValueDefinitions.class);
						if(arguments == null) {
                            // no arguments so invalid use of @param
                            holder.newAnnotation(HighlightSeverity.ERROR, "Invalid use of @param. The property has no arguments")
                                .range(element).create();
						} else {
							final JSGraphQLEndpointInputValueDefinition[] inputValues = PsiTreeUtil.getChildrenOfType(arguments, JSGraphQLEndpointInputValueDefinition.class);
							boolean found = false;
							if(inputValues != null) {
								for (JSGraphQLEndpointInputValueDefinition inputValue: inputValues) {
									if(inputValue.getInputValueDefinitionIdentifier().getText().equals(paramName)) {
										found = true;
										break;
									}
								}
							}
                            if (!found) {
                                holder.newAnnotation(
                                    HighlightSeverity.ERROR,
                                    "@param name '" + element.getText() + "' doesn't match any of the field arguments"
                                ).range(element).create();
                            }

						}
					}
				}
			}
		}
	}

	private boolean isTodoToken(String elementText) {
		if(elementText.startsWith("todo")) {
            return elementText.length() == 4 || !Character.isAlphabetic(elementText.charAt(4));
        }
		return false;
	}

	private void setTextAttributes(AnnotationHolder holder, TextAttributesKey key) {
		holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(key).create();
	}
}
