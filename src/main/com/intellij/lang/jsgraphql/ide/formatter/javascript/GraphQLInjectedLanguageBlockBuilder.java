/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.formatter.javascript;

import com.google.common.collect.Lists;
import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageFormatting;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.Segment;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.common.DefaultInjectedLanguageBlockBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class GraphQLInjectedLanguageBlockBuilder extends DefaultInjectedLanguageBlockBuilder {

    public GraphQLInjectedLanguageBlockBuilder(@NotNull CodeStyleSettings settings) {
        super(settings);
    }

    @Override
    public boolean addInjectedBlocks(List<? super Block> result, ASTNode injectionHost, Wrap wrap, Alignment alignment, Indent indent) {
        boolean added = super.addInjectedBlocks(result, injectionHost, wrap, alignment, indent);
        if (!added) {
            // 'if (places.size() != 1) {' guard in super
            // happens when the GraphQL is interrupted by JS/TS template fragments
            added = graphQLSupportingAddInjectedBlocks(result, injectionHost, wrap, alignment, indent);
        }
        return added;
    }


    // based on DefaultInjectedLanguageBlockBuilder.addInjectedBlocks but merges multiple shreds into one
    private boolean graphQLSupportingAddInjectedBlocks(List<? super Block> result,
                                                       ASTNode injectionHost,
                                                       Wrap wrap,
                                                       Alignment alignment,
                                                       Indent indent) {
        final PsiFile[] injectedFile = new PsiFile[1];
        final Ref<TextRange> injectedRangeInsideHost = new Ref<>();
        final Ref<Integer> prefixLength = new Ref<>();
        final Ref<Integer> suffixLength = new Ref<>();
        final Ref<ASTNode> injectionHostToUse = new Ref<>(injectionHost);

        final PsiLanguageInjectionHost.InjectedPsiVisitor injectedPsiVisitor = (injectedPsi, defaultImplPlaces) -> {

            List<PsiLanguageInjectionHost.Shred> places = Lists.newArrayList(defaultImplPlaces);
            if (places.size() != 1) {
                // this is where DefaultInjectedLanguageBlockBuilder.addInjectedBlocks bails, and we don't get our injected formatting blocks
                places = mergePlacesIntoOne(places);
            }

            final PsiLanguageInjectionHost.Shred shred = places.get(0);
            TextRange textRange = shred.getRangeInsideHost();
            PsiLanguageInjectionHost shredHost = shred.getHost();
            if (shredHost == null) {
                return;
            }
            ASTNode node = shredHost.getNode();
            if (node == null) {
                return;
            }
            if (node != injectionHost) {
                int shift = 0;
                boolean canProcess = false;
                for (ASTNode n = injectionHost.getTreeParent(), prev = injectionHost; n != null; prev = n, n = n.getTreeParent()) {
                    shift += n.getStartOffset() - prev.getStartOffset();
                    if (n == node) {
                        textRange = textRange.shiftRight(shift);
                        canProcess = true;
                        break;
                    }
                }
                if (!canProcess) {
                    return;
                }
            }

            String childText;
            if ((injectionHost.getTextLength() == textRange.getEndOffset() && textRange.getStartOffset() == 0) ||
                (canProcessFragment((childText = injectionHost.getText()).substring(0, textRange.getStartOffset()), injectionHost) &&
                    canProcessFragment(childText.substring(textRange.getEndOffset()), injectionHost))) {
                injectedFile[0] = injectedPsi;
                injectedRangeInsideHost.set(textRange);
                prefixLength.set(shred.getPrefix().length());
                suffixLength.set(shred.getSuffix().length());
            }
        };
        final PsiElement injectionHostPsi = injectionHost.getPsi();
        Project project = injectionHostPsi.getProject();

        InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(project);
        injectedLanguageManager.enumerateEx(injectionHostPsi, injectionHostPsi.getContainingFile(), false, injectedPsiVisitor);

        if (injectedFile[0] != null) {
            final Language childLanguage = injectedFile[0].getLanguage();
            final FormattingModelBuilder builder = LanguageFormatting.INSTANCE.forContext(childLanguage, injectionHostPsi);

            if (builder != null) {
                final int startOffset = injectedRangeInsideHost.get().getStartOffset();
                final int endOffset = injectedRangeInsideHost.get().getEndOffset();
                TextRange range = injectionHostToUse.get().getTextRange();

                int childOffset = range.getStartOffset();
                if (startOffset != 0) {
                    final ASTNode leaf = injectionHostToUse.get().findLeafElementAt(startOffset - 1);
                    result.add(createBlockBeforeInjection(leaf, wrap, alignment, indent, new TextRange(childOffset, childOffset + startOffset)));
                }

                addInjectedLanguageBlockWrapper(result, injectedFile[0].getNode(), indent, childOffset + startOffset,
                    new TextRange(prefixLength.get(), injectedFile[0].getTextLength() - suffixLength.get()));

                if (endOffset != injectionHostToUse.get().getTextLength()) {
                    final ASTNode leaf = injectionHostToUse.get().findLeafElementAt(endOffset);
                    result.add(createBlockAfterInjection(leaf, wrap, alignment, indent, new TextRange(childOffset + endOffset, range.getEndOffset())));
                }
                return true;
            }
        }
        return false;
    }

    private List<PsiLanguageInjectionHost.Shred> mergePlacesIntoOne(List<PsiLanguageInjectionHost.Shred> places) {

        final PsiLanguageInjectionHost.Shred mergedShred = new PsiLanguageInjectionHost.Shred() {

            private PsiLanguageInjectionHost.Shred getFirst() {
                return places.get(0);
            }

            private PsiLanguageInjectionHost.Shred getLast() {
                return places.get(places.size() - 1);
            }

            @Nullable
            @Override
            public Segment getHostRangeMarker() {
                if (getFirst().getHostRangeMarker() != null && getLast().getHostRangeMarker() != null) {
                    return new Segment() {
                        @Override
                        public int getStartOffset() {
                            return getFirst().getHostRangeMarker().getStartOffset();
                        }

                        @Override
                        public int getEndOffset() {
                            return getLast().getHostRangeMarker().getEndOffset();
                        }
                    };
                }
                return null;
            }

            @NotNull
            @Override
            public TextRange getRangeInsideHost() {
                return TextRange.create(getFirst().getRangeInsideHost().getStartOffset(), getLast().getRangeInsideHost().getEndOffset());
            }

            @NotNull
            @Override
            public TextRange getRange() {
                return TextRange.create(getFirst().getRange().getStartOffset(), getLast().getRange().getEndOffset());
            }

            @Override
            public boolean isValid() {
                for (PsiLanguageInjectionHost.Shred place : places) {
                    if (!place.isValid()) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void dispose() {
                places.forEach(PsiLanguageInjectionHost.Shred::dispose);
            }

            @Nullable
            @Override
            public PsiLanguageInjectionHost getHost() {
                return getFirst().getHost();
            }

            @NotNull
            @Override
            public String getPrefix() {
                return getFirst().getPrefix();
            }

            @NotNull
            @Override
            public String getSuffix() {
                return getLast().getSuffix();
            }
        };
        return Collections.singletonList(mergedShred);
    }
}
