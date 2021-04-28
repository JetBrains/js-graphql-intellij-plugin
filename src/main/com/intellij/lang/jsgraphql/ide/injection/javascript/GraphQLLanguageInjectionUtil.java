/*
  Copyright (c) 2015-present, Jim Kynde Meyer
  All rights reserved.
  <p>
  This source code is licensed under the MIT license found in the
  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.injection.javascript;

import com.google.common.collect.Sets;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression;
import com.intellij.lang.jsgraphql.ide.injection.GraphQLCommentBasedInjectionHelper;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class GraphQLLanguageInjectionUtil {
    private static final String GRAPHQL_EOL_COMMENT = "#graphql";
    // Min length for injection - `#graphql`
    private static final int GRAPHQL_COMMENT_MIN_TEMPLATE_LENGTH = 10;

    public static final String RELAY_QL_TEMPLATE_TAG = "Relay.QL";
    public static final String GRAPHQL_TEMPLATE_TAG = "graphql";
    public static final String GRAPHQL_EXPERIMENTAL_TEMPLATE_TAG = "graphql.experimental";
    public static final String GQL_TEMPLATE_TAG = "gql";
    public static final String APOLLO_GQL_TEMPLATE_TAG = "Apollo.gql";

    public final static Set<String> SUPPORTED_TAG_NAMES = Sets.newHashSet(
        RELAY_QL_TEMPLATE_TAG,
        GRAPHQL_TEMPLATE_TAG,
        GRAPHQL_EXPERIMENTAL_TEMPLATE_TAG,
        GQL_TEMPLATE_TAG,
        APOLLO_GQL_TEMPLATE_TAG
    );

    public static boolean isGraphQLLanguageInjectionTarget(@Nullable PsiElement host) {
        if (!(host instanceof JSStringTemplateExpression)) {
            return false;
        }

        JSStringTemplateExpression template = (JSStringTemplateExpression) host;
        // check if we're a graphql tagged template
        final JSReferenceExpression tagExpression = PsiTreeUtil.getPrevSiblingOfType(template, JSReferenceExpression.class);
        if (tagExpression != null) {
            final String tagText = tagExpression.getText();
            if (SUPPORTED_TAG_NAMES.contains(tagText)) {
                return true;
            }

            final String builderTailName = tagExpression.getReferenceName();
            if (builderTailName != null && SUPPORTED_TAG_NAMES.contains(builderTailName)) {
                // a builder pattern that ends in a tagged template, e.g. someQueryAPI.graphql``
                return true;
            }
        }

        // also check for "manual" language=GraphQL injection comments
        final GraphQLCommentBasedInjectionHelper commentBasedInjectionHelper = GraphQLCommentBasedInjectionHelper.getInstance();
        if (commentBasedInjectionHelper != null && commentBasedInjectionHelper.isGraphQLInjectedUsingComment(host)) {
            return true;
        }

        if (isInjectedWithCommentInside(template.getText())) {
            return true;
        }

        return false;
    }

    /**
     * Checks a case when the possible injection contains a EOL comment "# graphql".
     */
    private static boolean isInjectedWithCommentInside(@NotNull String text) {
        int length = text.length();
        if (length < GRAPHQL_COMMENT_MIN_TEMPLATE_LENGTH) return false;

        int offset = 0;
        if (!StringUtil.isChar(text, offset++, '`')) return false;
        offset = CharArrayUtil.shiftForward(text, offset, " \n");
        if (offset >= length) return false;
        return text.startsWith(GRAPHQL_EOL_COMMENT, offset);
    }

    public static TextRange getGraphQLTextRange(JSStringTemplateExpression template) {
        int start = 0;
        int end = 0;
        final TextRange[] stringRanges = template.getStringRanges();
        for (TextRange textRange : stringRanges) {
            if (start == 0) {
                start = textRange.getStartOffset();
            }
            end = textRange.getEndOffset();
        }
        return TextRange.create(start, end);
    }
}
