/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.psi;

import static com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointImportFileReferencePsiElement.*;

import java.util.Collection;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.v1.JSGraphQLScalars;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;

/**
 * Represents a usage of a specific named type that is declared elsewhere, for example the return type of a field.
 */
public class JSGraphQLEndpointNamedTypePsiElement extends JSGraphQLEndpointPsiElement implements PsiNameIdentifierOwner {

	public JSGraphQLEndpointNamedTypePsiElement(@NotNull ASTNode node) {
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
		final JSGraphQLEndpointNamedTypePsiElement self = this;
		final PsiElement nameIdentifier = getNameIdentifier();
		if(nameIdentifier != null) {
			if(JSGraphQLScalars.SCALAR_TYPES.contains(nameIdentifier.getText())) {
				return new PsiReferenceBase.Immediate<PsiElement>(this, TextRange.allOf(nameIdentifier.getText()), getFirstChild());
			}
			return new PsiReferenceBase<PsiElement>(this, TextRange.from(nameIdentifier.getTextOffset() - self.getTextOffset(), nameIdentifier.getTextLength())) {
				@Nullable
				@Override
				public PsiElement resolve() {
					final Collection<JSGraphQLEndpointNamedTypeDefinition> definitions = JSGraphQLEndpointPsiUtil.getKnownDefinitions(
							self.getContainingFile(),
							JSGraphQLEndpointNamedTypeDefinition.class,
							false,
							null
					);
					final JSGraphQLEndpointNamedTypeDefinition resolvedElement = definitions.stream()
							.filter(d -> d.getNamedTypeDef() != null && d.getNamedTypeDef().getText().equals(nameIdentifier.getText()))
							.findFirst().orElse(null);
					if(resolvedElement != null) {
						return resolvedElement.getNamedTypeDef();
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
}
