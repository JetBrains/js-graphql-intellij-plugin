/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.tree.IElementType;

public class JSGraphQLEndpointFoldingBuilder implements FoldingBuilder {

	private static Set<IElementType> FOLDING_ELEMENT_TYPES = Sets.newHashSet(
			JSGraphQLEndpointTokenTypes.FIELD_DEFINITION_SET,
			JSGraphQLEndpointTokenTypes.OPERATION_TYPE_DEFINITION_SET,
			JSGraphQLEndpointTokenTypes.ENUM_VALUE_DEFINITION_SET
	);

	@Nullable
	@Override
	public String getPlaceholderText(@NotNull ASTNode node) {
		return "{...}";
	}

	@Override
	@NotNull
	public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
		List<FoldingDescriptor> list = new ArrayList<>();
		buildFolding(node, list);
		FoldingDescriptor[] descriptors = new FoldingDescriptor[list.size()];
		return list.toArray(descriptors);
	}

	private static void buildFolding(ASTNode node, List<FoldingDescriptor> list) {
		boolean isBlock = FOLDING_ELEMENT_TYPES.contains(node.getElementType());
		if (isBlock && !node.getTextRange().isEmpty()) {
			final TextRange range = node.getTextRange();
			list.add(new FoldingDescriptor(node, range));
		}
		for (ASTNode child : node.getChildren(null)) {
			buildFolding(child, list);
		}
	}

	@Override
	public boolean isCollapsedByDefault(@NotNull ASTNode node) {
		return false;
	}
}
