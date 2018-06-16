/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.annotator;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.jsgraphql.v1.JSGraphQLKeywords;
import com.intellij.lang.jsgraphql.v1.JSGraphQLTokenTypes;
import com.intellij.lang.jsgraphql.v1.psi.JSGraphQLElementType;
import com.intellij.lang.jsgraphql.v1.psi.JSGraphQLPsiElement;
import com.intellij.lang.jsgraphql.v1.schema.psi.JSGraphQLSchemaFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Warns about using the GraphQL operations in GraphQL IDL files (.graphqls).
 */
public class JSGraphQLLanguageWarningAnnotator {

    public Annotation annotate(PsiFile file, @NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (file instanceof JSGraphQLSchemaFile) {
            if (element.getNode().getElementType() == JSGraphQLTokenTypes.KEYWORD) {
                if (JSGraphQLKeywords.GRAPHQL_ROOT_KEYWORDS.contains(element.getNode().getText())) {
                    JSGraphQLPsiElement parent = PsiTreeUtil.getParentOfType(element, JSGraphQLPsiElement.class);
                    while (parent != null) {
                        if (JSGraphQLElementType.SCHEMA_DEF_KIND.equals(((JSGraphQLElementType) parent.getNode().getElementType()).getKind())) {
                            // ignore operation keywords inside schema {}
                            return null;
                        }
                        parent = PsiTreeUtil.getParentOfType(parent, JSGraphQLPsiElement.class);
                    }
                    return holder.createWarningAnnotation(element, "Operations and fragments should be written in GraphQL files (.graphql)");
                }
            }
        }
        return null;
    }
}
