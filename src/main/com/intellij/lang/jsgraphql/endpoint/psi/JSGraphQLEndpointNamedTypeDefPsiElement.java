/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.psi;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.endpoint.doc.psi.JSGraphQLEndpointDocPsiUtil;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * Represents the type name in a declaration of an endpoint type, e.g. object type, interface type, enum etc.
 */
public class JSGraphQLEndpointNamedTypeDefPsiElement extends JSGraphQLEndpointNamedPsiElement implements JSGraphQLEndpointDocumentationAware {

	public JSGraphQLEndpointNamedTypeDefPsiElement(@NotNull ASTNode node) {
		super(node);
	}

	@Override
	public String getDeclaration() {
		final StringBuilder sb = new StringBuilder();
		for (ASTNode child : this.getParent().getNode().getChildren(null)) {
			final String text = child.getText();
			if(text.startsWith("{")) {
				// don't include field definitions
				break;
			}
			sb.append(text);
		}
		return sb.toString();
	}

	@Override
	public String getDocumentation(boolean fullDocumentation) {
		final JSGraphQLEndpointNamedTypeDefinition parent = PsiTreeUtil.getParentOfType(this, JSGraphQLEndpointNamedTypeDefinition.class);
		if(parent != null) {
			return JSGraphQLEndpointDocPsiUtil.getDocumentation(parent);
		}
		return null;
	}
}
