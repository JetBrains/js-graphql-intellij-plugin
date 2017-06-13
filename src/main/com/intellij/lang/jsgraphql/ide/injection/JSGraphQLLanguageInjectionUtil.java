/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.injection;

import com.google.common.collect.Sets;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression;
import com.intellij.lang.jsgraphql.psi.JSGraphQLFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiRecursiveElementVisitor;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class JSGraphQLLanguageInjectionUtil {

    public static final String RELAY_QL_TEMPLATE_TAG = "Relay.QL";
    public static final String GRAPHQL_TEMPLATE_TAG = "graphql";
    public static final String GRAPHQL_EXPERIMENTAL_TEMPLATE_TAG = "graphql.experimental";
    public static final String GQL_TEMPLATE_TAG = "gql";

    public final static Set<String> SUPPORTED_TAG_NAMES = Sets.newHashSet(
            RELAY_QL_TEMPLATE_TAG,
            GRAPHQL_TEMPLATE_TAG,
            GRAPHQL_EXPERIMENTAL_TEMPLATE_TAG,
            GQL_TEMPLATE_TAG
    );


    public static final String GRAPHQL_ENVIRONMENT = "graphql";
    public static final String RELAY_ENVIRONMENT = "relay";
    public static final String APOLLO_ENVIRONMENT = "apollo";
    public static final String LOKKA_ENVIRONMENT = "lokka";
    public static final String DEFAULT_GQL_ENVIRONMENT = APOLLO_ENVIRONMENT;

    private static final String PROJECT_GQL_ENV = JSGraphQLLanguageInjectionUtil.class.getName() + ".gql";

    public static boolean isJSGraphQLLanguageInjectionTarget(PsiElement host) {
        return isJSGraphQLLanguageInjectionTarget(host, null);
    }

    public static String getEnvironment(PsiFile file) {
        if (file instanceof JSFile) {
            // for JS Files we have to check the kind of environment being used
            final Ref<String> envRef = new Ref<>();
            file.accept(new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(PsiElement element) {
                    if (!isJSGraphQLLanguageInjectionTarget(element, envRef)) {
                        // no match yet, so keep visiting
                        super.visitElement(element);
                    }
                }
            });
            final String environment = envRef.get();
            if (environment != null) {
                return environment;
            }
        } else if (file instanceof JSGraphQLFile) {
            final Ref<String> tag = new Ref<>();
            if (file.getContext() != null && isJSGraphQLLanguageInjectionTarget(file.getContext(), tag)) {
                return tag.get();
            }
        }
        // fallback is traditional GraphQL
        return GRAPHQL_ENVIRONMENT;
    }

    public static boolean isJSGraphQLLanguageInjectionTarget(PsiElement host, @Nullable Ref<String> envRef) {
        if (host instanceof JSStringTemplateExpression && host instanceof PsiLanguageInjectionHost) {
            JSStringTemplateExpression template = (JSStringTemplateExpression) host;
            // check if we're a Relay.QL or graphql tagged template
            JSReferenceExpression tagExpression = null;
            if(template.getFirstChild() instanceof JSReferenceExpression) {
                // up to version 2016.X
                tagExpression = (JSReferenceExpression) template.getFirstChild();
            } else if(template.getPrevSibling() instanceof JSReferenceExpression) {
                // from version 2017.1
                tagExpression = (JSReferenceExpression) template.getPrevSibling();
            }
            if (tagExpression != null) {
                final String tagText = tagExpression.getText();
                if (SUPPORTED_TAG_NAMES.contains(tagText)) {
                    if (envRef != null) {
                        envRef.set(getEnvironmentFromTemplateTag(tagText, host));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static String getEnvironmentFromTemplateTag(String tagText, PsiElement host) {
        if (RELAY_QL_TEMPLATE_TAG.equals(tagText)) {
            return RELAY_ENVIRONMENT;
        }
        if (GRAPHQL_TEMPLATE_TAG.equals(tagText) || GRAPHQL_EXPERIMENTAL_TEMPLATE_TAG.equals(tagText)) {
            return GRAPHQL_ENVIRONMENT;
        }
        if (GQL_TEMPLATE_TAG.equals(tagText)) {
            return PropertiesComponent.getInstance(host.getProject()).getValue(PROJECT_GQL_ENV, DEFAULT_GQL_ENVIRONMENT);
        }
        // fallback
        return GRAPHQL_ENVIRONMENT;
    }

    public static boolean isGQLEnvironmentConfigured(Project project) {
        return !"".equals(PropertiesComponent.getInstance(project).getValue(PROJECT_GQL_ENV, ""));
    }

    public static void setGQLEnvironment(Project project, String env) {
        PropertiesComponent.getInstance(project).setValue(PROJECT_GQL_ENV, env);
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
