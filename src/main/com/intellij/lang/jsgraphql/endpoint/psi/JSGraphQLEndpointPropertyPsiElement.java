/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes;
import com.intellij.lang.jsgraphql.endpoint.doc.psi.JSGraphQLEndpointDocPsiUtil;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointImportFileReferencePsiElement.NO_VARIANTS;

/**
 * Represents the property name of a field in a field definition set
 */
public class JSGraphQLEndpointPropertyPsiElement extends JSGraphQLEndpointPsiElement implements PsiNameIdentifierOwner, JSGraphQLEndpointDocumentationAware {

	public JSGraphQLEndpointPropertyPsiElement(@NotNull ASTNode node) {
		super(node);
	}

	@Override
	public String getName() {
		// used for find usages
		final PsiElement nameIdentifier = getNameIdentifier();
		return nameIdentifier != null ? nameIdentifier.getText() : getText();
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier() {
		final ASTNode identifier = getNode().findChildByType(JSGraphQLEndpointTokenTypes.IDENTIFIER);
		return identifier != null ? identifier.getPsi() : null;
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
		return JSGraphQLEndpointPsiUtil.renameIdentifier(this, name);
	}

	@Override
	public PsiReference getReference() {
		final JSGraphQLEndpointPropertyPsiElement self = this;
		final PsiElement nameIdentifier = getNameIdentifier();
		if(nameIdentifier != null) {
			return new PsiReferenceBase<PsiElement>(this, TextRange.from(nameIdentifier.getTextOffset() - self.getTextOffset(), nameIdentifier.getTextLength())) {
				@Nullable
				@Override
				public PsiElement resolve() {
					// the property may belong to a field that was declared in an implemented interface
					final JSGraphQLEndpointObjectTypeDefinition typeDefinition = PsiTreeUtil.getParentOfType(self, JSGraphQLEndpointObjectTypeDefinition.class);
					if(typeDefinition != null && typeDefinition.getImplementsInterfaces() != null) {
						final JSGraphQLEndpointNamedType[] implementedTypes = PsiTreeUtil.getChildrenOfType(typeDefinition.getImplementsInterfaces(), JSGraphQLEndpointNamedType.class);
						if(implementedTypes != null) {
							for (JSGraphQLEndpointNamedType implementedType : implementedTypes) {
								final PsiReference reference = implementedType.getReference();
								final PsiElement resolvedType = reference.resolve();
								if(resolvedType != null && resolvedType.getParent() instanceof JSGraphQLEndpointInterfaceTypeDefinition) {
									Ref<JSGraphQLEndpointPropertyPsiElement> result = new Ref<>(null);
									resolvedType.getParent().accept(new PsiRecursiveElementVisitor() {
										@Override
										public void visitElement(PsiElement element) {
											if(result.get() != null) {
												return;
											}
											if(element instanceof JSGraphQLEndpointPropertyPsiElement) {
												final JSGraphQLEndpointPropertyPsiElement interfaceProperty = (JSGraphQLEndpointPropertyPsiElement) element;
												final String name = interfaceProperty.getName();
												if(nameIdentifier.getText().equals(name)) {
													result.set(interfaceProperty);
												}
											}
											super.visitElement(element);
										}
									});
									return result.get();
								}
							}
						}
					}
					return null;
				}

				@NotNull
				@Override
				public Object[] getVariants() {
					return NO_VARIANTS;
				}

				@Override
				public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
					return self.setName(newElementName);
				}
			};
		}
		return null;
	}

	@Override
	public boolean isEquivalentTo(PsiElement another) {
		if(this == another) {
			return true;
		}
		final PsiReference reference = another.getReference();
		if(reference != null) {
			return this.equals(reference.resolve());
		}
		return super.isEquivalentTo(another);
	}

	@Override
	public String getDeclaration() {
		final StringBuilder sb = new StringBuilder();
		final JSGraphQLEndpointNamedTypeDefinition parent = PsiTreeUtil.getParentOfType(this, JSGraphQLEndpointNamedTypeDefinition.class);
		if(parent != null && parent.getNamedTypeDef() != null) {
			sb.append(parent.getNamedTypeDef().getText()).append(" ").append(this.getText());
			final JSGraphQLEndpointFieldDefinition fieldDefinition = (JSGraphQLEndpointFieldDefinition) this.getParent();
			if(fieldDefinition.getArgumentsDefinition() != null) {
				sb.append(fieldDefinition.getArgumentsDefinition().getText());
			}
			if(fieldDefinition.getCompositeType() != null) {
				sb.append(": ");
				sb.append(fieldDefinition.getCompositeType().getText());
			}
		}
		return sb.toString();
	}

	@Override
	public String getDocumentation(boolean fullDocumentation) {
		// documentation is placed on the field that the property belongs to
		return JSGraphQLEndpointDocPsiUtil.getDocumentation(this.getParent());
	}
}
