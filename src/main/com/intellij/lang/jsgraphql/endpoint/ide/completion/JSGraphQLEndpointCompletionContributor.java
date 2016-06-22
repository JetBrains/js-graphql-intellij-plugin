/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.completion;

import java.util.ArrayList;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.completion.AddSpaceInsertHandler;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.jsgraphql.JSGraphQLKeywords;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;


public class JSGraphQLEndpointCompletionContributor extends CompletionContributor {

	public JSGraphQLEndpointCompletionContributor() {

		CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
			@Override
			protected void addCompletions(@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {

				final PsiFile file = parameters.getOriginalFile();

				if (!(file instanceof JSGraphQLEndpointFile)) {
					return;
				}

				final PsiElement completionElement = Optional.ofNullable(parameters.getOriginalPosition()).orElse(parameters.getPosition());
				if (completionElement != null) {

					final PsiElement parent = completionElement.getParent();
					if (parent instanceof JSGraphQLEndpointFile) {
						// top level completions, e.g. keywords
						final ArrayList<String> keywords = Lists.newArrayList(
								"import",//JSGraphQLKeywords.IMPORT,
								JSGraphQLKeywords.TYPE,
								JSGraphQLKeywords.INTERFACE,
								JSGraphQLKeywords.INPUT,
								JSGraphQLKeywords.UNION,
								JSGraphQLKeywords.ENUM
						);
						for (String keyword : keywords) {
							LookupElementBuilder element = LookupElementBuilder.create(keyword)
									.withBoldness(true)
									.withInsertHandler(AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
							result.addElement(element);
						}
						return;
					}

//					final JSGraphQLObjectTypeDefinitionPsiElement typeDefinition = PsiTreeUtil.getParentOfType(completionElement, JSGraphQLObjectTypeDefinitionPsiElement.class);
//					if (typeDefinition != null) {
//
//						// implements right after type name
//						final PsiElement prevSibling = PsiTreeUtil.prevVisibleLeaf(completionElement);
//						if (prevSibling != null && prevSibling.getNode().getElementType() == JSGraphQLTokenTypes.DEF) {
//							LookupElementBuilder element = LookupElementBuilder.create("implements")
//									.withBoldness(true)
//									.withInsertHandler(AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
//							result.addElement(element);
//							return;
//						}
//
//						// interface name to implement after implements token and other interfaces
//						if (prevSibling != null) {
//							final JSGraphQLFieldDefinitionSetPsiElement fieldDefinitionSet = PsiTreeUtil.getChildOfType(typeDefinition, JSGraphQLFieldDefinitionSetPsiElement.class);
//							if (fieldDefinitionSet == null || fieldDefinitionSet.getNode().getStartOffset() > completionElement.getNode().getStartOffset()) {
//								// before fields
//								for (ASTNode astNode : typeDefinition.getNode().getChildren(TokenSet.create(JSGraphQLTokenTypes.KEYWORD))) {
//									if ("implements".equals(astNode.getText())) {
//										if (astNode.getStartOffset() < completionElement.getNode().getStartOffset()) {
//											// and after implements
//											final JSGraphQLInterfaceTypeDefinitionPsiElement[] interfaces = PsiTreeUtil.getChildrenOfType(file, JSGraphQLInterfaceTypeDefinitionPsiElement.class);
//											final Set<String> currentInterfaceNames = typeDefinition.getImplementedInterfaceNames();
//											for (JSGraphQLInterfaceTypeDefinitionPsiElement anInterface : interfaces) {
//												final JSGraphQLNamedTypePsiElement nameElement = anInterface.getNameElement();
//												if(nameElement != null) {
//													final String name = nameElement.getText();
//													if(!currentInterfaceNames.contains(name)) {
//														final LookupElementBuilder element = LookupElementBuilder.create(name);
//														result.addElement(element);
//													}
//												}
//											}
//											return;
//										}
//									}
//								}
//							}
//						}
//					}


				}

			}
		};

		extend(CompletionType.BASIC, PlatformPatterns.psiElement(), provider);

	}

}