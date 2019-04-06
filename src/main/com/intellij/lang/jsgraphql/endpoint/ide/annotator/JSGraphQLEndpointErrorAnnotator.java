/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.annotator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.intellij.lang.jsgraphql.endpoint.psi.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.jsgraphql.v1.JSGraphQLScalars;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes;
import com.intellij.lang.jsgraphql.v1.ide.configuration.JSGraphQLConfigurationProvider;
import com.intellij.lang.jsgraphql.v1.ide.configuration.JSGraphQLSchemaEndpointAnnotation;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;

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
					holder.createErrorAnnotation(element, "Unknown type '" + element.getText() + "'. Are you missing an import?").setTextAttributes(CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES);
				} else {

					// types referenced after implements must be interfaces
					if (PsiTreeUtil.getParentOfType(element, JSGraphQLEndpointImplementsInterfaces.class) != null) {
						if (PsiTreeUtil.getParentOfType(resolved, JSGraphQLEndpointInterfaceTypeDefinition.class) == null) {
							holder.createErrorAnnotation(element, "'" + element.getText() + "' must be an interface to be used here");
						}
					}

					// input types must be declared before their use (imports are considered as declared before)
					final JSGraphQLEndpointInputObjectTypeDefinition resolvedInputDef = PsiTreeUtil.getParentOfType(resolved, JSGraphQLEndpointInputObjectTypeDefinition.class);
					if (resolvedInputDef != null) {
						if(resolvedInputDef.getTextOffset() > element.getTextOffset() && resolvedInputDef.getContainingFile() == element.getContainingFile()) {
							// non-imported input types must be declare earlier in the buffer than the usage
							holder.createErrorAnnotation(element, "Input type must be declared before use");
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
				holder.createErrorAnnotation(element, "Cannot resolve file " + element.getText());
			}

			final JSGraphQLEndpointImportDeclaration[] importDeclarations = PsiTreeUtil.getChildrenOfType(element.getContainingFile(), JSGraphQLEndpointImportDeclaration.class);
			if (importDeclarations != null) {
				final String importName = element.getText();
				for (JSGraphQLEndpointImportDeclaration anImport : importDeclarations) {
					final JSGraphQLEndpointImportFileReference fileReference = anImport.getImportFileReference();
					if (fileReference != null && fileReference != element) {
						if (Objects.equals(fileReference.getText(), importName)) {
							holder.createErrorAnnotation(element, element.getText() + " is imported more than once");
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
							holder.createErrorAnnotation(identifier, "Field '" + identifier.getText() + "' is declared more than once");
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
							holder.createErrorAnnotation(fieldReturnType, "Field return type '" + fieldReturnType.getText() + "' cannot be an input type");
						}
					}
				}
			}

			final JSGraphQLEndpointFieldDefinition overridenField = getOverriddenField(fieldDefinition);
			if (overridenField != null) {
				if (!hasSameSignature(fieldDefinition, overridenField)) {
					final JSGraphQLEndpointInterfaceTypeDefinition overridenInterface = PsiTreeUtil.getParentOfType(overridenField, JSGraphQLEndpointInterfaceTypeDefinition.class);
					if (overridenInterface != null && overridenInterface.getNamedTypeDef() != null) {
						int endOffset = fieldDefinition.getProperty().getTextRange().getEndOffset();
						if (fieldDefinition.getCompositeType() != null) {
							endOffset = fieldDefinition.getCompositeType().getTextRange().getEndOffset();
						}
						holder.createErrorAnnotation(TextRange.create(fieldDefinition.getProperty().getTextOffset(), endOffset), "Field signature doesn't match the field it overrides in interface '" + overridenInterface.getNamedTypeDef().getText() + "'");
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
								holder.createErrorAnnotation(argumentType, "Argument type '" + argumentType.getText() + "' must be one of the following: 'input', 'enum', 'scalar'");
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
									holder.createErrorAnnotation(namedArgument.getIdentifier(), "Unknown argument '" + namedArgument.getIdentifier().getText() + "'");
								} else {
									if (namedArgument.getAnnotationArgumentValue() != null) {
										switch (type) {
											case "String":
												if (namedArgument.getAnnotationArgumentValue().getQuotedString() == null) {
													holder.createErrorAnnotation(namedArgument.getAnnotationArgumentValue(), "String value expected");
												}
												break;
											case "Boolean": {
												final PsiElement firstChild = namedArgument.getAnnotationArgumentValue().getFirstChild();
												if (firstChild != null) {
													if (!TokenSet.create(JSGraphQLEndpointTokenTypes.TRUE, JSGraphQLEndpointTokenTypes.FALSE).contains(firstChild.getNode().getElementType())) {
														holder.createErrorAnnotation(namedArgument.getAnnotationArgumentValue(), "true or false expected");
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
														holder.createErrorAnnotation(namedArgument.getAnnotationArgumentValue(), "Number expected");
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
				holder.createErrorAnnotation(atAnnotation, "Unknown annotation '" + atAnnotation.getText() + "'.");
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
			holder.createErrorAnnotation(element, "'" + typeName + "' is redeclared in " + StringUtils.join(files, ", "));
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
			public void visitElement(PsiElement element) {
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
