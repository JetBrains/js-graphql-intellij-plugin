/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.doc.psi;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointFieldDefinition;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointNamedTypeDefinition;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;

public class JSGraphQLEndpointDocPsiUtil {

	/**
	 * Gets the values of other documentation tags that are immediate siblings
	 *
	 * @param comment the comment to use as a staring point
	 */
	public static List<String> getOtherDocTagValues(PsiComment comment) {


		final Project project = comment.getProject();
		final InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(project);

		final List<PsiComment> siblings = Lists.newArrayList();
		getDocumentationCommentSiblings(comment, siblings, PsiElement::getPrevSibling);
		getDocumentationCommentSiblings(comment, siblings, PsiElement::getNextSibling);

		final List<String> values = Lists.newArrayList();

		for (PsiComment sibling : siblings) {
			if (sibling instanceof PsiLanguageInjectionHost) {
				List<Pair<PsiElement, TextRange>> injected = injectedLanguageManager.getInjectedPsiFiles(sibling);
				if (injected != null) {
					for (Pair<PsiElement, TextRange> pair : injected) {
						PsiFile injectedFile = pair.first.getContainingFile();
						final JSGraphQLEndpointDocTag tag = PsiTreeUtil.findChildOfType(injectedFile, JSGraphQLEndpointDocTag.class);
						if (tag != null && tag.getDocValue() != null) {
							values.add(tag.getDocValue().getText());
						}
					}
				}
			}
		}
		return values;
	}

	private static void getDocumentationCommentSiblings(PsiComment comment, List<PsiComment> comments, Function<PsiElement, PsiElement> step) {
		PsiElement element = step.apply(comment);
		while (element != null) {
			if (element instanceof PsiWhiteSpace) {
				element = step.apply(element);
			} else if (isDocumentationComment(element)) {
				comments.add((PsiComment) element);
				element = step.apply(element);
			} else {
				break;
			}
		}
	}

	/**
	 * Gets whether the specified comment is considered documentation, i.e. that it's placed directly above a type or field definition
	 */
	public static boolean isDocumentationComment(PsiElement element) {
		if (element instanceof PsiComment) {
			PsiElement next = element.getNextSibling();
			while (next != null) {
				final boolean isWhiteSpace = next instanceof PsiWhiteSpace;
				if (next instanceof PsiComment || isWhiteSpace) {
					if (isWhiteSpace && StringUtils.countMatches(next.getText(), "\n") > 1) {
						// a blank line before the next element, so this comment is not directly above it
						break;
					}
					next = next.getNextSibling();
				} else {
					break;
				}
			}
			if (next instanceof JSGraphQLEndpointFieldDefinition || next instanceof JSGraphQLEndpointNamedTypeDefinition) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the PSI comment that starts the documentation for the specified element, or <code>null</code> if no documentation is available
	 */
	public static PsiComment getDocumentationStartElement(PsiElement element) {
		final PsiComment comment = PsiTreeUtil.getPrevSiblingOfType(element, PsiComment.class);
		if(isDocumentationComment(comment)) {
			final List<PsiComment> siblings = Lists.newArrayList(comment);
			getDocumentationCommentSiblings(comment, siblings, PsiElement::getPrevSibling);
			Collections.reverse(siblings);
			return siblings.get(0);
		}
		return null;
	}

	/**
	 * Gets the text of the continuous comments placed directly above the specified element
	 * @param element element whose previous siblings are enumerated and included if they're documentation comments
	 * @return the combined text of the documentation comments, preserving line breaks, or <code>null</code> if no documentation is available
	 */
	public static String getDocumentation(PsiElement element) {
		final PsiComment comment = PsiTreeUtil.getPrevSiblingOfType(element, PsiComment.class);
		final PsiElement previousElement =PsiTreeUtil.getPrevSiblingOfType(element, element.getClass());
		if(isDocumentationComment(comment)) {
			if(previousElement != null && previousElement.getTextOffset() > comment.getTextOffset()) {
				// the comment is for another element of same type so no docs for this element
				return null;
			}
			final List<PsiComment> siblings = Lists.newArrayList(comment);
			getDocumentationCommentSiblings(comment, siblings, PsiElement::getPrevSibling);
			Collections.reverse(siblings);
			return siblings.stream().map(c -> StringUtils.stripStart(c.getText(), "# ")).collect(Collectors.joining("\n"));
		}
		return null;
	}
}
