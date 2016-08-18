/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.psi;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;

/**
 * Represents the type name in a declaration of an endpoint type, e.g. object type, interface type, enum etc.
 */
public class JSGraphQLEndpointNamedTypeDefPsiElement extends JSGraphQLEndpointPsiElement implements PsiNameIdentifierOwner {

	public JSGraphQLEndpointNamedTypeDefPsiElement(@NotNull ASTNode node) {
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
	public boolean isEquivalentTo(PsiElement another) {
		if(another == null) {
			return false;
		}
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
