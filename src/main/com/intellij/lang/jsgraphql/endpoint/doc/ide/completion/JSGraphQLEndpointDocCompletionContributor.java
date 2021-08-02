/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.doc.ide.completion;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.intellij.codeInsight.completion.AddSpaceInsertHandler;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.jsgraphql.endpoint.doc.JSGraphQLEndpointDocTokenTypes;
import com.intellij.lang.jsgraphql.endpoint.doc.psi.JSGraphQLEndpointDocFile;
import com.intellij.lang.jsgraphql.endpoint.doc.psi.JSGraphQLEndpointDocPsiUtil;
import com.intellij.lang.jsgraphql.endpoint.doc.psi.JSGraphQLEndpointDocTag;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointFieldDefinition;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointInputValueDefinition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;


public class JSGraphQLEndpointDocCompletionContributor extends CompletionContributor {

	public JSGraphQLEndpointDocCompletionContributor() {

		CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
			@SuppressWarnings("unchecked")
			@Override
			protected void addCompletions(@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {

				final PsiFile file = parameters.getOriginalFile();

				if (!(file instanceof JSGraphQLEndpointDocFile)) {
					return;
				}

				final PsiElement completionElement = Optional.ofNullable(parameters.getOriginalPosition()).orElse(parameters.getPosition());
				if (completionElement != null) {
					final PsiComment comment = PsiTreeUtil.getContextOfType(completionElement, PsiComment.class);
					if (comment != null && JSGraphQLEndpointDocPsiUtil.isDocumentationComment(comment)) {

						if (completionElement.getNode().getElementType() == JSGraphQLEndpointDocTokenTypes.DOCVALUE) {
							final JSGraphQLEndpointFieldDefinition fieldDefinition = PsiTreeUtil.getNextSiblingOfType(comment, JSGraphQLEndpointFieldDefinition.class);
							if (fieldDefinition != null && fieldDefinition.getArgumentsDefinition() != null) {
								final List<String> otherDocTagValues = JSGraphQLEndpointDocPsiUtil.getOtherDocTagValues(comment);
								for (JSGraphQLEndpointInputValueDefinition arg : PsiTreeUtil.findChildrenOfType(fieldDefinition.getArgumentsDefinition(), JSGraphQLEndpointInputValueDefinition.class)) {
									final String argName = arg.getInputValueDefinitionIdentifier().getText();
									if (!otherDocTagValues.contains(argName)) {
										result.addElement(LookupElementBuilder.create(argName).withInsertHandler(AddSpaceInsertHandler.INSTANCE));
									}
								}
							}
							return;
						}

						final JSGraphQLEndpointDocTag tagBefore = PsiTreeUtil.getPrevSiblingOfType(completionElement, JSGraphQLEndpointDocTag.class);
						final JSGraphQLEndpointDocTag tagParent = PsiTreeUtil.getParentOfType(completionElement, JSGraphQLEndpointDocTag.class);
						if (tagBefore == null || tagParent != null) {
							String completion = "param";
							final boolean includeAt = completionElement.getNode().getElementType() != JSGraphQLEndpointDocTokenTypes.DOCNAME;
							if (includeAt) {
								completion = "@" + completion;
							}
							result.addElement(LookupElementBuilder.create(completion).withInsertHandler(AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP));
						}
					}
				}
			}
		};

		extend(CompletionType.BASIC, PlatformPatterns.psiElement(), provider);

	}

}
