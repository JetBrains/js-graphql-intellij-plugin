/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.injection.javascript;

import com.google.common.collect.Sets;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecma6.ES6TaggedTemplateExpression;
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression;
import com.intellij.lang.jsgraphql.ide.injection.GraphQLCommentBasedInjectionHelper;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.regex.Pattern;

public class GraphQLLanguageInjectionUtil {
    private static final String GRAPHQL_EOL_COMMENT = "#graphql";
    // Min length for injection - `#graphql`
    private static final int GRAPHQL_EOL_COMMENT_MIN_TEMPLATE_LENGTH = 10;

    private static final Pattern GRAPHQL_C_STYLE_COMMENT_PATTERN = Pattern.compile("/\\*\\s*GraphQL\\s*\\*/");

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

        // gql``, Relay.QL``, etc
        JSStringTemplateExpression template = (JSStringTemplateExpression) host;
        if (isInjectedUsingTemplateTag(template)) {
            return true;
        }

        // built-in "language=GraphQL" injection comments
        if (isInjectedUsingBuiltInComments(template)) {
            return true;
        }

        // /* GraphQL */
        if (isInjectedUsingCStyleComment(template)) {
            return true;
        }

        // # graphql
        if (isInjectedUsingCommentInside(template.getText())) {
            return true;
        }

        return false;
    }

    private static boolean isInjectedUsingBuiltInComments(@NotNull PsiElement host) {
        final GraphQLCommentBasedInjectionHelper commentBasedInjectionHelper = GraphQLCommentBasedInjectionHelper.getInstance();
        return commentBasedInjectionHelper != null && commentBasedInjectionHelper.isGraphQLInjectedUsingComment(host);
    }

    private static boolean isInjectedUsingTemplateTag(@NotNull JSStringTemplateExpression template) {
        PsiElement parent = template.getParent();
        if (!(parent instanceof ES6TaggedTemplateExpression)) return false;

        // check if we're a graphql tagged template
        final JSReferenceExpression tagExpression = PsiTreeUtil.getPrevSiblingOfType(template, JSReferenceExpression.class);
        if (tagExpression == null) {
            return false;
        }

        final String tagText = tagExpression.getText();
        if (SUPPORTED_TAG_NAMES.contains(tagText)) {
            return true;
        }

        final String builderTailName = tagExpression.getReferenceName();
        // a builder pattern that ends in a tagged template, e.g. someQueryAPI.graphql``
        return builderTailName != null && SUPPORTED_TAG_NAMES.contains(builderTailName);
    }

    /**
     * Checks a case when the possible injection contains a EOL comment "# graphql".
     */
    private static boolean isInjectedUsingCommentInside(@NotNull String text) {
        int length = text.length();
        if (length < GRAPHQL_EOL_COMMENT_MIN_TEMPLATE_LENGTH) return false;

        int offset = 0;
        if (!StringUtil.isChar(text, offset++, '`')) return false;
        offset = CharArrayUtil.shiftForward(text, offset, " \n");
        if (offset >= length) return false;
        return text.startsWith(GRAPHQL_EOL_COMMENT, offset);
    }

    private static boolean isInjectedUsingCStyleComment(@NotNull JSStringTemplateExpression template) {
        PsiElement initialElement = template;

        PsiElement parent = template.getParent();
        if (parent instanceof ES6TaggedTemplateExpression) {
            initialElement = parent;
        }

        PsiElement element = PsiTreeUtil.skipWhitespacesBackward(initialElement);
        if (PsiUtilCore.getElementType(element) != JSTokenTypes.C_STYLE_COMMENT) {
            return false;
        }

        return GRAPHQL_C_STYLE_COMMENT_PATTERN.matcher(element.getText()).matches();
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
