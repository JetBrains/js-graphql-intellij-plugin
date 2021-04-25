/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.formatter.kotlin;

import com.intellij.formatting.*;
import com.intellij.injected.editor.DocumentWindow;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.lang.jsgraphql.ide.injection.kotlin.GraphQLLanguageInjectionUtil;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.formatter.common.SettingsAwareBlock;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.util.containers.ContainerUtilRt;
import com.intellij.webcore.formatter.CompositeBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtStringTemplateExpression;

import java.util.ArrayList;
import java.util.List;

public class GraphQLBlockWrapper extends AbstractBlock implements BlockEx, SettingsAwareBlock {
    @Nullable
    private GraphQLBlockWrapper parent;
    private SpacingBuilder spacingBuilder;
    private final CodeStyleSettings settings;
    private Block wrapped;
    private List<Block> subBlocks;

    protected GraphQLBlockWrapper(Block wrapped, @Nullable GraphQLBlockWrapper parent, @NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment, SpacingBuilder spacingBuilder, CodeStyleSettings settings) {
        super(node, wrap, alignment);
        this.wrapped = wrapped;
        this.parent = parent;
        this.spacingBuilder = spacingBuilder;
        this.settings = settings;
    }

    @Override
    protected List<Block> buildChildren() {

        // check if there's children we need to handle -- otherwise just return the wrapped child blocks
        if (wrapped != null) {
            Ref<Boolean> myNodeContainsGraphQL = new Ref<>(false);
            myNode.getPsi().accept(new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(PsiElement element) {
                    if (myNodeContainsGraphQL.get()) {
                        return;
                    } else if (element instanceof KtStringTemplateExpression) {
                        if (GraphQLLanguageInjectionUtil.isKtGraphQLLanguageInjectionTarget(element)) {
                            myNodeContainsGraphQL.set(true);
                            return;
                        }
                    }
                    super.visitElement(element);
                }
            });
            if (!myNodeContainsGraphQL.get()) {
                // no need to wrap the children since we're not doing injection inside them
                return wrapped.getSubBlocks();
            }
        }

        final List<Block> blocks = new ArrayList<>();

        if (myNode.getPsi() instanceof KtStringTemplateExpression && GraphQLLanguageInjectionUtil.isKtGraphQLLanguageInjectionTarget(myNode.getPsi())) {

            KtStringTemplateExpression psi = (KtStringTemplateExpression) myNode.getPsi();
            InjectedLanguageUtil.enumerate(psi, (injectedPsi, places) -> {
                // NO-OP here, but we need to enumerate for injection blocks to work in AbstractBlock#buildInjectedBlocks
                // since they call InjectedLanguageUtil.getCachedInjectedDocuments and return if empty
            });
            // when the psi is a GraphQL injection target, we return an empty block list to allow AbstractBlock#buildInjectedBlocks to build blocks in KtGraphQL
            return blocks;
        }

        if (wrapped != null) {
            final List<Block> subBlocks = wrapped.getSubBlocks();
            for (Block subBlock : subBlocks) {
                if (subBlock instanceof ASTBlock) {
                    final ASTNode node = ((ASTBlock) subBlock).getNode();
                    final Block block = new GraphQLBlockWrapper(subBlock, this, node, subBlock.getWrap(), subBlock.getAlignment(), spacingBuilder, settings);
                    blocks.add(block);
                } else if (subBlock instanceof CompositeBlock) {
                    // the block represents multiple blocks, e.g. a method call and its parameter list
                    final List<Block> nestedSubBlocks = subBlock.getSubBlocks();
                    for (Block nestedSubBlock : nestedSubBlocks) {
                        if (nestedSubBlock instanceof ASTBlock) {
                            final Block block = new GraphQLBlockWrapper(nestedSubBlock, this, ((ASTBlock) nestedSubBlock).getNode(), nestedSubBlock.getWrap(), nestedSubBlock.getAlignment(), spacingBuilder, settings);
                            blocks.add(block);
                        } else {
                            // don't know how to wrap this, but we need to add it to main valid ranges for formatting
                            blocks.add(nestedSubBlock);
                        }
                    }
                } else {
                    // fallback is that we don't know how to format inside this type of block
                    blocks.add(subBlock);
                }
            }

        }

