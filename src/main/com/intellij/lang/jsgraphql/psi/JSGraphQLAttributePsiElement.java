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

/**
 * The name of a field argument or input type field
 */
public class JSGraphQLAttributePsiElement extends JSGraphQLNamedPsiElement {

	public JSGraphQLAttributePsiElement(@NotNull ASTNode node) {
		super(node);
	}
}
