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

/**
 * Represents the identifier in an an input value definition (the name of a field argument)
 */
public class JSGraphQLEndpointInputValueDefinitionIdentifierPsiElement extends JSGraphQLEndpointNamedPsiElement {

	public JSGraphQLEndpointInputValueDefinitionIdentifierPsiElement(@NotNull ASTNode node) {
		super(node);
	}

}