        return blocks;
    }

    @NotNull
    @Override
    public List<Block> getSubBlocks() {
        if (subBlocks == null) {
            subBlocks = super.getSubBlocks();
            if (subBlocks.isEmpty()) {
                subBlocks = buildInjectedBlocks();
            }
        }
        return subBlocks;
    }

    @Nullable
    @Override
    protected Indent getChildIndent() {
        if (myNode.getPsi() instanceof KtStringTemplateExpression) {
            // enter in a top-level block inside template indents, e.g. queries etc.
            return Indent.getNormalIndent();
        }
        if (wrapped != null) {
            return wrapped.getChildAttributes(0).getChildIndent();
        }
        if (parent == null) {
            return Indent.getNoneIndent();
        }
        return Indent.getIndent(Indent.Type.NORMAL, false, false);
    }

    @Override
    public Indent getIndent() {
        if (myNode.getPsi() instanceof KtStringTemplateExpression) {
            // we're a GraphQL block for a Kt string template expression, so return normal indent to indent queries inside the template string
            return Indent.getNormalIndent();
        }
        if (wrapped != null) {
            Indent indent = wrapped.getIndent();
            if (indent != null) {
                return indent;
            }
        }
        return Indent.getNoneIndent();
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        if (wrapped != null) {
            Spacing wrappedSpacing = wrapped.getSpacing(child1, child2);
            if (wrappedSpacing == null) {
                // the wrapped formatter might not recognize a wrapped block, so try with the orignal wrapped children instead
                wrappedSpacing = wrapped.getSpacing(unwrap(child1), unwrap(child2));
            }
            return wrappedSpacing;
        }
        return spacingBuilder.getSpacing(this, child1, child2);
    }

    @Override
    public boolean isLeaf() {
        if (wrapped != null) {
            return wrapped.isLeaf();
        }
        return myNode.getFirstChildNode() == null;
    }

    @NotNull
    @Override
    public TextRange getTextRange() {
        final TextRange textRange;
        if (wrapped != null) {
            textRange = wrapped.getTextRange();
        } else {
            textRange = super.getTextRange();
        }
        return textRange;
    }

    @Nullable
    @Override
    public Language getLanguage() {
        if (wrapped != null) {
            return myNode.getPsi().getLanguage();
        }
        return GraphQLLanguage.INSTANCE;
    }

    @NotNull
    @Override
    public CodeStyleSettings getSettings() {
        return settings;
    }


    // ---- implementation ----

    private Block unwrap(Block child) {
        if (child instanceof GraphQLBlockWrapper) {
            return ((GraphQLBlockWrapper) child).wrapped;
        }
        return child;
    }

    // This is based on AbstractBlock.buildInjectedBlocks, but substitutes DefaultInjectedLanguageBlockBuilder for a GraphQLInjectedLanguageBlockBuilder to
    //  enable host Kt/TS template fragments that separate/shreds the GraphQL with ${...} expressions
    @NotNull
    private List<Block> buildInjectedBlocks() {

        PsiElement psi = myNode.getPsi();
        if (psi == null) {
            return EMPTY;
        }
        PsiFile file = psi.getContainingFile();
        if (file == null) {
            return EMPTY;
        }

        if (InjectedLanguageUtil.getCachedInjectedDocuments(file).isEmpty()) {
            return EMPTY;
        }

        TextRange blockRange = myNode.getTextRange();
        List<DocumentWindow> documentWindows = InjectedLanguageUtil.getCachedInjectedDocuments(file);
        for (DocumentWindow documentWindow : documentWindows) {
            int startOffset = documentWindow.injectedToHost(0);
            int endOffset = startOffset + documentWindow.getTextLength();
            if (blockRange.containsRange(startOffset, endOffset)) {
                PsiFile injected = PsiDocumentManager.getInstance(psi.getProject()).getCachedPsiFile(documentWindow);
                if (injected != null) {
                    List<Block> result = ContainerUtilRt.newArrayList();
                    GraphQLInjectedLanguageBlockBuilder builder = new GraphQLInjectedLanguageBlockBuilder(settings);
                    builder.addInjectedBlocks(result, myNode, getWrap(), getAlignment(), getIndent());
                    return result;
                }
            }
        }
        return EMPTY;
    }

}
