/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.psi;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.jsgraphql.psi.JSGraphQLPsiElement;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointLanguage;

public abstract class JSGraphQLEndpointPsiElement extends JSGraphQLPsiElement {

	public JSGraphQLEndpointPsiElement(@NotNull ASTNode node) {
		super(node);
	}

	@NotNull
	@Override
	public Language getLanguage() {
		return JSGraphQLEndpointLanguage.INSTANCE;
	}
}
