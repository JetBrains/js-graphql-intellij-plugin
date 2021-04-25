/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.injection.kotlin;

import com.google.common.collect.Lists;
import com.intellij.lang.ASTNode;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.KtStringTemplateExpression;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.lang.jsgraphql.ide.injection.kotlin.GraphQLLanguageInjectionUtil.isKtGraphQLLanguageInjectionTarget;

public class GraphQLTemplateFragmentLanguageInjector implements MultiHostInjector {

    private static final ArrayList<Class<KtStringTemplateExpression>> INJECTION_CLASSES = Lists.newArrayList(KtStringTemplateExpression.class);

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {


        if(isKtGraphQLLanguageInjectionTarget(context)) {

            final KtStringTemplateExpression template = (KtStringTemplateExpression)context;

            final TextRange graphQlTextRange = GraphQLLanguageInjectionUtil.getGraphQLTextRange(template);
//            if(graphQlTextRange.isEmpty()) {
            // all whitespace
//                return;
//            }

            registrar.startInjecting(GraphQLLanguage.INSTANCE);

            final StringBuilder sb = new StringBuilder();
            final TextRange stringRanges = template.getTextRange();
            int stringIndex = 0;
            boolean insideTemplate = false;
            for (ASTNode astNode : template.getNode().getChildren(null)) {
                if(astNode.getElementType() == KtTokens.OPEN_QUOTE) {
                    insideTemplate = true;
                    continue;
                }
                if(astNode.getElementType() == KtTokens.LONG_TEMPLATE_ENTRY_END) {
//                    registrar.addPlace(sb.toString(), "", (PsiLanguageInjectionHost) template, stringRanges[stringIndex]);
                    stringIndex++;
                    sb.setLength(0);
                } else if(insideTemplate) {
                    sb.append(astNode.getText());
                }
            }

            registrar.doneInjecting();

            // update graphql config notifications
            final VirtualFile virtualFile = context.getContainingFile().getVirtualFile();
            if(virtualFile != null && !ApplicationManager.getApplication().isUnitTestMode()) {
                EditorNotifications.getInstance(context.getProject()).updateNotifications(virtualFile);
            }
        }
    }

    @NotNull
    @Override
    public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return INJECTION_CLASSES;
    }
}
