/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus

import com.intellij.icons.AllIcons
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInspection
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo
import com.intellij.lang.jsgraphql.schema.findElement
import com.intellij.ui.treeStructure.CachingSimpleNode
import com.intellij.ui.treeStructure.SimpleNode

/**
 * Tree node with children for each error in a GraphQL schema
 */
class GraphQLSchemaErrorsListNode(parent: SimpleNode, private val schemaInfo: GraphQLSchemaInfo) :
  CachingSimpleNode(parent) {

  init {
    myName = GraphQLBundle.message("graphql.toolwindow.schema.errors.list.node.name")
    icon = AllIcons.Nodes.Folder
  }

  public override fun buildChildren(): Array<SimpleNode> {
    val children = mutableListOf<SimpleNode>()
    for (error in schemaInfo.getErrors(myProject)) {
      val node = error.node
      if (project != null && error.inspectionClass != null &&
          !GraphQLInspection.isToolEnabled(project, error.inspectionClass!!, node?.findElement(project))
      ) {
        continue
      }
      children.add(GraphQLSchemaErrorNode(this, error))
    }
    if (children.isEmpty()) {
      val noErrors: SimpleNode = object : SimpleNode(this) {
        override fun getChildren(): Array<SimpleNode> {
          return NO_CHILDREN
        }

        override fun getName(): String {
          return GraphQLBundle.message("graphql.toolwindow.schema.errors.list.node.empty.errors")
        }
      }
      noErrors.icon = AllIcons.General.InspectionsOK
      children.add(noErrors)
    }
    return children.toTypedArray()
  }

  override fun isAutoExpandNode(): Boolean {
    return true
  }
}
