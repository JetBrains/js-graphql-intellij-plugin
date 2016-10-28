/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * The value of a field in an input object, enclosed in { }
 */
public class JSGraphQLObjectValuePsiElement extends JSGraphQLPsiElement {

	public JSGraphQLObjectValuePsiElement(@NotNull ASTNode node) {
		super(node);
	}

	@NotNull
	public JSGraphQLAttributePsiElement getAttribute() {
		return PsiTreeUtil.getRequiredChildOfType(this, JSGraphQLAttributePsiElement.class);
	}
}
