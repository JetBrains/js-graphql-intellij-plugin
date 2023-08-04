/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.findUsages

import com.intellij.lang.jsgraphql.psi.GraphQLReferenceElement
import com.intellij.psi.PsiElement
import com.intellij.usages.impl.rules.UsageType
import com.intellij.usages.impl.rules.UsageTypeProvider

/**
 * Shows field access as "read" in the find usages panel
 */
class GraphQLUsageTypeProvider : UsageTypeProvider {
  override fun getUsageType(element: PsiElement): UsageType? {
    return if (element is GraphQLReferenceElement) UsageType.READ else null
  }
}
