/**
 *  Copyright (c) 2015, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.completion;

import com.google.common.collect.Sets;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementWeigher;
import com.intellij.icons.AllIcons;
import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons;
import com.intellij.lang.jsgraphql.ide.injection.JSGraphQLLanguageInjectionUtil;
import com.intellij.lang.jsgraphql.languageservice.JSGraphQLNodeLanguageServiceClient;
import com.intellij.lang.jsgraphql.languageservice.api.Hint;
import com.intellij.lang.jsgraphql.languageservice.api.HintsResponse;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;
import java.util.Set;


public class JSGraphQLCompletionContributor extends CompletionContributor {

    private static Set<String> SCALAR_TYPES = Sets.newHashSet("String", "String!", "Boolean", "Boolean!", "Int", "Int!", "Float", "Float!", "ID", "ID!");

    public JSGraphQLCompletionContributor() {

        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {

                final boolean relay = JSGraphQLLanguageInjectionUtil.isRelayInjection(parameters.getOriginalFile());
                final String buffer = parameters.getOriginalFile().getText();
                final Editor editor = parameters.getEditor();
                final Project project = editor.getProject();
                final LogicalPosition logicalPosition = editor.offsetToLogicalPosition(parameters.getOffset());

                final HintsResponse hints = JSGraphQLNodeLanguageServiceClient.getHints(buffer, logicalPosition.line, logicalPosition.column, project, relay);

                if(hints != null) {

                    result = updateResult(parameters, result);

                    for (Hint hint : hints.getHints()) {

                        final String text = hint.getText();
                        String type = hint.getType();

                        LookupElementBuilder element = LookupElementBuilder.create(text).withBoldness(true);
                        if(hint.getDescription() != null) {
                            element = element.withTailText(" - " + hint.getDescription(), true);
                        }

                        if(type != null) {

                            // fields with type or built-ins
                            Icon propertyIcon = JSGraphQLIcons.Schema.Field;
                            Icon typeIcon = hint.isRelay() ? JSGraphQLIcons.Logos.Relay : null;
                            if(SCALAR_TYPES.contains(type)) {
                                if(text.equals("true") || text.equals("false")) {
                                    propertyIcon = null;
                                    type = null;
                                    element = element.withTailText(null);
                                } else {
                                    propertyIcon = JSGraphQLIcons.Schema.Scalar;
                                }
                            }
                            element = element.withTypeText(type, typeIcon, false).withIcon(propertyIcon);

                        } else {

                            // types, attributes
                            Icon propertyIcon = null;
                            boolean showType = true;
                            if(text.startsWith("__") || Character.isUpperCase(text.charAt(0))) {
                                // type
                                propertyIcon = JSGraphQLIcons.Schema.Type;
                                showType = false;
                            }

                            element = element.withIcon(propertyIcon);

                            if(showType) {
                                // indicate the type by capitalizing the text
                                String derivedType = String.valueOf(text.charAt(0)).toUpperCase() + text.substring(1);
                                if("{".equals(text)) {
                                    derivedType = "Query"; // anonymous Query type
                                }
                                element = element.withTypeText(derivedType, true);
                            }

                        }
                        result.addElement(element);
                    }
                }

            }
        };

        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), provider);

    }

    @NotNull
    private CompletionResultSet updateResult(CompletionParameters params, @NotNull CompletionResultSet result) {
        CompletionResultSet completionResultSet = result;
        CompletionSorter completionSorter =
                CompletionSorter.defaultSorter(params, completionResultSet.getPrefixMatcher())
                        .weighBefore("priority", new LookupElementWeigher("GraphQLWeight") {
                            @NotNull
                            @Override
                            public Comparable weigh(@NotNull LookupElement element) {
                                return new LookupElementComparator(element);
                            }
                        });
        completionResultSet = completionResultSet.withRelevanceSorter(completionSorter);
        return completionResultSet;
    }

    /**
     * Moves the built-in '__' types to the bottom of the completion list by sorting them with a '|' prefix that has a higher char code value
     */
    private static class LookupElementComparator implements Comparable<LookupElementComparator> {

        private LookupElement lookupElement;

        public LookupElementComparator(LookupElement lookupElement) {
            this.lookupElement = lookupElement;
        }

        @Override
        public int compareTo(LookupElementComparator other) {
            return getSortText(lookupElement).compareTo(getSortText(other.lookupElement));
        }

        private String getSortText(LookupElement element) {
            if(element.getLookupString().startsWith("__")) {
                return "|" + element.getLookupString();
            }
            return element.getLookupString();
        }
    }

}