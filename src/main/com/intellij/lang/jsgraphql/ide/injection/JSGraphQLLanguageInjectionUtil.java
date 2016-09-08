/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.injection;

import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression;
import com.intellij.lang.jsgraphql.psi.JSGraphQLFile;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.Nullable;

public class JSGraphQLLanguageInjectionUtil {

    public static final String RELAY_QL_TEMPLATE_TAG = "Relay.QL";
    public static final String GRAPHQL_TEMPLATE_TAG = "graphql";
    public static final String APOLLO_GQL_TEMPLATE_TAG = "gql";

    public enum JSGraphQLInjectionTag {
        RelayQL,
        GraphQL
    }

    public static boolean isJSGraphQLLanguageInjectionTarget(PsiElement host) {
        return isJSGraphQLLanguageInjectionTarget(host, null);
    }

    public static boolean isRelayInjection(PsiFile file) {
        if(file instanceof JSFile) {
            return true;
        } else if (file instanceof JSGraphQLFile) {
            final Ref<JSGraphQLInjectionTag> tag = new Ref<>();
            return file.getContext() != null && isJSGraphQLLanguageInjectionTarget(file.getContext(), tag) && tag.get() == JSGraphQLInjectionTag.RelayQL;
        }
        return false;
    }

    public static boolean isJSGraphQLLanguageInjectionTarget(PsiElement host, @Nullable Ref<JSGraphQLInjectionTag> tagRef) {
        if(host instanceof JSStringTemplateExpression && host instanceof PsiLanguageInjectionHost) {
            JSStringTemplateExpression template = (JSStringTemplateExpression) host;
            // check if we're a Relay.QL or graphql tagged template
            final PsiElement firstChild = template.getFirstChild();
            if (firstChild instanceof JSReferenceExpression) {
                final String tagText = firstChild.getText();
                if (RELAY_QL_TEMPLATE_TAG.equals(tagText) || GRAPHQL_TEMPLATE_TAG.equals(tagText) || APOLLO_GQL_TEMPLATE_TAG.equals(tagText)) {
                    if(tagRef != null) {
                        tagRef.set(RELAY_QL_TEMPLATE_TAG.equals(tagText) ? JSGraphQLInjectionTag.RelayQL : JSGraphQLInjectionTag.GraphQL);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static TextRange getGraphQLTextRange(JSStringTemplateExpression template) {
        int start = 0;
        int end = 0;
        final TextRange[] stringRanges = template.getStringRanges();
        for (TextRange textRange : stringRanges) {
            if(start == 0) {
                start = textRange.getStartOffset();
            }
            end = textRange.getEndOffset();
        }
        return TextRange.create(start, end);
    }
}
