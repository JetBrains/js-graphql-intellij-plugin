/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.doc.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.jsgraphql.endpoint.doc.JSGraphQLEndpointDocLanguage;

public abstract class JSGraphQLEndpointDocPsiElement extends ASTWrapperPsiElement {

	public JSGraphQLEndpointDocPsiElement(@NotNull ASTNode node) {
		super(node);
	}

	@NotNull
	@Override
	public Language getLanguage() {
		return JSGraphQLEndpointDocLanguage.INSTANCE;
	}
}
