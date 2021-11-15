/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.references;

import com.intellij.lang.jsgraphql.psi.GraphQLReferenceElement;
import com.intellij.psi.PsiElement;
import com.intellij.usages.impl.rules.UsageType;
import com.intellij.usages.impl.rules.UsageTypeProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Shows field access as "read" in the find usages panel
 */
public class GraphQLUsageTypeProvider implements UsageTypeProvider {

    @Nullable
    @Override
    public UsageType getUsageType(@NotNull PsiElement element) {
        if (element instanceof GraphQLReferenceElement) {
            return UsageType.READ;
        }
        return null;
    }
}
