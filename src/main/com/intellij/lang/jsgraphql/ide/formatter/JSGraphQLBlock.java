/**
 *  Copyright (c) 2015, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.JSGraphQLDebugUtil;
import com.intellij.lang.jsgraphql.JSGraphQLTokenTypes;
import com.intellij.lang.jsgraphql.psi.JSGraphQLElementType;
import com.intellij.lang.jsgraphql.psi.JSGraphQLPsiElement;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.TokenType;
import com.intellij.psi.formatter.common.AbstractBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class JSGraphQLBlock extends AbstractBlock {

    private static final Logger log = Logger.getInstance(JSGraphQLBlock.class);

    @Nullable
    private JSGraphQLBlock parent;
    private SpacingBuilder spacingBuilder;
    private List<PsiLanguageInjectionHost.Shred> places;

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
            if(!JSGraphQLTokenTypes.WHITESPACE.equals(child.getElementType()) && !TokenType.ERROR_ELEMENT.equals(child.getElementType())) {
                if(!child.getTextRange().isEmpty()) {
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
        if(!JSGraphQLDebugUtil.debug || blocks.isEmpty()) return;
        for (Block block : blocks) {
            TextRange textRange = block.getTextRange();
            if(textRange.isEmpty()) {
                log.error("Empty block", block.toString());
            }
            if(textRange.getEndOffset() < textRange.getStartOffset()) {
                log.error("Negative block", block.toString());
            }
        }
    }

    @Nullable
    @Override
    protected Indent getChildIndent() {
        if(parent == null) {
            return Indent.getNoneIndent();
        }
        return Indent.getIndent(Indent.Type.NORMAL, false, false);
    }

    private JSGraphQLElementType getAstNode(JSGraphQLBlock block) {
        if(block.myNode.getPsi() instanceof JSGraphQLPsiElement) {
            final JSGraphQLPsiElement element = ((JSGraphQLPsiElement) block.myNode.getPsi());
            final JSGraphQLElementType astNode = (JSGraphQLElementType) element.getNode().getElementType();
            return astNode;
        }
        return null;
    }

    @Override
    public Indent getIndent() {

        if(myNode.getElementType() == JSGraphQLTokenTypes.RBRACE || myNode.getElementType() == JSGraphQLTokenTypes.LBRACE) {
            return Indent.getNoneIndent();
        }

        if(parent != null) {
            JSGraphQLElementType astNode = getAstNode(parent);
            if(astNode != null) {
                if(JSGraphQLElementType.SELECTION_SET_KIND.equals(astNode.getKind())) {
                    // this block is inside a selection set: '{ ... }', so indent it
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
        if(JSGraphQLDebugUtil.debug) {
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
