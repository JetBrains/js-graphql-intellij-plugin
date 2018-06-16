/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.findUsages;

import com.intellij.lang.jsgraphql.v1.psi.JSGraphQLNamedPropertyPsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.usages.impl.rules.UsageType;
import com.intellij.usages.impl.rules.UsageTypeProvider;
import org.jetbrains.annotations.Nullable;

/**
 * Shows field access as "read" in the find usages panel
 */
public class JSGraphQLUsageTypeProvider implements UsageTypeProvider {

    @Nullable
    @Override
    public UsageType getUsageType(PsiElement element) {
        if(element instanceof JSGraphQLNamedPropertyPsiElement) {
            return UsageType.READ;
        }
        return null;
    }
}
