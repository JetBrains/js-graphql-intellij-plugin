/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.javascript.injection;

import com.google.common.collect.Lists;
import com.intellij.lang.ASTNode;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression;
import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class GraphQLJavaScriptTemplateFragmentLanguageInjector implements MultiHostInjector {

  private static final List<Class<JSStringTemplateExpression>> INJECTION_CLASSES = Lists.newArrayList(JSStringTemplateExpression.class);

  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
    if (!GraphQLJavaScriptLanguageInjectionUtil.isGraphQLLanguageInjectionTarget(context)) {
      return;
    }

    final JSStringTemplateExpression template = (JSStringTemplateExpression)context;

    final TextRange graphQlTextRange = GraphQLJavaScriptLanguageInjectionUtil.getGraphQLTextRange(template);
    if (graphQlTextRange.isEmpty()) {
      // all whitespace
      return;
    }

    registrar.startInjecting(GraphQLLanguage.INSTANCE);

    final StringBuilder sb = new StringBuilder();
    final TextRange[] stringRanges = template.getStringRanges();
    int stringIndex = 0;
    boolean insideTemplate = false;
    for (ASTNode astNode : template.getNode().getChildren(null)) {
      if (astNode.getElementType() == JSTokenTypes.BACKQUOTE) {
        insideTemplate = true;
        continue;
      }
      if (astNode.getElementType() == JSTokenTypes.STRING_TEMPLATE_PART) {
        registrar.addPlace(sb.toString(), "", template, stringRanges[stringIndex]);
        stringIndex++;
        sb.setLength(0);
      }
      else if (insideTemplate) {
        sb.append(astNode.getText());
      }
    }

    registrar.doneInjecting();

    // update graphql config notifications
    final VirtualFile virtualFile = context.getContainingFile().getVirtualFile();
    if (virtualFile != null && !ApplicationManager.getApplication().isUnitTestMode()) {
      EditorNotifications.getInstance(context.getProject()).updateNotifications(virtualFile);
    }
  }

  @Override
  public @NotNull List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return INJECTION_CLASSES;
  }
}
