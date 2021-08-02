/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.doc;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.intellij.psi.tree.IElementType;

public class JSGraphQLEndpointDocTokenType extends IElementType {

	public JSGraphQLEndpointDocTokenType(@NotNull @NonNls String debugName) {
		super(debugName, JSGraphQLEndpointDocLanguage.INSTANCE);
	}
}

