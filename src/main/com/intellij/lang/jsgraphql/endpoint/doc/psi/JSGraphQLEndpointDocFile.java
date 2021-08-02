/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.doc.psi;

import org.jetbrains.annotations.NotNull;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.jsgraphql.endpoint.doc.JSGraphQLEndpointDocFileType;
import com.intellij.lang.jsgraphql.endpoint.doc.JSGraphQLEndpointDocLanguage;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;

public class JSGraphQLEndpointDocFile extends PsiFileBase {

	public JSGraphQLEndpointDocFile(@NotNull FileViewProvider viewProvider) {
		super(viewProvider, JSGraphQLEndpointDocLanguage.INSTANCE);
	}

	@NotNull
	@Override
	public FileType getFileType() {
		return JSGraphQLEndpointDocFileType.INSTANCE;
	}

	@Override
	public String toString() {
		return "GraphQL Endpoint Doc File";
	}

}
