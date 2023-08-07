/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.findUsages

import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.asSafely
import com.intellij.lang.jsgraphql.psi.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.jetbrains.annotations.Nls

/**
 * Find usages for named GraphQL PSI elements
 * @see GraphQLNamedElement
 */
class GraphQLFindUsagesProvider : FindUsagesProvider {
  override fun getWordsScanner(): WordsScanner? {
    return null
  }

  override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
    return psiElement.isValid && psiElement is GraphQLElement && psiElement is PsiNamedElement
  }

  override fun getHelpId(psiElement: PsiElement): String {
    return "reference.dialogs.findUsages.other"
  }

  @Nls
  override fun getType(element: PsiElement): String {
    if (element is GraphQLIdentifier) {
      when (element.parent) {
        is GraphQLTypeNameDefinition -> return GraphQLBundle.message("graphql.find.usage.type")
        is GraphQLFieldDefinition -> return GraphQLBundle.message("graphql.find.usage.field")
        is GraphQLInputValueDefinition -> return GraphQLBundle.message("graphql.find.usage.argument")
        is GraphQLFragmentDefinition -> return GraphQLBundle.message("graphql.find.usage.fragment")
        is GraphQLEnumValue -> return GraphQLBundle.message("graphql.find.usage.enum.value")
        is GraphQLDirectiveDefinition -> return GraphQLBundle.message("graphql.find.usage.directive")
      }
    }

    return GraphQLBundle.message("graphql.find.usage.unknown")
  }

  override fun getDescriptiveName(element: PsiElement): String =
    element.parent?.asSafely<PsiNamedElement>()?.name.orEmpty()

  override fun getNodeText(element: PsiElement, useFullName: Boolean): String =
    element.parent.asSafely<PsiNamedElement>()?.name ?: element.text
}
