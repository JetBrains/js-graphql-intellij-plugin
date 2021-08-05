/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.completion;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointFileType;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypesSets;
import com.intellij.lang.jsgraphql.endpoint.psi.*;
import com.intellij.lang.jsgraphql.endpoint.psi.impl.JSGraphQLEndpointImplementsInterfacesImpl;
import com.intellij.lang.jsgraphql.icons.GraphQLIcons;
import com.intellij.lang.jsgraphql.v1.JSGraphQLScalars;
import com.intellij.lang.jsgraphql.v1.ide.configuration.JSGraphQLConfigurationProvider;
import com.intellij.lang.jsgraphql.v1.ide.configuration.JSGraphQLSchemaEndpointAnnotation;
import com.intellij.lang.jsgraphql.v1.ide.configuration.JSGraphQLSchemaEndpointAnnotationArgument;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;


public class JSGraphQLEndpointCompletionContributor extends CompletionContributor {

	private static final ArrayList<String> TOP_LEVEL_KEYWORDS = Lists.newArrayList(
			JSGraphQLEndpointTokenTypes.IMPORT.toString(),
			JSGraphQLEndpointTokenTypes.TYPE.toString(),
			JSGraphQLEndpointTokenTypes.INTERFACE.toString(),
			JSGraphQLEndpointTokenTypes.INPUT.toString(),
			JSGraphQLEndpointTokenTypes.UNION.toString(),
			JSGraphQLEndpointTokenTypes.ENUM.toString(),
			JSGraphQLEndpointTokenTypes.SCALAR.toString(),
			JSGraphQLEndpointTokenTypes.SCHEMA.toString(),
			JSGraphQLEndpointTokenTypes.ANNOTATION_DEF.toString()
	);

