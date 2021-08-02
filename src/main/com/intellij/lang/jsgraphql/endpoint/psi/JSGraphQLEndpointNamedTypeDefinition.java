/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.psi;

import org.jetbrains.annotations.Nullable;

import com.intellij.psi.PsiElement;

/**
 * PSI interface implemented by all type definitions, e.g. object type, interface type, etc.
 */
public interface JSGraphQLEndpointNamedTypeDefinition extends PsiElement {

	@Nullable
	JSGraphQLEndpointNamedTypeDef getNamedTypeDef();

}
