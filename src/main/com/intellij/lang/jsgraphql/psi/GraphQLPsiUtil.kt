@file:JvmName("GraphQLPsiUtil")
/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi

import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.originalFileOrSelf
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.SmartList


fun findContainingTypeNameIdentifier(psiElement: PsiElement?): PsiElement? {
  if (psiElement == null) {
    return null
  }
  val typeOwner = PsiTreeUtil.getParentOfType(
    psiElement,
    GraphQLNamedTypeDefinition::class.java,
    GraphQLNamedTypeExtension::class.java
  )

  return when (typeOwner) {
    is GraphQLNamedTypeDefinition -> typeOwner.typeNameDefinition?.nameIdentifier
    is GraphQLNamedTypeExtension -> typeOwner.typeName?.nameIdentifier
    else -> null
  }
}

fun findContainingTypeName(psiElement: PsiElement?): String? =
  findContainingTypeNameIdentifier(psiElement)?.text

fun getPhysicalVirtualFile(virtualFile: VirtualFile?): VirtualFile? {
  var result = virtualFile?.originalFileOrSelf() ?: return null
  if (result is VirtualFileWindow) {
    // injected virtual files
    result = (result as VirtualFileWindow).delegate
  }
  return result
}

fun getPhysicalVirtualFile(psiFile: PsiFile?): VirtualFile? {
  return getPhysicalVirtualFile(getOriginalVirtualFile(psiFile))
}

private fun getOriginalVirtualFile(psiFile: PsiFile?): VirtualFile? {
  if (psiFile == null || !psiFile.isValid) {
    return null
  }
  var file = psiFile.virtualFile
  if (file == null) {
    val originalFile = psiFile.originalFile
    if (originalFile !== psiFile && originalFile.isValid) {
      file = originalFile.virtualFile
    }
  }
  return file
}

/**
 * Gets the virtual file system path of a PSI file
 */
fun getPhysicalFileName(psiFile: PsiFile): String {
  val virtualFile = getPhysicalVirtualFile(psiFile)
  return virtualFile?.path ?: psiFile.name
}

fun getLeadingFileComments(file: PsiFile): List<PsiComment> {
  val comments: MutableList<PsiComment> = SmartList()
  var child = file.firstChild
  if (child is PsiWhiteSpace) {
    child = PsiTreeUtil.skipWhitespacesForward(child)
  }
  while (child is PsiComment) {
    comments.add(child)
    child = PsiTreeUtil.skipWhitespacesForward(child)
  }
  return comments
}

fun skipDeclarationDescription(element: PsiElement): PsiElement {
  if (element is GraphQLDescriptionAware) {
    val description = element.description
    if (description != null) {
      val target = PsiTreeUtil.skipWhitespacesForward(description)
      if (target != null) {
        return target
      }
    }
  }
  return element
}
