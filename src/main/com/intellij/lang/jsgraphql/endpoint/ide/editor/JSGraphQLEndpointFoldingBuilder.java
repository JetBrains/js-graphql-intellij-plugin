/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.editor;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.impl.source.tree.FileElement;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JSGraphQLEndpointFoldingBuilder extends FoldingBuilderEx implements DumbAware {

	private static final TokenSet IMPORTS_TOKEN_SET = TokenSet.create(
			JSGraphQLEndpointTokenTypes.IMPORT_DECLARATION,
			JSGraphQLEndpointTokenTypes.LINE_COMMENT,
			TokenType.WHITE_SPACE
	);

	private static TokenSet FOLDING_ELEMENT_TYPES = TokenSet.create(
			JSGraphQLEndpointTokenTypes.FIELD_DEFINITION_SET,
			JSGraphQLEndpointTokenTypes.OPERATION_TYPE_DEFINITION_SET,
			JSGraphQLEndpointTokenTypes.ENUM_VALUE_DEFINITION_SET,
			JSGraphQLEndpointTokenTypes.ARGUMENTS_DEFINITION,
			JSGraphQLEndpointTokenTypes.ANNOTATION_ARGUMENTS
	);

	@Nullable
	@Override
	public String getPlaceholderText(@NotNull ASTNode node) {
		if(node.getElementType() == JSGraphQLEndpointTokenTypes.IMPORT_DECLARATION) {
			return "...";
		}
		if(node.getElementType() == JSGraphQLEndpointTokenTypes.ARGUMENTS_DEFINITION || node.getElementType() == JSGraphQLEndpointTokenTypes.ANNOTATION_ARGUMENTS) {
			return "(...)";
		}
		return "{...}";
	}

	@NotNull
	@Override
	public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
		List<FoldingDescriptor> list = new ArrayList<>();
		buildFolding(root.getNode(), list);
		FoldingDescriptor[] descriptors = new FoldingDescriptor[list.size()];
		return list.toArray(descriptors);
	}

	private static void buildFolding(ASTNode node, List<FoldingDescriptor> list) {

		// fold consecutive import statements
		if(node instanceof FileElement) {
			final ASTNode[] imports = node.getChildren(TokenSet.create(JSGraphQLEndpointTokenTypes.IMPORT_DECLARATION));
			final Set<ASTNode> folded = Sets.newHashSet();
			for (ASTNode anImport : imports) {
				if(folded.add(anImport)) {
					final List<ASTNode> nodesToFold = Lists.newArrayList(anImport);
					ASTNode next = anImport.getTreeNext();
					while(next != null && IMPORTS_TOKEN_SET.contains(next.getElementType())) {
						nodesToFold.add(next);
						folded.add(next);
						next = next.getTreeNext();
					}
					if(nodesToFold.size() > 1) {
						// more than one import in a row
						ASTNode lastVisible = nodesToFold.get(nodesToFold.size()-1);
						while(lastVisible.getElementType() == TokenType.WHITE_SPACE) {
							lastVisible = lastVisible.getTreePrev();
						}
						final int endOffset = lastVisible.getStartOffset() + lastVisible.getTextLength();
						final TextRange range = TextRange.create(anImport.getStartOffset() + 7, endOffset);
						if(range.getLength() > 0) {
							final Set<Object> dependencies = nodesToFold.stream()
									.filter(n -> n != anImport)
									.map(n -> (Object) n.getPsi())
									.collect(Collectors.toSet());
							list.add(new FoldingDescriptor(anImport, range, null, dependencies));
						}
					}
				}
			}
		}

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
		return node.getElementType() == JSGraphQLEndpointTokenTypes.IMPORT_DECLARATION;
	}
}
