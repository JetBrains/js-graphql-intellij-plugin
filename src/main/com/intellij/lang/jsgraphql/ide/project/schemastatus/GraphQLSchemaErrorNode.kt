/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.ide.project.schemastatus.GraphQLTreeNodeNavigationUtil.openSourceLocation
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInspection
import com.intellij.lang.jsgraphql.schema.GraphQLUnexpectedSchemaError
import com.intellij.lang.jsgraphql.schema.findElement
import com.intellij.lang.jsgraphql.types.GraphQLError
import com.intellij.lang.jsgraphql.types.language.SourceLocation
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiFileFactory
import com.intellij.ui.treeStructure.CachingSimpleNode
import com.intellij.ui.treeStructure.SimpleNode
import com.intellij.ui.treeStructure.SimpleTree
import com.intellij.util.ExceptionUtil
import java.awt.event.InputEvent

/**
 * Tree node for an error in a GraphQL schema
 */
class GraphQLSchemaErrorNode(parent: SimpleNode?, private val error: GraphQLError) : CachingSimpleNode(parent) {
  init {
    myName = error.message

    val location = location
    if (location != null) {
      val tooltip = getTooltip(location)
      if (tooltip != null) {
        templatePresentation.tooltip = tooltip
      }
    }
    else if (error is GraphQLUnexpectedSchemaError) {
      templatePresentation.locationString = " - " + GraphQLBundle.message("graphql.toolwindow.schema.error.node.tooltip")
    }

    setIconFromError(error)
  }

  private fun setIconFromError(error: GraphQLError) {
    var icon = HighlightDisplayLevel.ERROR.icon
    val node = error.node
    if (project != null && error.inspectionClass != null && node != null) {
      val element = node.findElement(project)
      if (element != null) {
        icon = GraphQLInspection.getHighlightDisplayLevel(error.inspectionClass!!, element).icon
      }
    }
    setIcon(icon)
  }

  override fun handleDoubleClickOrEnter(tree: SimpleTree, inputEvent: InputEvent) {
    val location = location
    if (location != null && location.sourceName != null) {
      openSourceLocation(myProject, location, false)
    }
    else if (error is GraphQLUnexpectedSchemaError) {
      val stackTrace = ExceptionUtil.getThrowableText(error.exception)
      val file = PsiFileFactory.getInstance(myProject)
        .createFileFromText("graphql-error.txt", PlainTextLanguage.INSTANCE, stackTrace)
      OpenFileDescriptor(myProject, file.virtualFile).navigate(true)
    }
  }

  public override fun buildChildren(): Array<SimpleNode> {
    return NO_CHILDREN
  }

  override fun isAlwaysLeaf(): Boolean {
    return true
  }

  private val location: SourceLocation?
    get() {
      val locations = error.locations
      return if (locations != null && locations.isNotEmpty()) locations[0] else null
    }

  companion object {
    @NlsSafe
    private fun getTooltip(location: SourceLocation): String? {
      return if (location.sourceName == null || location.line == -1 || location.column == -1) {
        null
      }
      else {
        location.sourceName + ":" + location.line + ":" + location.column
      }
    }
  }
}
