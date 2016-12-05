/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.formatter;

import com.google.common.collect.Sets;
import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.JSGraphQLDebugUtil;
import com.intellij.lang.jsgraphql.JSGraphQLKeywords;
import com.intellij.lang.jsgraphql.JSGraphQLTokenTypes;
import com.intellij.lang.jsgraphql.psi.JSGraphQLElementType;
import com.intellij.lang.jsgraphql.psi.JSGraphQLPsiElement;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.TokenType;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.intellij.lang.jsgraphql.psi.JSGraphQLElementType.*;

public class JSGraphQLBlock extends AbstractBlock {

    private static final Logger log = Logger.getInstance(JSGraphQLBlock.class);

    private static final Set<String> INDENT_PARENT_KINDS = Sets.newHashSet(
            OBJECT_TYPE_DEF_KIND,
            INTERFACE_DEF_KIND,
            ENUM_DEF_KIND,
            INPUT_DEF_KIND,
            EXTEND_DEF_KIND
    );

    private static final Set<IElementType> NO_INDENT_ELEMENT_TYPES = Sets.newHashSet(
            JSGraphQLTokenTypes.LBRACE,
            JSGraphQLTokenTypes.RBRACE,
            JSGraphQLTokenTypes.LBRACKET,
            JSGraphQLTokenTypes.RBRACKET,
            JSGraphQLTokenTypes.LPAREN,
            JSGraphQLTokenTypes.RPAREN
    );

    @Nullable
    private JSGraphQLBlock parent;
    private SpacingBuilder spacingBuilder;

    protected JSGraphQLBlock(@Nullable JSGraphQLBlock parent, @NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment, SpacingBuilder spacingBuilder) {
        super(node, wrap, alignment);
        this.parent = parent;
        this.spacingBuilder = spacingBuilder;
    }

    @Override
    protected List<Block> buildChildren() {

        List<Block> blocks = new ArrayList<>();
        ASTNode child = myNode.getFirstChildNode();
        while (child != null) {
            if (!JSGraphQLTokenTypes.WHITESPACE.equals(child.getElementType()) && !TokenType.ERROR_ELEMENT.equals(child.getElementType())) {
                if (!child.getTextRange().isEmpty()) {
                    JSGraphQLBlock block = new JSGraphQLBlock(this, child, null, null, spacingBuilder);
                    blocks.add(block);
                }
            }
            child = child.getTreeNext();
        }

        verifyBlocks(blocks);

        return blocks;
    }

    public static void verifyBlocks(List<Block> blocks) {
        if (!JSGraphQLDebugUtil.debug || blocks.isEmpty()) return;
        for (Block block : blocks) {
            TextRange textRange = block.getTextRange();
            if (textRange.isEmpty()) {
                log.error("Empty block", block.toString());
            }
            if (textRange.getEndOffset() < textRange.getStartOffset()) {
                log.error("Negative block", block.toString());
            }
        }
    }

    @Nullable
    @Override
    protected Indent getChildIndent() {
        if (parent == null) {
            return Indent.getNoneIndent();
        }
        return Indent.getIndent(Indent.Type.NORMAL, false, false);
    }

    private JSGraphQLElementType getAstNode(JSGraphQLBlock block) {
        if (block.myNode.getPsi() instanceof JSGraphQLPsiElement) {
            final JSGraphQLPsiElement element = ((JSGraphQLPsiElement) block.myNode.getPsi());
            final JSGraphQLElementType astNode = (JSGraphQLElementType) element.getNode().getElementType();
            return astNode;
        }
        return null;
    }

    @Override
    public Indent getIndent() {

        if (NO_INDENT_ELEMENT_TYPES.contains(myNode.getElementType())) {
            return Indent.getNoneIndent();
        }
        if(myNode.getElementType() == JSGraphQLTokenTypes.PUNCTUATION) {
            if(",".equals(myNode.getText())) {
                return Indent.getNormalIndent();
            }
        }

        if(parent != null) {
            JSGraphQLElementType astNode = getAstNode(parent);
            if (astNode != null) {
                final String kind = astNode.getKind();
                if (JSGraphQLElementType.SELECTION_SET_KIND.equals(kind)) {
                    // this block is inside a selection set: '{ ... }', so indent it
                    return Indent.getNormalIndent();
                }
                if (JSGraphQLElementType.ARGUMENTS_KIND.equals(kind)) {
                    // this block is inside a () arguments list, so indent it
                    return Indent.getNormalIndent();
                }
                if (JSGraphQLElementType.OBJECT_VALUE_KIND.equals(kind)) {
                    // this block is inside a {} object value, so indent it
                    return Indent.getNormalIndent();
                }
                if (JSGraphQLElementType.LIST_VALUE_KIND.equals(kind)) {
                    // this block is inside a [] list value, so indent it
                    return Indent.getNormalIndent();
                }
                if(JSGraphQLElementType.SCHEMA_DEF_KIND.equals(kind)) {
                    // inside schema {}
                    if(myNode.getText().equals(JSGraphQLKeywords.QUERY) || myNode.getText().equals(JSGraphQLKeywords.MUTATION) || myNode.getText().equals(JSGraphQLKeywords.SUBSCRIPTION)) {
                        return Indent.getNormalIndent();
                    }
                    if(myNode.getElementType() == JSGraphQLTokenTypes.COMMENT) {
                        return Indent.getNormalIndent();
                    }
                }
                if(myNode.getElementType() != JSGraphQLTokenTypes.KEYWORD && INDENT_PARENT_KINDS.contains(kind)) {
                    // properties inside schema definitions
                    return Indent.getNormalIndent();
                }
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
                    Logger.getInstance(JSGraphQLBlock.class).error("Invalid range " + textRange + ": " + this.getNode().getText());
                }
            }
            if (textRange.getStartOffset() > textRange.getEndOffset()) {
                System.out.println("Invalid range:" + this);
            }
        }
        return textRange;
    }


}
