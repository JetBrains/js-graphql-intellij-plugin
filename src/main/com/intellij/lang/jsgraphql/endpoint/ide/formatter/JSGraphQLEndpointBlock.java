/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.formatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;
import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.formatting.Wrap;
import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.v1.JSGraphQLDebugUtil;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.TokenType;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;

public class JSGraphQLEndpointBlock extends AbstractBlock {

	static Set<IElementType> INDENT_PARENTS = Sets.newHashSet(
			JSGraphQLEndpointTokenTypes.FIELD_DEFINITION_SET,
			JSGraphQLEndpointTokenTypes.OPERATION_TYPE_DEFINITION_SET,
			JSGraphQLEndpointTokenTypes.ENUM_VALUE_DEFINITION_SET,
			JSGraphQLEndpointTokenTypes.ARGUMENTS_DEFINITION,
			JSGraphQLEndpointTokenTypes.ANNOTATION_ARGUMENTS,
			JSGraphQLEndpointTokenTypes.ANNOTATION_ARGUMENT_OBJECT_VALUE,
			JSGraphQLEndpointTokenTypes.ANNOTATION_ARGUMENT_LIST_VALUE
	);

	static Set<IElementType> INDENT_EXCLUSIONS = Sets.newHashSet(
			JSGraphQLEndpointTokenTypes.RBRACE,
			JSGraphQLEndpointTokenTypes.LBRACE,
			JSGraphQLEndpointTokenTypes.RPAREN,
			JSGraphQLEndpointTokenTypes.LPAREN,
			JSGraphQLEndpointTokenTypes.RBRACKET,
			JSGraphQLEndpointTokenTypes.LBRACKET
	);

	@Nullable
	private JSGraphQLEndpointBlock parent;
	private SpacingBuilder spacingBuilder;

	protected JSGraphQLEndpointBlock(@Nullable JSGraphQLEndpointBlock parent, @NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment, SpacingBuilder spacingBuilder) {
		super(node, wrap, alignment);
		this.parent = parent;
		this.spacingBuilder = spacingBuilder;
	}

	@Override
	protected List<Block> buildChildren() {

		List<Block> blocks = new ArrayList<>();
		ASTNode child = myNode.getFirstChildNode();
		while (child != null) {
			if (!TokenType.WHITE_SPACE.equals(child.getElementType()) && !TokenType.BAD_CHARACTER.equals(child.getElementType())) {
				if (!child.getTextRange().isEmpty()) {
					JSGraphQLEndpointBlock block = new JSGraphQLEndpointBlock(this, child, null, null, spacingBuilder);
					blocks.add(block);
				}
			}
			child = child.getTreeNext();
		}

		return blocks;
	}

	@Nullable
	@Override
	protected Indent getChildIndent() {
		if (parent == null) {
			return Indent.getNoneIndent();
		}
		if (INDENT_PARENTS.contains(myNode.getElementType())) {
			return Indent.getNormalIndent();
		}
		return Indent.getNoneIndent();
	}

	@Override
	public Indent getIndent() {

		if (INDENT_EXCLUSIONS.contains(myNode.getElementType())) {
			return Indent.getNoneIndent();
		}

		if (parent != null) {
			final IElementType elementType = parent.myNode.getElementType();
			if (INDENT_PARENTS.contains(elementType)) {
				return Indent.getNormalIndent();
			}
		}

		// default is no indentation
		return Indent.getNoneIndent();

	}

	@Nullable
	@Override
	public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
		return spacingBuilder.getSpacing(this, child1, child2);
	}

	@Override
	public boolean isLeaf() {
		return myNode.getFirstChildNode() == null;
	}

	@NotNull
	@Override
	public TextRange getTextRange() {
		TextRange textRange = super.getTextRange();
		if (JSGraphQLDebugUtil.debug) {
			if (parent != null) {
				try {
					assert textRange.getStartOffset() >= parent.getTextRange().getStartOffset();
					assert textRange.getEndOffset() <= parent.getTextRange().getEndOffset();
				} catch (AssertionError e) {
					Logger.getInstance(JSGraphQLEndpointBlock.class).error("Invalid range " + textRange + ": " + this.getNode().getText());
				}
			}
			if (textRange.getStartOffset() > textRange.getEndOffset()) {
				System.out.println("Invalid range:" + this);
			}
		}
		return textRange;
	}


}
