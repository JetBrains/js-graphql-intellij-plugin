/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageFormatting;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression;
import com.intellij.lang.jsgraphql.v1.ide.injection.JSGraphQLLanguageInjectionUtil;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class JSGraphQLInjectedFormattingModelBuilder implements CustomFormattingModelBuilder {

    private static final Key<Boolean> WANT_DEFAULT_FORMATTER_KEY = Key.<Boolean>create("JSGraphQLInjectedFormattingModelBuilder.wantDefault");

    @NotNull
    @Override
    public FormattingModel createModel(PsiElement element, CodeStyleSettings settings) {
        if(element instanceof JSFile || element.getContainingFile() instanceof JSFile) {
            final JSFile file = (JSFile)(element instanceof JSFile ? element : element.getContainingFile());
            file.putUserData(WANT_DEFAULT_FORMATTER_KEY, true);
            try {
                final FormattingModelBuilder formattingModelBuilder = LanguageFormatting.INSTANCE.forContext(file.getLanguage(), element);
                if (formattingModelBuilder != null) {
                    final FormattingModel model = formattingModelBuilder.createModel(element, settings);
                    final Block rootBlock = model.getRootBlock();
                    return new DelegatingFormattingModel(model, new JSGraphQLBlockWrapper(rootBlock, null, element.getNode(), rootBlock.getWrap(), rootBlock.getAlignment(), createSpaceBuilder(settings, element), settings));
                }
            } finally {
                file.putUserData(WANT_DEFAULT_FORMATTER_KEY, null);
            }
        }
        throw new IllegalArgumentException("Unsupported element '" + element + "'. It must be an element in a JSFile with its own default formatter to support injected GraphQL formatting");
    }

    private static SpacingBuilder createSpaceBuilder(CodeStyleSettings settings, PsiElement element) {
        return new SpacingBuilder(settings, element.getLanguage());
    }

    @Nullable
    @Override
    public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
        return null;
    }

    @Override
    public boolean isEngagedToFormat(PsiElement context) {
        if(context instanceof JSFile) {
            if(Boolean.TRUE.equals(context.getUserData(WANT_DEFAULT_FORMATTER_KEY))) {
                // we're looking up the default formatter at the moment
                return false;
            }
            Collection<JSStringTemplateExpression> templateExpressions = PsiTreeUtil.findChildrenOfType(context, JSStringTemplateExpression.class);
            for (JSStringTemplateExpression templateExpression : templateExpressions) {
                if(JSGraphQLLanguageInjectionUtil.isJSGraphQLLanguageInjectionTarget(templateExpression)) {
                    return true;
                }
            }
        }
        return false;
    }
}