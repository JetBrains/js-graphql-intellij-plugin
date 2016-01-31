/**
 * Copyright (c) 2015, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.rename;

import com.intellij.lang.jsgraphql.psi.JSGraphQLNamedPsiElement;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;

/**
 * Vetoes renaming of JSGraphQLNamedPsiElement since names of types and fields are defined by the schema.
 */
public class JSGraphQLRenameVetoCondition implements Condition<PsiElement> {
    @Override
    public boolean value(PsiElement psiElement) {
        return psiElement instanceof JSGraphQLNamedPsiElement;
    }
}
