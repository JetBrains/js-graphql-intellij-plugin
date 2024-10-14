@file:JvmName("GraphQLTypeDefinitionUtil")

package com.intellij.lang.jsgraphql.schema

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.jsgraphql.psi.GraphQLElement
import com.intellij.lang.jsgraphql.types.language.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.ex.temp.TempFileSystem
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafElement

fun isExtensionDefinition(definition: SDLDefinition<*>?): Boolean {
  return definition is SchemaExtensionDefinition ||
         definition is InputObjectTypeExtensionDefinition ||
         definition is ObjectTypeExtensionDefinition ||
         definition is InterfaceTypeExtensionDefinition ||
         definition is ScalarTypeExtensionDefinition ||
         definition is UnionTypeExtensionDefinition ||
         definition is EnumTypeExtensionDefinition
}

val GraphQLElement.sourceLocation: SourceLocation
  get() {
    val injectedLanguageManager = InjectedLanguageManager.getInstance(project)
    var file = containingFile
    val isInjected = injectedLanguageManager.isInjectedFragment(file)

    var offset = locationOffset
    if (isInjected) {
      offset = injectedLanguageManager.injectedToHost(this, offset)
      file = injectedLanguageManager.getTopLevelFile(file)
    }

    val document = file?.fileDocument
    if (document == null || offset < 0 || offset > document.textLength) {
      return SourceLocation.EMPTY
    }

    val line = document.getLineNumber(offset)
    val col = offset - document.getLineStartOffset(line)

    return SourceLocation(line, col, file.viewProvider.virtualFile.path)
  }

private val GraphQLElement.locationOffset: Int
  get() {
    return navigationElement.textRange.startOffset
  }

fun Node<*>.findElement(project: Project): PsiElement? = sourceLocation.findElement(project)

fun SourceLocation.findElement(project: Project): PsiElement? {
  if (line == -1 || column == -1 || sourceName.isNullOrEmpty()) return null
  val file = findVirtualFile()?.findPsiFile(project) ?: return null
  val document = file.fileDocument
  val offset = document.getLineStartOffset(line) + column
  var element = InjectedLanguageManager.getInstance(project).findInjectedElementAt(file, offset)
                ?: file.findElementAt(offset)
                ?: return null

  if (element is PsiWhiteSpace || element is LeafElement) {
    element = element.parent
  }

  return element
}

private fun SourceLocation.findVirtualFile(): VirtualFile? {
  var file = LocalFileSystem.getInstance().findFileByPath(sourceName)?.takeIf { it.isValid }
  if (file == null && ApplicationManager.getApplication().isUnitTestMode) {
    file = TempFileSystem.getInstance().findFileByPath(sourceName)
  }
  return file
}