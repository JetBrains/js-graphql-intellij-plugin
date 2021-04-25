/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.injection.kotlin;

import com.google.common.collect.Sets;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.jsgraphql.ide.injection.GraphQLCommentBasedInjectionHelper;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.*;

import java.util.Set;

public class GraphQLLanguageInjectionUtil {

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


    public static final String GRAPHQL_ENVIRONMENT = "graphql";
    public static final String GRAPHQL_TEMPLATE_ENVIRONMENT = "graphql-template";
    public static final String RELAY_ENVIRONMENT = "relay";
    public static final String APOLLO_ENVIRONMENT = "apollo";
    public static final String LOKKA_ENVIRONMENT = "lokka";
    public static final String DEFAULT_GQL_ENVIRONMENT = APOLLO_ENVIRONMENT;

    private static final String PROJECT_GQL_ENV = GraphQLLanguageInjectionUtil.class.getName() + ".gql";

    public static boolean isKtGraphQLLanguageInjectionTarget(PsiElement host) {
        return isKtGraphQLLanguageInjectionTarget(host, null);
    }

    public static String getEnvironment(PsiFile file) {
        if (file instanceof KtFile) {
            // for Kt Files we have to check the kind of environment being used
            final Ref<String> envRef = new Ref<>();
            file.accept(new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(PsiElement element) {
                    if (!isKtGraphQLLanguageInjectionTarget(element, envRef)) {
                        // no match yet, so keep visiting
                        super.visitElement(element);
                    }
                }
            });
            final String environment = envRef.get();
            if (environment != null) {
                return environment;
            }
        } else if (file instanceof GraphQLFile) {
            final Ref<String> tag = new Ref<>();
            if (file.getContext() != null && isKtGraphQLLanguageInjectionTarget(file.getContext(), tag)) {
                return tag.get();
            }
        }
        // fallback is traditional GraphQL
        return GRAPHQL_ENVIRONMENT;
    }

    public static boolean isKtGraphQLLanguageInjectionTarget(PsiElement host, @Nullable Ref<String> envRef) {
        if (host instanceof KtStringTemplateExpression) {
            KtStringTemplateExpression template = (KtStringTemplateExpression) host;
            // check if we're a graphql tagged template
            final KtReferenceExpression tagExpression = PsiTreeUtil.getPrevSiblingOfType(template, KtReferenceExpression.class);
            if (tagExpression != null) {
                final String tagText = tagExpression.getText();
                if (SUPPORTED_TAG_NAMES.contains(tagText)) {
                    if (envRef != null) {
                        envRef.set(getEnvironmentFromTemplateTag(tagText, host));
                    }
                    return true;
                }
                final String builderTailName = tagExpression.getText();
                if (builderTailName != null && SUPPORTED_TAG_NAMES.contains(builderTailName)) {
                    // a builder pattern that ends in a tagged template, e.g. someQueryAPI.graphql``
                    if (envRef != null) {
                        envRef.set(getEnvironmentFromTemplateTag(builderTailName, host));
                    }
                    return true;
                }
            }
            // also check for "manual" language=GraphQL injection comments
            final GraphQLCommentBasedInjectionHelper commentBasedInjectionHelper = ServiceManager.getService(GraphQLCommentBasedInjectionHelper.class);
            if (commentBasedInjectionHelper != null) {
                return commentBasedInjectionHelper.isGraphQLInjectedUsingComment(host, envRef);
            }
        }
        return false;
    }

    public static String getEnvironmentFromTemplateTag(String tagText, PsiElement host) {
        if (RELAY_QL_TEMPLATE_TAG.equals(tagText)) {
            return RELAY_ENVIRONMENT;
        }
        if (GRAPHQL_TEMPLATE_TAG.equals(tagText) || GRAPHQL_EXPERIMENTAL_TEMPLATE_TAG.equals(tagText)) {
            if (host instanceof KtStringTemplateExpression) {
                final KtStringTemplateEntry[] arguments = ((KtStringTemplateExpression) host).getEntries();
                if (arguments.length > 0) {
                    // one or more placeholders inside the tagged template text, so consider it templated graphql
                    return GRAPHQL_TEMPLATE_ENVIRONMENT;
                }
            }
            return GRAPHQL_ENVIRONMENT;
        }
        if (GQL_TEMPLATE_TAG.equals(tagText)) {
            return PropertiesComponent.getInstance(host.getProject()).getValue(PROJECT_GQL_ENV, DEFAULT_GQL_ENVIRONMENT);
        }

        if (APOLLO_GQL_TEMPLATE_TAG.equals(tagText)) {
            return APOLLO_ENVIRONMENT;
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

    public static TextRange getGraphQLTextRange(KtStringTemplateExpression template) {
        int start = 0;
        int end = 0;
        final KtStringTemplateEntry[] stringRanges = template.getEntries();
        for (KtStringTemplateEntry textRange : stringRanges) {
            if (start == 0) {
                start = textRange.getTextOffset();
            }
            end = textRange.getTextOffset();
        }
        return TextRange.create(start, end);
    }
}
