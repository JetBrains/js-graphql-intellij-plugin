/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.annotator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes;
import com.intellij.lang.jsgraphql.endpoint.psi.*;
import com.intellij.lang.jsgraphql.v1.JSGraphQLScalars;
import com.intellij.lang.jsgraphql.v1.ide.configuration.JSGraphQLConfigurationProvider;
import com.intellij.lang.jsgraphql.v1.ide.configuration.JSGraphQLSchemaEndpointAnnotation;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class JSGraphQLEndpointErrorAnnotator implements Annotator {

	private static final Key<Multimap<String, JSGraphQLEndpointNamedTypeDefinition>> KNOWN_DEFINITIONS = Key.create("JSGraphQLEndpointErrorAnnotator.knownDefinitions");
	private static final Key<List<JSGraphQLSchemaEndpointAnnotation>> ANNOTATIONS = Key.create(JSGraphQLSchemaEndpointAnnotation.class.getName());

	@Override
	public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {

		// references, e.g. to unknown types
		if (element instanceof JSGraphQLEndpointNamedTypePsiElement) {
			final PsiReference reference = element.getReference();
			if (reference != null) {
				final PsiElement resolved = reference.resolve();
				if (resolved == null) {
					holder.newAnnotation(HighlightSeverity.ERROR, "Unknown type '" + element.getText() + "'. Are you missing an import?")
                        .textAttributes(CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES)
                        .range(element)
                        .create();
				} else {
					// types referenced after implements must be interfaces
					if (PsiTreeUtil.getParentOfType(element, JSGraphQLEndpointImplementsInterfaces.class) != null) {
						if (PsiTreeUtil.getParentOfType(resolved, JSGraphQLEndpointInterfaceTypeDefinition.class) == null) {
							holder.newAnnotation(HighlightSeverity.ERROR, "'" + element.getText() + "' must be an interface to be used here")
                                .range(element)
                                .create();
						}
					}

					// input types must be declared before their use (imports are considered as declared before)
					final JSGraphQLEndpointInputObjectTypeDefinition resolvedInputDef = PsiTreeUtil.getParentOfType(resolved, JSGraphQLEndpointInputObjectTypeDefinition.class);
					if (resolvedInputDef != null) {
						if(resolvedInputDef.getTextOffset() > element.getTextOffset() && resolvedInputDef.getContainingFile() == element.getContainingFile()) {
							// non-imported input types must be declared earlier in the buffer than the usage
							holder.newAnnotation(HighlightSeverity.ERROR, "Input type must be declared before use")
                                .range(element)
                                .create();
						}
					}

				}
			}

			return;
		}

		// imports
		if (element instanceof JSGraphQLEndpointImportFileReference) {
			final PsiReference reference = element.getReference();
			if (reference == null || reference.resolve() == null) {
				// file not found
				holder.newAnnotation(HighlightSeverity.ERROR, "Cannot resolve file " + element.getText())
                    .range(element)
                    .create();
			}

			final JSGraphQLEndpointImportDeclaration[] importDeclarations = PsiTreeUtil.getChildrenOfType(element.getContainingFile(), JSGraphQLEndpointImportDeclaration.class);
			if (importDeclarations != null) {
				final String importName = element.getText();
				for (JSGraphQLEndpointImportDeclaration anImport : importDeclarations) {
					final JSGraphQLEndpointImportFileReference fileReference = anImport.getImportFileReference();
					if (fileReference != null && fileReference != element) {
						if (Objects.equals(fileReference.getText(), importName)) {
							holder.newAnnotation(HighlightSeverity.ERROR, element.getText() + " is imported more than once")
                                .range(element)
                                .create();
						}
					}
				}
			}

			return;
		}

		// fields
		if (element instanceof JSGraphQLEndpointFieldDefinition) {
			final JSGraphQLEndpointFieldDefinition fieldDefinition = (JSGraphQLEndpointFieldDefinition) element;
			final PsiElement identifier = fieldDefinition.getProperty().getIdentifier();
			final String fieldName = identifier.getText();
			// duplicate fields
			final JSGraphQLEndpointFieldDefinitionSet fieldDefinitionSet = PsiTreeUtil.getParentOfType(element, JSGraphQLEndpointFieldDefinitionSet.class);
			if (fieldDefinitionSet != null) {
				final JSGraphQLEndpointFieldDefinition[] allFields = PsiTreeUtil.getChildrenOfType(fieldDefinitionSet, JSGraphQLEndpointFieldDefinition.class);
				if (allFields != null) {
					for (JSGraphQLEndpointFieldDefinition otherField : allFields) {
						if (otherField == fieldDefinition) {
							continue;
						}
						if (Objects.equals(otherField.getProperty().getIdentifier().getText(), fieldName)) {
							holder.newAnnotation(HighlightSeverity.ERROR, "Field '" + identifier.getText() + "' is declared more than once")
                                .range(identifier)
                                .create();
						}
					}
				}
			}
			// field return type must not be input inside non-input types
			if (fieldDefinition.getCompositeType() != null) {
				final JSGraphQLEndpointNamedTypePsiElement fieldReturnType = PsiTreeUtil.findChildOfType(fieldDefinition.getCompositeType(), JSGraphQLEndpointNamedTypePsiElement.class);
				if (fieldReturnType != null && PsiTreeUtil.getParentOfType(fieldDefinition, JSGraphQLEndpointInputObjectTypeDefinition.class) == null) {
					final PsiReference reference = fieldReturnType.getReference();
					if (reference != null) {
						final PsiElement resolved = reference.resolve();
						if (resolved != null && PsiTreeUtil.getParentOfType(resolved, JSGraphQLEndpointInputObjectTypeDefinition.class) != null) {
							holder.newAnnotation(HighlightSeverity.ERROR, "Field return type '" + fieldReturnType.getText() + "' cannot be an input type")
                                .range(fieldReturnType)
                                .create();
						}
					}
				}
			}

			final JSGraphQLEndpointFieldDefinition overriddenField = getOverriddenField(fieldDefinition);
			if (overriddenField != null) {
				if (!hasSameSignature(fieldDefinition, overriddenField)) {
					final JSGraphQLEndpointInterfaceTypeDefinition overriddenInterface = PsiTreeUtil.getParentOfType(overriddenField, JSGraphQLEndpointInterfaceTypeDefinition.class);
					if (overriddenInterface != null && overriddenInterface.getNamedTypeDef() != null) {
						int endOffset = fieldDefinition.getProperty().getTextRange().getEndOffset();
						if (fieldDefinition.getCompositeType() != null) {
							endOffset = fieldDefinition.getCompositeType().getTextRange().getEndOffset();
						}
						holder.newAnnotation(HighlightSeverity.ERROR, "Field signature doesn't match the field it overrides in interface '" + overriddenInterface.getNamedTypeDef().getText() + "'")
                            .range(TextRange.create(fieldDefinition.getProperty().getTextOffset(), endOffset))
                            .create();
					}
				}
			}

			return;
		}

		// argument types must be input, scalar, enum
		if (element instanceof JSGraphQLEndpointArgumentsDefinition) {
			final Collection<JSGraphQLEndpointNamedTypePsiElement> argumentTypes = PsiTreeUtil.findChildrenOfType(element, JSGraphQLEndpointNamedTypePsiElement.class);
			for (JSGraphQLEndpointNamedTypePsiElement argumentType : argumentTypes) {
				final PsiReference reference = argumentType.getReference();
				if (reference != null) {
					final PsiElement resolved = reference.resolve();
					if (resolved != null) {
						boolean valid = true;
						boolean builtInScalar = JSGraphQLScalars.SCALAR_TYPES.contains(resolved.getText());
						if (!builtInScalar) {
							if (PsiTreeUtil.getParentOfType(resolved, JSGraphQLEndpointObjectTypeDefinition.class) != null) {
								valid = false;
							} else if (PsiTreeUtil.getParentOfType(resolved, JSGraphQLEndpointInterfaceTypeDefinition.class) != null) {
								valid = false;
							} else if (PsiTreeUtil.getParentOfType(resolved, JSGraphQLEndpointUnionTypeDefinition.class) != null) {
								valid = false;
							}
							if (!valid) {
								holder.newAnnotation(HighlightSeverity.ERROR, "Argument type '" + argumentType.getText() + "' must be one of the following: 'input', 'enum', 'scalar'")
                                    .range(argumentType)
                                    .create();
							}
						}
					}
				}
			}

			return;
		}

		// annotations
		if (element instanceof JSGraphQLEndpointAnnotation) {
			final JSGraphQLEndpointAnnotation annotation = (JSGraphQLEndpointAnnotation) element;
			final PsiElement atAnnotation = annotation.getAtAnnotation();
			List<JSGraphQLSchemaEndpointAnnotation> annotations = holder.getCurrentAnnotationSession().getUserData(ANNOTATIONS);
			if (annotations == null) {
				final JSGraphQLConfigurationProvider configurationProvider = JSGraphQLConfigurationProvider.getService(element.getProject());
				annotations = configurationProvider.getEndpointAnnotations(element.getContainingFile());
				holder.getCurrentAnnotationSession().putUserData(ANNOTATIONS, annotations);
			}
			boolean knownAnnotation = false;
			for (JSGraphQLSchemaEndpointAnnotation endpointAnnotation : annotations) {
				if (Objects.equals("@" + endpointAnnotation.name, atAnnotation.getText())) {
					knownAnnotation = true;
					// check argument names and types
					if (endpointAnnotation.arguments != null) {
						final Map<String, String> argumentNameToType = endpointAnnotation.arguments.stream().collect(Collectors.toMap(a -> a.name, a -> Optional.ofNullable(a.type).orElse("")));
						if (annotation.getAnnotationArguments() != null && annotation.getAnnotationArguments().getNamedAnnotationArguments() != null) {
							for (JSGraphQLEndpointNamedAnnotationArgument namedArgument : annotation.getAnnotationArguments().getNamedAnnotationArguments().getNamedAnnotationArgumentList()) {
								final String type = argumentNameToType.get(namedArgument.getIdentifier().getText());
								if (type == null) {
                                    holder.newAnnotation(HighlightSeverity.ERROR, "Unknown argument '" + namedArgument.getIdentifier().getText() + "'")
                                        .range(namedArgument.getIdentifier())
                                        .create();
								} else {
									if (namedArgument.getAnnotationArgumentValue() != null) {
										switch (type) {
											case "String":
												if (namedArgument.getAnnotationArgumentValue().getQuotedString() == null) {
													holder.newAnnotation(HighlightSeverity.ERROR, "String value expected")
                                                        .range(namedArgument.getAnnotationArgumentValue())
                                                        .create();
												}
												break;
											case "Boolean": {
												final PsiElement firstChild = namedArgument.getAnnotationArgumentValue().getFirstChild();
												if (firstChild != null) {
													if (!TokenSet.create(JSGraphQLEndpointTokenTypes.TRUE, JSGraphQLEndpointTokenTypes.FALSE).contains(firstChild.getNode().getElementType())) {
														holder.newAnnotation(HighlightSeverity.ERROR, "True or false expected")
                                                            .range(namedArgument.getAnnotationArgumentValue())
                                                            .create();
													}
												}
												break;
											}
											case "Number":
											case "Float":
											case "Int": {
												final PsiElement firstChild = namedArgument.getAnnotationArgumentValue().getFirstChild();
												if (firstChild != null) {
													if (!JSGraphQLEndpointTokenTypes.NUMBER.equals(firstChild.getNode().getElementType())) {
														holder.newAnnotation(HighlightSeverity.ERROR, "Number expected")
                                                            .range(namedArgument.getAnnotationArgumentValue())
                                                            .create();
													}
												}
												break;
											}
										}
									}
								}
							}
						}
					}
					break;
				}
			}
			if (!knownAnnotation) {
				holder.newAnnotation(HighlightSeverity.ERROR, "Unknown annotation '" + atAnnotation.getText() + "'.")
                    .range(atAnnotation)
                    .create();
			}
			return;

		}

		// duplicate types with same name
		if (element instanceof JSGraphQLEndpointNamedTypeDef) {
			final JSGraphQLEndpointNamedTypeDef namedTypeDef = (JSGraphQLEndpointNamedTypeDef) element;

			// current file
			annotateRedeclarations(namedTypeDef, element.getContainingFile(), KNOWN_DEFINITIONS, holder);

		}


	}

	@SuppressWarnings("SameParameterValue")
    private void annotateRedeclarations(@NotNull JSGraphQLEndpointNamedTypeDef element, PsiFile importingFile, Key<Multimap<String, JSGraphQLEndpointNamedTypeDefinition>> key, @NotNull AnnotationHolder holder) {
		final Key<Boolean> annotationKey = Key.create(element.getContainingFile().getName() + ":" + element.getTextOffset());
		if (holder.getCurrentAnnotationSession().getUserData(annotationKey) == Boolean.TRUE) {
			// already annotated about redeclaration
			return;
		}
		Multimap<String, JSGraphQLEndpointNamedTypeDefinition> knownDefinitionsByName = holder.getCurrentAnnotationSession().getUserData(key);
		if (knownDefinitionsByName == null) {
			knownDefinitionsByName = HashMultimap.create();
			for (JSGraphQLEndpointNamedTypeDefinition definition : JSGraphQLEndpointPsiUtil.getKnownDefinitions(importingFile, JSGraphQLEndpointNamedTypeDefinition.class, true, null)) {
				if (definition.getNamedTypeDef() != null) {
					knownDefinitionsByName.put(definition.getNamedTypeDef().getText(), definition);
				}
			}
		}
		final String typeName = element.getText();
		final Collection<JSGraphQLEndpointNamedTypeDefinition> typesWithSameName = knownDefinitionsByName.get(typeName);
		if (typesWithSameName != null && typesWithSameName.size() > 1) {
			final Set<String> files = typesWithSameName.stream().map(t -> "'" + t.getContainingFile().getName() + "'").collect(Collectors.toSet());
            holder.newAnnotation(HighlightSeverity.ERROR, "'" + typeName + "' is redeclared in " + StringUtils.join(files, ", "))
                .range(element)
                .create();
			holder.getCurrentAnnotationSession().putUserData(annotationKey, Boolean.TRUE);
		}
	}

	private JSGraphQLEndpointFieldDefinition getOverriddenField(JSGraphQLEndpointFieldDefinition override) {
		final String propertyName = override.getProperty().getIdentifier().getText();
		final JSGraphQLEndpointObjectTypeDefinition typeDefinition = PsiTreeUtil.getParentOfType(override, JSGraphQLEndpointObjectTypeDefinition.class);
		if (typeDefinition != null) {
			final JSGraphQLEndpointImplementsInterfaces implementsInterfaces = PsiTreeUtil.findChildOfType(typeDefinition, JSGraphQLEndpointImplementsInterfaces.class);
			if (implementsInterfaces != null) {
				for (JSGraphQLEndpointNamedType namedType : implementsInterfaces.getNamedTypeList()) {
					final PsiReference reference = namedType.getReference();
					if (reference != null) {
						final PsiElement interfaceTypeName = reference.resolve();
						if (interfaceTypeName != null) {
							final JSGraphQLEndpointInterfaceTypeDefinition interfaceTypeDefinition = PsiTreeUtil.getParentOfType(interfaceTypeName, JSGraphQLEndpointInterfaceTypeDefinition.class);
							if (interfaceTypeDefinition != null) {
								for (JSGraphQLEndpointProperty property : PsiTreeUtil.findChildrenOfType(interfaceTypeDefinition, JSGraphQLEndpointProperty.class)) {
									if (property.getIdentifier().getText().equals(propertyName)) {
										return PsiTreeUtil.getParentOfType(property, JSGraphQLEndpointFieldDefinition.class);
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Compares the signatures of two fields, ignoring comments, whitespace, and annotations
	 */
	private boolean hasSameSignature(JSGraphQLEndpointFieldDefinition override, JSGraphQLEndpointFieldDefinition toImplement) {
		final StringBuilder toImplementSignature = new StringBuilder();
		final StringBuilder overrideSignature = new StringBuilder();

		final Ref<StringBuilder> sb = new Ref<>();
		final PsiElementVisitor visitor = new PsiRecursiveElementVisitor() {
			@Override
			public void visitElement(@NotNull PsiElement element) {
				if (element instanceof JSGraphQLEndpointAnnotation) {
					return;
				}
				if (element instanceof PsiWhiteSpace) {
					return;
				}
				if (element instanceof PsiComment) {
					return;
				}
				if (element instanceof LeafPsiElement) {
					sb.get().append(element.getText()).append(" ");
				}
				super.visitElement(element);
			}
		};

		sb.set(overrideSignature);
		override.accept(visitor);

		sb.set(toImplementSignature);
		toImplement.accept(visitor);

		return toImplementSignature.toString().equals(overrideSignature.toString());
	}

}
