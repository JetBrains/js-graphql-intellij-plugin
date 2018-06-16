/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.injection;

import com.google.common.collect.Lists;
import com.intellij.lang.ASTNode;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression;
import com.intellij.lang.jsgraphql.v1.JSGraphQLLanguage;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JSGraphQLTemplateFragmentLanguageInjector implements MultiHostInjector {

    private static final ArrayList<Class<JSStringTemplateExpression>> INJECTION_CLASSES = Lists.newArrayList(JSStringTemplateExpression.class);

    public final static ThreadLocal<String> CURRENT_INJECTION_ENVIRONMENT = new ThreadLocal<>();

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {

        final Ref<String> envRef = new Ref<>(null);
        if(JSGraphQLLanguageInjectionUtil.isJSGraphQLLanguageInjectionTarget(context, envRef)) {

            final JSStringTemplateExpression template = (JSStringTemplateExpression)context;

            final TextRange graphQlTextRange = JSGraphQLLanguageInjectionUtil.getGraphQLTextRange(template);
            if(graphQlTextRange.isEmpty()) {
                // all whitespace
                return;
            }

            registrar.startInjecting(JSGraphQLLanguage.INSTANCE);

            final StringBuilder sb = new StringBuilder();
            final TextRange[] stringRanges = template.getStringRanges();
            int stringIndex = 0;
            boolean insideTemplate = false;
            for (ASTNode astNode : template.getNode().getChildren(null)) {
                if(astNode.getElementType() == JSTokenTypes.BACKQUOTE) {
                    insideTemplate = true;
                    continue;
                }
                if(astNode.getElementType() == JSTokenTypes.STRING_TEMPLATE_PART) {
                    registrar.addPlace(sb.toString(), "", (PsiLanguageInjectionHost) template, stringRanges[stringIndex]);
                    stringIndex++;
                    sb.setLength(0);
                } else if(insideTemplate) {
                    sb.append(astNode.getText());
                }
            }

            try {
                CURRENT_INJECTION_ENVIRONMENT.set(envRef.get());
                // lexing and parsing occurs inside the next method, allowing us to access the env from the thread local
                registrar.doneInjecting();
            } finally {
                CURRENT_INJECTION_ENVIRONMENT.remove();
            }
        }
    }

    @NotNull
    @Override
    public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return INJECTION_CLASSES;
    }
}
