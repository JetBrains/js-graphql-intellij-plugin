/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.completion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.completion.AddSpaceInsertHandler;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.jsgraphql.JSGraphQLScalars;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointFileType;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypesSets;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointAnnotation;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointAnnotationArguments;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointEnumTypeDefinition;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointFieldDefinition;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointFieldDefinitionSet;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointFile;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointImportFileReference;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointInputObjectTypeDefinition;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointInputValueDefinitions;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointInterfaceTypeDefinition;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointObjectTypeDefinition;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointOperationTypeDefinitionSet;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointProperty;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointPsiUtil;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointQuotedString;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointUnionTypeDefinition;
import com.intellij.lang.jsgraphql.endpoint.psi.impl.JSGraphQLEndpointImplementsInterfacesImpl;
import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons;
import com.intellij.lang.jsgraphql.ide.configuration.JSGraphQLConfigurationProvider;
import com.intellij.lang.jsgraphql.ide.configuration.JSGraphQLSchemaEndpointAnnotation;
import com.intellij.lang.jsgraphql.ide.configuration.JSGraphQLSchemaEndpointAnnotationArgument;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;


public class JSGraphQLEndpointCompletionContributor extends CompletionContributor {

	private static final ArrayList<String> TOP_LEVEL_KEYWORDS = Lists.newArrayList(
			JSGraphQLEndpointTokenTypes.IMPORT.toString(),
			JSGraphQLEndpointTokenTypes.TYPE.toString(),
			JSGraphQLEndpointTokenTypes.INTERFACE.toString(),
			JSGraphQLEndpointTokenTypes.INPUT.toString(),
			JSGraphQLEndpointTokenTypes.UNION.toString(),
			JSGraphQLEndpointTokenTypes.ENUM.toString(),
			JSGraphQLEndpointTokenTypes.SCHEMA.toString()
	);

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
					final PsiElement leafBeforeCompletion = PsiTreeUtil.prevVisibleLeaf(completionElement);

					// 1. complete on interface name after IMPLEMENTS token
					if (completeImplementableInterface(result, completionElement, leafBeforeCompletion)) {
						return;
					}

					// 2. import file
					if (completeImportFile(result, file, parent)) {
						return;
					}

					// 3.A. top level completions, e.g. keywords
					if (completeKeywords(result, completionElement, leafBeforeCompletion, parent)) {
						return;
					}

					// 3.B. implements when type definition surrounds completion element (e.g. when it has a FieldDefinitionSet)
					if (completeImplementsInsideTypeDefinition(result, completionElement, parent)) {
						return;
					}

					// 4. completions inside FieldDefinitionSet
					final JSGraphQLEndpointFieldDefinitionSet fieldDefinitionSet = PsiTreeUtil.getParentOfType(completionElement, JSGraphQLEndpointFieldDefinitionSet.class);
					if (fieldDefinitionSet != null) {

						// 4.A. field/argument type completion
						if (completeFieldOrArgumentType(result, completionElement)) {
							return;
						}

						// 4.B. annotations
						if (completeAnnotations(result, file, completionElement)) {
							return;
						}

						// 4.C. annotation arguments
						if (completeAnnotationArguments(result, file, completionElement, leafBeforeCompletion)) {
							return;
						}

						// 4.D. override for interface fields
						if (completeOverrideFields(fieldDefinitionSet, completionElement, result)) {
							return;
						}
					}

