/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.doc;

import javax.swing.*;

import com.intellij.lang.jsgraphql.icons.GraphQLIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.fileTypes.LanguageFileType;

public class JSGraphQLEndpointDocFileType extends LanguageFileType {

	public static final JSGraphQLEndpointDocFileType INSTANCE = new JSGraphQLEndpointDocFileType();

	private JSGraphQLEndpointDocFileType() {
		super(JSGraphQLEndpointDocLanguage.INSTANCE);
	}

	@NotNull
	@Override
	public String getName() {
		return "GraphQL Endpoint Doc";
	}

	@NotNull
	@Override
	public String getDescription() {
		return "GraphQL Endpoint Doc file";
	}

	@NotNull
	@Override
	public String getDefaultExtension() {
		return "graphqld";
	}

	@Nullable
	@Override
	public Icon getIcon() {
		return GraphQLIcons.Files.GraphQLSchema;
	}
}