	public JSGraphQLEndpointCompletionContributor() {

		CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
			@Override
			protected void addCompletions(@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {

				final PsiFile file = parameters.getOriginalFile();

				if (!(file instanceof JSGraphQLEndpointFile)) {
					return;
				}

				final boolean autoImport = parameters.isExtendedCompletion() || parameters.getCompletionType() == CompletionType.SMART;

				final PsiElement completionElement = Optional.ofNullable(parameters.getOriginalPosition()).orElse(parameters.getPosition());
				if (completionElement != null) {

					final PsiElement parent = completionElement.getParent();
					final PsiElement leafBeforeCompletion = PsiTreeUtil.prevVisibleLeaf(completionElement);

					// 1. complete on interface name after IMPLEMENTS token
					if (completeImplementableInterface(result, autoImport, completionElement, leafBeforeCompletion)) {
						return;
					}

					// 2. import file
					if (completeImportFile(result, file, parent)) {
						return;
					}

					// 3.A. top level completions, e.g. keywords and definition annotations
					if (completeKeywordsAndDefinitionAnnotations(result, autoImport, completionElement, leafBeforeCompletion, parent)) {
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
						if (completeFieldOrArgumentType(result, autoImport, completionElement)) {
							return;
						}

						// 4.B. annotations
						if (completeAnnotations(result, autoImport, file, completionElement)) {
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
		extend(CompletionType.SMART, PlatformPatterns.psiElement(), provider);

	}

	private boolean completeImplementableInterface(@NotNull CompletionResultSet result, boolean autoImport, PsiElement completionElement, PsiElement leafBeforeCompletion) {
		if (leafBeforeCompletion != null) {
			final TokenSet skipping = TokenSet.create(JSGraphQLEndpointTokenTypes.IDENTIFIER, JSGraphQLEndpointTokenTypes.COMMA);
			final PsiElement implementsBefore = findPreviousLeaf(completionElement, JSGraphQLEndpointTokenTypes.IMPLEMENTS, skipping);
			if (implementsBefore != null) {
				final Collection<JSGraphQLEndpointTypeResult<JSGraphQLEndpointInterfaceTypeDefinition>> availableInterfaceNames = getAvailableInterfaceNames(implementsBefore, autoImport);
				for (JSGraphQLEndpointTypeResult typeResult : availableInterfaceNames) {
					LookupElementBuilder element = withAutoImport(LookupElementBuilder.create(typeResult.name), typeResult, autoImport);
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
		if ((parent instanceof JSGraphQLEndpointQuotedString || parent instanceof JSGraphQLEndpointString) && PsiTreeUtil.getParentOfType(parent, JSGraphQLEndpointImportFileReference.class) != null) {

			final Project project = file.getProject();
			final VirtualFile entryFile = JSGraphQLConfigurationProvider.getService(project).getEndpointEntryFile(file);
			final GlobalSearchScope scope = JSGraphQLEndpointPsiUtil.getImportScopeFromEntryFile(project, entryFile, file);
			final Collection<VirtualFile> files = FileTypeIndex.getFiles(JSGraphQLEndpointFileType.INSTANCE, scope);
			for (VirtualFile virtualFile : files) {
				if(virtualFile.equals(entryFile)) {
					// entry file should never be imported
					continue;
				}
				final PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
				if (psiFile != null) {
					if(psiFile.equals(file)) {
						// don't suggest the current file
						continue;
					}
					String name = JSGraphQLEndpointImportUtil.getImportName(project, psiFile);
					if (!StringUtil.isEmpty(name)) {
                        result.addElement(LookupElementBuilder.create(name).withIcon(psiFile.getIcon(0)));
                    }
				}
			}
			return true;
		}
		return false;
	}

	private boolean completeKeywordsAndDefinitionAnnotations(@NotNull CompletionResultSet result, boolean autoImport, PsiElement completionElement, PsiElement leafBeforeCompletion, PsiElement parent) {
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

			completeAnnotations(result, autoImport, completionElement.getContainingFile(), completionElement);

			return true;

		}
		if(parent instanceof JSGraphQLEndpointAnnotation && parent.getParent() instanceof JSGraphQLEndpointNamedTypeDefinition) {
			// completing inside a definition/top level annotation
			return completeAnnotations(result, autoImport, completionElement.getContainingFile(), completionElement);
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


	private boolean completeFieldOrArgumentType(@NotNull CompletionResultSet result, boolean autoImport, PsiElement completionElement) {
		final TokenSet skipping = TokenSet.create(JSGraphQLEndpointTokenTypes.LBRACKET);
		final PsiElement colonBefore = findPreviousLeaf(completionElement, JSGraphQLEndpointTokenTypes.COLON, skipping);
		if (colonBefore != null) {
			for (String scalarType : JSGraphQLScalars.SCALAR_TYPES) {
				LookupElementBuilder element = LookupElementBuilder.create(scalarType).withIcon(GraphQLIcons.Schema.Scalar);
				result.addElement(element);
			}
			for (JSGraphQLEndpointTypeResult enumType : getKnownEnumTypeNames(completionElement, autoImport)) {
				LookupElementBuilder element = LookupElementBuilder.create(enumType.name).withIcon(GraphQLIcons.Schema.Enum);
				element = withAutoImport(element, enumType, autoImport);
				result.addElement(element);
			}
			final boolean isInsideInputValueDefinitions = PsiTreeUtil.getParentOfType(colonBefore, JSGraphQLEndpointInputValueDefinitions.class) != null;
			final JSGraphQLEndpointInputObjectTypeDefinition inputObjectTypeDefinition = PsiTreeUtil.getParentOfType(colonBefore, JSGraphQLEndpointInputObjectTypeDefinition.class);
			final boolean isInsideInputType = inputObjectTypeDefinition != null;
			if (isInsideInputValueDefinitions || isInsideInputType) {
				// argument (input) or input type definition
				final Collection<JSGraphQLEndpointTypeResult<JSGraphQLEndpointInputObjectTypeDefinition>> knownInputTypeNames = getKnownInputTypeNames(completionElement, autoImport);
				if(isInsideInputType) {
					// input types can only refer to other input types: http://facebook.github.io/graphql/#sec-Input-Objects
					if(inputObjectTypeDefinition.getNamedTypeDef() != null) {
						knownInputTypeNames.removeIf(t -> t.name.equals(inputObjectTypeDefinition.getNamedTypeDef().getText()));
					}
				}
				for (JSGraphQLEndpointTypeResult typeResult : knownInputTypeNames) {
					LookupElementBuilder element = LookupElementBuilder.create(typeResult.name).withIcon(GraphQLIcons.Schema.Type);
					element = withAutoImport(element, typeResult, autoImport);
					result.addElement(element);
				}
			} else {
				// field return type (type, interface, union)
				for (JSGraphQLEndpointTypeResult typeResult : getAvailableInterfaceNames(completionElement, autoImport)) {
					LookupElementBuilder element = LookupElementBuilder.create(typeResult.name).withIcon(GraphQLIcons.Schema.Interface);
					element = withAutoImport(element, typeResult, autoImport);
					result.addElement(element);
				}
				for (JSGraphQLEndpointTypeResult typeResult : getKnownTypeNames(completionElement, autoImport)) {
					LookupElementBuilder element = LookupElementBuilder.create(typeResult.name).withIcon(GraphQLIcons.Schema.Type);
					element = withAutoImport(element, typeResult, autoImport);
					result.addElement(element);
				}
				for (JSGraphQLEndpointTypeResult typeResult : getKnownUnionTypeNames(completionElement, autoImport)) {
					LookupElementBuilder element = LookupElementBuilder.create(typeResult.name).withIcon(GraphQLIcons.Schema.Type);
					element = withAutoImport(element, typeResult, autoImport);
					result.addElement(element);
				}
			}
			return true;
		}
		return false;
	}

	private boolean completeAnnotations(@NotNull CompletionResultSet result, boolean autoImport, PsiFile file, PsiElement completionElement) {
		final JSGraphQLEndpointFieldDefinition field = PsiTreeUtil.getNextSiblingOfType(completionElement, JSGraphQLEndpointFieldDefinition.class);
		final JSGraphQLEndpointProperty property = PsiTreeUtil.getNextSiblingOfType(completionElement, JSGraphQLEndpointProperty.class);
		final JSGraphQLEndpointAnnotation nextAnnotation = PsiTreeUtil.getNextSiblingOfType(completionElement, JSGraphQLEndpointAnnotation.class);
		final boolean afterAtAnnotation = completionElement.getNode().getElementType() == JSGraphQLEndpointTokenTypes.AT_ANNOTATION;
		final boolean isTopLevelCompletion = completionElement.getParent() instanceof JSGraphQLEndpointFile;
		if (afterAtAnnotation || isTopLevelCompletion || field != null || nextAnnotation != null || property != null) {
			final JSGraphQLConfigurationProvider configurationProvider = JSGraphQLConfigurationProvider.getService(file.getProject());
			for (JSGraphQLSchemaEndpointAnnotation endpointAnnotation : configurationProvider.getEndpointAnnotations(file)) {
				String completion = endpointAnnotation.name;
				if (!afterAtAnnotation) {
					completion = "@" + completion;
				}
				LookupElementBuilder element = LookupElementBuilder.create(completion).withIcon(GraphQLIcons.Schema.Attribute);
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
				for (JSGraphQLSchemaEndpointAnnotation endpointAnnotation : configurationProvider.getEndpointAnnotations(file)) {
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
				final Collection<JSGraphQLEndpointInterfaceTypeDefinition> interfaceTypeDefinitions = JSGraphQLEndpointPsiUtil.getKnownDefinitions(
						fieldDefinitionSet.getContainingFile(),
						JSGraphQLEndpointInterfaceTypeDefinition.class,
						false,
						null
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
				for (JSGraphQLEndpointTypeResult typeResult : getKnownTypeNames(completionElement, false)) {
					LookupElementBuilder element = LookupElementBuilder.create(typeResult.name).withIcon(GraphQLIcons.Schema.Type);
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

	private LookupElementBuilder withAutoImport(LookupElementBuilder element, JSGraphQLEndpointTypeResult typeResult, boolean autoImport) {
		if(autoImport && typeResult.fileToImport != null) {
			element = element.withInsertHandler(new JSGraphQLEndpointAutoImportInsertHandler(typeResult.fileToImport));
			element = element.withTypeText(typeResult.fileToImport.getName(), true);
		}
		return element;
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

	private Collection<JSGraphQLEndpointTypeResult<JSGraphQLEndpointInterfaceTypeDefinition>> getAvailableInterfaceNames(PsiElement implementsToken, boolean autoImport) {
		final Collection<JSGraphQLEndpointTypeResult<JSGraphQLEndpointInterfaceTypeDefinition>> available = getKnownInterfaceNames(implementsToken, autoImport);
		final JSGraphQLEndpointImplementsInterfacesImpl implementsInterfaces = PsiTreeUtil.getParentOfType(implementsToken, JSGraphQLEndpointImplementsInterfacesImpl.class);
		if (implementsInterfaces != null) {
			final List<String> currentInterfaces = implementsInterfaces.getNamedTypeList().stream().map(type -> type.getIdentifier().getText()).collect(Collectors.toList());
			available.removeIf(t -> currentInterfaces.contains(t.name));
		}
		return available;
	}

	private Collection<JSGraphQLEndpointTypeResult<JSGraphQLEndpointInterfaceTypeDefinition>> getKnownInterfaceNames(PsiElement element, boolean autoImport) {
		return JSGraphQLEndpointPsiUtil.getKnownDefinitionNames(element.getContainingFile(), JSGraphQLEndpointInterfaceTypeDefinition.class, autoImport);
	}

	private Collection<JSGraphQLEndpointTypeResult<JSGraphQLEndpointObjectTypeDefinition>> getKnownTypeNames(PsiElement element, boolean autoImport) {
		return JSGraphQLEndpointPsiUtil.getKnownDefinitionNames(element.getContainingFile(), JSGraphQLEndpointObjectTypeDefinition.class, autoImport);
	}

	private Collection<JSGraphQLEndpointTypeResult<JSGraphQLEndpointInputObjectTypeDefinition>> getKnownInputTypeNames(PsiElement element, boolean autoImport) {
		return JSGraphQLEndpointPsiUtil.getKnownDefinitionNames(element.getContainingFile(), JSGraphQLEndpointInputObjectTypeDefinition.class, autoImport);
	}

	private Collection<JSGraphQLEndpointTypeResult<JSGraphQLEndpointEnumTypeDefinition>> getKnownEnumTypeNames(PsiElement element, boolean autoImport) {
		return JSGraphQLEndpointPsiUtil.getKnownDefinitionNames(element.getContainingFile(), JSGraphQLEndpointEnumTypeDefinition.class, autoImport);
	}


	private Collection<JSGraphQLEndpointTypeResult<JSGraphQLEndpointUnionTypeDefinition>> getKnownUnionTypeNames(PsiElement element, boolean autoImport) {
		return JSGraphQLEndpointPsiUtil.getKnownDefinitionNames(element.getContainingFile(), JSGraphQLEndpointUnionTypeDefinition.class, autoImport);
	}

	private Collection<JSGraphQLEndpointTypeResult<JSGraphQLEndpointAnnotationDefinition>> getKnownAnnotationNames(PsiElement element, boolean autoImport) {
		return JSGraphQLEndpointPsiUtil.getKnownDefinitionNames(element.getContainingFile(), JSGraphQLEndpointAnnotationDefinition.class, autoImport);
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