					// 5. completions inside SchemaDefinition
					if (completeInsideSchemaDefinition(result, completionElement, leafBeforeCompletion)) {
						return;
					}

				}

			}
		};

		extend(CompletionType.BASIC, PlatformPatterns.psiElement(), provider);

	}


	private boolean completeImplementableInterface(@NotNull CompletionResultSet result, PsiElement completionElement, PsiElement leafBeforeCompletion) {
		if (leafBeforeCompletion != null) {
			final TokenSet skipping = TokenSet.create(JSGraphQLEndpointTokenTypes.IDENTIFIER, JSGraphQLEndpointTokenTypes.COMMA);
			final PsiElement implementsBefore = findPreviousLeaf(completionElement, JSGraphQLEndpointTokenTypes.IMPLEMENTS, skipping);
			if (implementsBefore != null) {
				final Collection<String> availableInterfaceNames = getAvailableInterfaceNames(implementsBefore);
				for (String name : availableInterfaceNames) {
					LookupElementBuilder element = LookupElementBuilder.create(name);
					result.addElement(element);
				}
				return true;
			}
		}
		return false;
	}

	private boolean completeImplementsKeyword(@NotNull CompletionResultSet result, PsiElement completionElement) {
		final PsiElement typeBefore = findPreviousLeaf(completionElement, JSGraphQLEndpointTokenTypes.TYPE, TokenSet.create(JSGraphQLEndpointTokenTypes.IDENTIFIER));
		if (typeBefore != null) {
			LookupElementBuilder element = LookupElementBuilder.create(JSGraphQLEndpointTokenTypes.IMPLEMENTS.toString())
					.withBoldness(true)
					.withInsertHandler(AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
			result.addElement(element);
			return true;
		}
		return false;
	}

	private boolean completeImportFile(@NotNull CompletionResultSet result, PsiFile file, PsiElement parent) {
		if (parent instanceof JSGraphQLEndpointQuotedString && parent.getParent() instanceof JSGraphQLEndpointImportFileReference) {

			final VirtualFile entryFile = JSGraphQLConfigurationProvider.getService(file.getProject()).getEndpointEntryFile();
			final VirtualFile entryFileDir = entryFile != null ? entryFile.getParent() : null;
			final GlobalSearchScope scope;
			if(entryFile != null) {
				scope = GlobalSearchScopesCore.directoriesScope(file.getProject(), true,  entryFile.getParent());
			} else {
				scope = GlobalSearchScope.projectScope(file.getProject());
			}
			final Collection<VirtualFile> files = FileTypeIndex.getFiles(JSGraphQLEndpointFileType.INSTANCE, scope);
			for (VirtualFile virtualFile : files) {
				if(virtualFile.equals(entryFile)) {
					// entry file should never be imported
					continue;
				}
				final PsiFile psiFile = PsiManager.getInstance(file.getProject()).findFile(virtualFile);
				if (psiFile != null) {
					if(psiFile.equals(file)) {
						// don't suggest the current file
						continue;
					}
					String name = StringUtils.substringBeforeLast(psiFile.getName(), ".");
					if(entryFileDir != null) {
						VirtualFile parentDir = virtualFile.getParent();
						while(parentDir != null && !parentDir.equals(entryFileDir)) {
							name = parentDir.getName() + '/' + name;
							parentDir = parentDir.getParent();
						}
					}
					result.addElement(LookupElementBuilder.create(name).withIcon(psiFile.getIcon(0)));
				}
			}
			return true;
		}
		return false;
	}

	private boolean completeKeywords(@NotNull CompletionResultSet result, PsiElement completionElement, PsiElement leafBeforeCompletion, PsiElement parent) {
		if (parent instanceof JSGraphQLEndpointFile || isTopLevelError(parent)) {

			if (isKeyword(leafBeforeCompletion)) {
				// no keyword suggestions right after another keyword
				return true;
			}

			// implements after TYPE NamedType
			if(completeImplementsKeyword(result, completionElement)) {
				return true;
			}

			for (String keyword : TOP_LEVEL_KEYWORDS) {
				LookupElementBuilder element = LookupElementBuilder.create(keyword).withBoldness(true);
				if(keyword.equals(JSGraphQLEndpointTokenTypes.IMPORT.toString())) {
					element = element.withInsertHandler(JSGraphQLEndpointImportInsertHandler.INSTANCE_WITH_AUTO_POPUP);
				} else {
					element = element.withInsertHandler(AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
				}
				result.addElement(element);
			}

			return true;

		}
		return false;
	}

	private boolean completeImplementsInsideTypeDefinition(@NotNull CompletionResultSet result, PsiElement completionElement, PsiElement parent) {
		if(parent instanceof JSGraphQLEndpointObjectTypeDefinition) {
			// implements after TYPE NamedType
			if(completeImplementsKeyword(result, completionElement)) {
				return true;
			}
		}
		return false;
	}


	private boolean completeFieldOrArgumentType(@NotNull CompletionResultSet result, PsiElement completionElement) {
		final TokenSet skipping = TokenSet.create(JSGraphQLEndpointTokenTypes.LBRACKET);
		final PsiElement colonBefore = findPreviousLeaf(completionElement, JSGraphQLEndpointTokenTypes.COLON, skipping);
		if (colonBefore != null) {
			for (String scalarType : JSGraphQLScalars.SCALAR_TYPES) {
				LookupElementBuilder element = LookupElementBuilder.create(scalarType).withIcon(JSGraphQLIcons.Schema.Scalar);
				result.addElement(element);
			}
			for (String scalarType : getKnownEnumTypeNames(completionElement)) {
				LookupElementBuilder element = LookupElementBuilder.create(scalarType).withIcon(JSGraphQLIcons.Schema.Enum);
				result.addElement(element);
			}
			final boolean isInsideInputValueDefinitions = PsiTreeUtil.getParentOfType(colonBefore, JSGraphQLEndpointInputValueDefinitions.class) != null;
			final JSGraphQLEndpointInputObjectTypeDefinition inputObjectTypeDefinition = PsiTreeUtil.getParentOfType(colonBefore, JSGraphQLEndpointInputObjectTypeDefinition.class);
			final boolean isInsideInputType = inputObjectTypeDefinition != null;
			if (isInsideInputValueDefinitions || isInsideInputType) {
				// argument (input) or input type definition
				final Collection<String> knownInputTypeNames = getKnownInputTypeNames(completionElement);
				if(isInsideInputType) {
					// input types can only refer to other input types: http://facebook.github.io/graphql/#sec-Input-Objects
					if(inputObjectTypeDefinition.getNamedTypeDef() != null) {
						knownInputTypeNames.removeIf(name -> name.equals(inputObjectTypeDefinition.getNamedTypeDef().getText()));
					}
				}
				for (String name : knownInputTypeNames) {
					LookupElementBuilder element = LookupElementBuilder.create(name).withIcon(JSGraphQLIcons.Schema.Type);
					result.addElement(element);
				}
			} else {
				// field return type (type, interface, union)
				for (String name : getAvailableInterfaceNames(completionElement)) {
					LookupElementBuilder element = LookupElementBuilder.create(name).withIcon(JSGraphQLIcons.Schema.Interface);
					result.addElement(element);
				}
				for (String name : getKnownTypeNames(completionElement)) {
					LookupElementBuilder element = LookupElementBuilder.create(name).withIcon(JSGraphQLIcons.Schema.Type);
					result.addElement(element);
				}
				for (String name : getKnownUnionTypeNames(completionElement)) {
					LookupElementBuilder element = LookupElementBuilder.create(name).withIcon(JSGraphQLIcons.Schema.Type);
					result.addElement(element);
				}
			}
			return true;
		}
		return false;
	}

	private boolean completeAnnotations(@NotNull CompletionResultSet result, PsiFile file, PsiElement completionElement) {
		final JSGraphQLEndpointFieldDefinition field = PsiTreeUtil.getNextSiblingOfType(completionElement, JSGraphQLEndpointFieldDefinition.class);
		final JSGraphQLEndpointProperty property = PsiTreeUtil.getNextSiblingOfType(completionElement, JSGraphQLEndpointProperty.class);
		final JSGraphQLEndpointAnnotation nextAnnotation = PsiTreeUtil.getNextSiblingOfType(completionElement, JSGraphQLEndpointAnnotation.class);
		final boolean afterAtAnnotation = completionElement.getNode().getElementType() == JSGraphQLEndpointTokenTypes.AT_ANNOTATION;
		if (afterAtAnnotation || field != null || nextAnnotation != null || property != null) {
			final JSGraphQLConfigurationProvider configurationProvider = JSGraphQLConfigurationProvider.getService(file.getProject());
			for (JSGraphQLSchemaEndpointAnnotation endpointAnnotation : configurationProvider.getEndpointAnnotations()) {
				String completion = endpointAnnotation.name;
				if (!afterAtAnnotation) {
					completion = "@" + completion;
				}
				LookupElementBuilder element = LookupElementBuilder.create(completion).withIcon(JSGraphQLIcons.Schema.Attribute);
				if(endpointAnnotation.arguments != null && endpointAnnotation.arguments.size() > 0) {
					element = element.withInsertHandler(ParenthesesInsertHandler.WITH_PARAMETERS);
				}
				result.addElement(element);
			}
			return true;
		}
		return false;
	}

	private boolean completeAnnotationArguments(@NotNull CompletionResultSet result, PsiFile file, PsiElement completionElement, PsiElement leafBeforeCompletion) {
		final JSGraphQLEndpointAnnotationArguments annotationArguments = PsiTreeUtil.getParentOfType(completionElement, JSGraphQLEndpointAnnotationArguments.class);
		if (annotationArguments != null && leafBeforeCompletion != null) {
			final JSGraphQLEndpointAnnotation annotation = PsiTreeUtil.getParentOfType(annotationArguments, JSGraphQLEndpointAnnotation.class);
			if(annotation != null) {
				final JSGraphQLConfigurationProvider configurationProvider = JSGraphQLConfigurationProvider.getService(file.getProject());
				for (JSGraphQLSchemaEndpointAnnotation endpointAnnotation : configurationProvider.getEndpointAnnotations()) {
					if (annotation.getAtAnnotation().getText().equals("@" + endpointAnnotation.name)) {
						final IElementType elementType = leafBeforeCompletion.getNode().getElementType();
						if(elementType == JSGraphQLEndpointTokenTypes.LPAREN || elementType == JSGraphQLEndpointTokenTypes.COMMA) {
							// completion on argument name
							if(endpointAnnotation.arguments != null) {
								for (JSGraphQLSchemaEndpointAnnotationArgument argument : endpointAnnotation.arguments) {
									LookupElementBuilder element = LookupElementBuilder.create(argument.name + " = ").withTypeText(" " + argument.type, true);
									element = element.withPresentableText(argument.name + "");
									result.addElement(element);
								}
							}
						} else if(elementType == JSGraphQLEndpointTokenTypes.EQUALS) {
							// completion on argument value
							if(endpointAnnotation.arguments != null) {
								final PsiElement annotationName = PsiTreeUtil.prevVisibleLeaf(leafBeforeCompletion);
								if(annotationName != null) {
									for (JSGraphQLSchemaEndpointAnnotationArgument argument : endpointAnnotation.arguments) {
										if(annotationName.getText().equals(argument.name)) {
											if("Boolean".equals(argument.type)) {
												result.addElement(LookupElementBuilder.create("true").withBoldness(true));
												result.addElement(LookupElementBuilder.create("false").withBoldness(true));
											}
											return true;
										}
									}
								}
							}
						}
						return true;
					}
				}
			}
			return true;
		}
		return false;
	}

	private boolean completeOverrideFields(JSGraphQLEndpointFieldDefinitionSet fieldDefinitionSet, PsiElement completionElement, CompletionResultSet result) {
		if(PsiTreeUtil.getParentOfType(completionElement, JSGraphQLEndpointAnnotation.class) != null) {
			return false;
		}
		final JSGraphQLEndpointObjectTypeDefinition typeDefinition = PsiTreeUtil.getParentOfType(fieldDefinitionSet, JSGraphQLEndpointObjectTypeDefinition.class);
		if (typeDefinition != null) {
			if (typeDefinition.getImplementsInterfaces() != null) {
				final Set<String> implementsNames = typeDefinition.getImplementsInterfaces().getNamedTypeList().stream().map(t -> t.getIdentifier().getText()).collect(Collectors.toSet());
				// TODO: Find available interfaces in imported files
				final Collection<JSGraphQLEndpointInterfaceTypeDefinition> interfaceTypeDefinitions = PsiTreeUtil.findChildrenOfType(
						fieldDefinitionSet.getContainingFile(), JSGraphQLEndpointInterfaceTypeDefinition.class
				);
				final Set<String> currentFieldNames = fieldDefinitionSet.getFieldDefinitionList().stream().map(f -> f.getProperty().getText()).collect(Collectors.toSet());
				for (JSGraphQLEndpointInterfaceTypeDefinition interfaceTypeDefinition : interfaceTypeDefinitions) {
					if (interfaceTypeDefinition.getNamedTypeDef() != null) {
						if (implementsNames.contains(interfaceTypeDefinition.getNamedTypeDef().getText())) {
							if (interfaceTypeDefinition.getFieldDefinitionSet() != null) {
								for (JSGraphQLEndpointFieldDefinition field : interfaceTypeDefinition.getFieldDefinitionSet().getFieldDefinitionList()) {
									if (!currentFieldNames.contains(field.getProperty().getText())) {
										LookupElementBuilder element = LookupElementBuilder.create(field.getText().trim());
										element = element.withTypeText(" " + interfaceTypeDefinition.getNamedTypeDef().getText(), true);
										result.addElement(element);
									}
								}
							}
						}
					}
				}
				return true;
			}
		}
		return false;
	}

	private boolean completeInsideSchemaDefinition(@NotNull CompletionResultSet result, PsiElement completionElement, PsiElement leafBeforeCompletion) {
		final JSGraphQLEndpointOperationTypeDefinitionSet operationSet = PsiTreeUtil.getParentOfType(completionElement, JSGraphQLEndpointOperationTypeDefinitionSet.class);
		if (operationSet != null) {
			final TokenSet skipping = TokenSet.create(JSGraphQLEndpointTokenTypes.LBRACKET);
			final PsiElement colonBefore = findPreviousLeaf(completionElement, JSGraphQLEndpointTokenTypes.COLON, skipping);
			if (colonBefore != null) {
				for (String name : getKnownTypeNames(completionElement)) {
					LookupElementBuilder element = LookupElementBuilder.create(name).withIcon(JSGraphQLIcons.Schema.Type);
					result.addElement(element);
				}
			} else {
				if (isKeyword(leafBeforeCompletion)) {
					return true;
				}
				for (IElementType operationType : Lists.newArrayList(JSGraphQLEndpointTokenTypes.QUERY, JSGraphQLEndpointTokenTypes.MUTATION, JSGraphQLEndpointTokenTypes.SUBSCRIPTION)) {
					final String completion = operationType.toString();
					LookupElementBuilder element = LookupElementBuilder.create(completion + ":")
							.withPresentableText(completion)
							.withBoldness(true)
							.withInsertHandler(AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
					result.addElement(element);
				}
			}
			return true;
		}
		return false;
	}


	// ---- PSI file traversals ----

	private boolean isKeyword(PsiElement leafBeforeCompletion) {
		if (leafBeforeCompletion != null) {
			if (JSGraphQLEndpointTokenTypesSets.KEYWORDS.contains(leafBeforeCompletion.getNode().getElementType())) {
				return true;
			}
		}
		return false;
	}

	private Collection<String> getAvailableInterfaceNames(PsiElement implementsToken) {
		final Collection<String> available = getKnownInterfaceNames(implementsToken);
		final JSGraphQLEndpointImplementsInterfacesImpl implementsInterfaces = PsiTreeUtil.getParentOfType(implementsToken, JSGraphQLEndpointImplementsInterfacesImpl.class);
		if (implementsInterfaces != null) {
			final List<String> currentInterfaces = implementsInterfaces.getNamedTypeList().stream().map(type -> type.getIdentifier().getText()).collect(Collectors.toList());
			available.removeIf(currentInterfaces::contains);
		}
		return available;
	}

	private Collection<String> getKnownInterfaceNames(PsiElement element) {
		return JSGraphQLEndpointPsiUtil.getKnownDefinitionNames(element.getContainingFile(), JSGraphQLEndpointInterfaceTypeDefinition.class);
	}

	private Collection<String> getKnownTypeNames(PsiElement element) {
		return JSGraphQLEndpointPsiUtil.getKnownDefinitionNames(element.getContainingFile(), JSGraphQLEndpointObjectTypeDefinition.class);
	}

	private Collection<String> getKnownInputTypeNames(PsiElement element) {
		return JSGraphQLEndpointPsiUtil.getKnownDefinitionNames(element.getContainingFile(), JSGraphQLEndpointInputObjectTypeDefinition.class);
	}

	private Collection<String> getKnownEnumTypeNames(PsiElement element) {
		return JSGraphQLEndpointPsiUtil.getKnownDefinitionNames(element.getContainingFile(), JSGraphQLEndpointEnumTypeDefinition.class);
	}


	private Collection<String> getKnownUnionTypeNames(PsiElement element) {
		return JSGraphQLEndpointPsiUtil.getKnownDefinitionNames(element.getContainingFile(), JSGraphQLEndpointUnionTypeDefinition.class);
	}

	private PsiElement findPreviousLeaf(PsiElement start, IElementType typeToFind, TokenSet skipping) {
		PsiElement result = PsiTreeUtil.prevVisibleLeaf(start);
		while (result != null) {
			if (result instanceof PsiWhiteSpace || result instanceof PsiComment) {
				result = PsiTreeUtil.prevVisibleLeaf(result);
				continue;
			}
			if (result.getNode().getElementType() == typeToFind) {
				return result;
			}
			if (!skipping.contains(result.getNode().getElementType())) {
				// not allowed
				return null;
			}
			result = PsiTreeUtil.prevVisibleLeaf(result);
		}
		return null;
	}

	private boolean isTopLevelError(PsiElement element) {
		return (element instanceof PsiErrorElement) && (element.getParent() instanceof PsiFile);
	}


}