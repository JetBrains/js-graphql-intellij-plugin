/**
 *  Copyright (c) 2015, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.annotator;

import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression;
import com.intellij.lang.jsgraphql.ide.injection.JSGraphQLLanguageInjectionUtil;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Resets the text attributes for STRING_TEMPLATE_PART tokens inside a Relay.QL template to default text color.
 */
public class JSGraphQLRelayTemplateAnnotator implements Annotator {

    public static final TextAttributesKey RELAY_TEMPLATE = TextAttributesKey.createTextAttributesKey("JSGRAPHQL_RELAY_TEMPLATE", TextAttributes.ERASE_MARKER);

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {

        if(element instanceof JSStringTemplateExpression) {
            if(JSGraphQLLanguageInjectionUtil.isJSGraphQLLanguageInjectionTarget(element)) {
                for (ASTNode astNode : element.getNode().getChildren(null)) {
                    if (astNode.getElementType() == JSTokenTypes.STRING_TEMPLATE_PART) {
                        final Annotation annotation = holder.createInfoAnnotation(astNode, null);
                        annotation.setTextAttributes(RELAY_TEMPLATE);
                    }
                }
            }
        }
    }
}
