/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.psi;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * Represents a field argument, eg. "id" in "{ node(id: 10) {}}"
 */
public class JSGraphQLArgumentPsiElement extends JSGraphQLPsiElement {

	public JSGraphQLArgumentPsiElement(@NotNull ASTNode node) {
		super(node);
	}

	@NotNull
	public JSGraphQLAttributePsiElement getAttribute() {
		return PsiTreeUtil.getRequiredChildOfType(this, JSGraphQLAttributePsiElement.class);
	}
}
