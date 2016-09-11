/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.completion;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementWeigher;
import com.intellij.lang.jsgraphql.JSGraphQLKeywords;
import com.intellij.lang.jsgraphql.JSGraphQLTokenTypes;
import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons;
import com.intellij.lang.jsgraphql.ide.injection.JSGraphQLLanguageInjectionUtil;
import com.intellij.lang.jsgraphql.languageservice.JSGraphQLNodeLanguageServiceClient;
import com.intellij.lang.jsgraphql.languageservice.api.Hint;
import com.intellij.lang.jsgraphql.languageservice.api.HintsResponse;
import com.intellij.lang.jsgraphql.psi.JSGraphQLFragmentDefinitionPsiElement;
import com.intellij.lang.jsgraphql.psi.JSGraphQLNamedTypePsiElement;
import com.intellij.lang.jsgraphql.schema.ide.project.JSGraphQLSchemaLanguageProjectService;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;


public class JSGraphQLCompletionContributor extends CompletionContributor {

    public JSGraphQLCompletionContributor() {

        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {

                final String environment = JSGraphQLLanguageInjectionUtil.getEnvironment(parameters.getOriginalFile());
                final String buffer = parameters.getOriginalFile().getText();
                final Editor editor = parameters.getEditor();
                final Project project = editor.getProject();
                final LogicalPosition logicalPosition = editor.offsetToLogicalPosition(parameters.getOffset());

                final HintsResponse hints = JSGraphQLNodeLanguageServiceClient.getHints(buffer, logicalPosition.line, logicalPosition.column, project, environment);

                if(hints != null) {

                    result = updateResult(parameters, result);

                    // check if the completion was invoked after the '...' fragment spread keyword
                    boolean isFragmentSpreadCompletion = false;
                    PsiElement originalPosition = parameters.getOriginalPosition();
                    if(originalPosition != null) {
                        if(originalPosition instanceof PsiWhiteSpace) {
                            // this is the completion invoked right after '...'
                            final PsiElement prevSibling = originalPosition.getPrevSibling();
                            if(prevSibling != null) {
                                final boolean isKeyword = prevSibling.getNode().getElementType() == JSGraphQLTokenTypes.KEYWORD;
                                if(isKeyword && JSGraphQLKeywords.FRAGMENT_DOTS.equals(prevSibling.getText())) {
                                    isFragmentSpreadCompletion = true;
                                }
                            }
                        } else if(originalPosition.getParent() instanceof JSGraphQLNamedTypePsiElement) {
                            // completion inside the fragment name, e.g. after '...Fra'
                            for (PsiElement child = originalPosition.getParent().getPrevSibling(); child != null; child = child.getPrevSibling()) {
                                if(child instanceof PsiWhiteSpace) {
                                    continue;
                                }
                                final boolean isKeyword = child.getNode().getElementType() == JSGraphQLTokenTypes.KEYWORD;
                                if(isKeyword && JSGraphQLKeywords.FRAGMENT_DOTS.equals(child.getText())) {
                                    isFragmentSpreadCompletion = true;
                                }
                                break;
                            }
                        }
                    }

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
                            if(isFragmentSpreadCompletion) {
                                propertyIcon = JSGraphQLIcons.Schema.Fragment;
                            } else if(JSGraphQLSchemaLanguageProjectService.SCALAR_TYPES.contains(type)) {
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

                        if(JSGraphQLKeywords.ALL.contains(text)) {
                            // add a whitespace after completion of a keyword
                            element = element.withInsertHandler(AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
                        }

                        result.addElement(element);
                    }
                }

            }
        };

        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), provider);

    }

    private List<JSGraphQLFragmentDefinitionPsiElement> getFragmentDefinitions(PsiFile file) {
        List<JSGraphQLFragmentDefinitionPsiElement> ret = Lists.newArrayList();
        file.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if(element instanceof JSGraphQLFragmentDefinitionPsiElement) {
                    ret.add((JSGraphQLFragmentDefinitionPsiElement) element);
                } else {
                    super.visitElement(element);
                }
            }
        });
        return ret;
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