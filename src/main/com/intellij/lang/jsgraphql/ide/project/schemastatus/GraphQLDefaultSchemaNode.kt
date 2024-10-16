/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus

import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.icons.GraphQLIcons
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeProvider
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.ui.treeStructure.CachingSimpleNode
import com.intellij.ui.treeStructure.SimpleNode

/**
 * Tree node that represents the default project schema when no config files exist
 */
class GraphQLDefaultSchemaNode(project: Project, parent: GraphQLSchemasRootNode) : CachingSimpleNode(project, parent) {
  private val schemaInfo: GraphQLSchemaInfo

  init {
    myName = GraphQLBundle.message("graphql.toolwindow.default.schema.node.name")
    presentation.locationString = project.presentableUrl
    presentation.setIcon(GraphQLIcons.Files.GraphQLSchema)

    val globalScope = runReadAction { GraphQLScopeProvider.getInstance(project).globalScope }
    schemaInfo = GraphQLSchemaProvider.getInstance(myProject).getCachedSchemaInfo(globalScope)
  }

  public override fun buildChildren(): Array<SimpleNode> {
    val children: MutableList<SimpleNode> = mutableListOf(GraphQLSchemaContentNode(this, schemaInfo))
    children.add(GraphQLSchemaErrorsListNode(this, schemaInfo))
    children.add(GraphQLSchemaEndpointsListNode(this, null))
    return children.toTypedArray()
  }

  override fun isAutoExpandNode(): Boolean {
    return true
  }

  override fun getEqualityObjects(): Array<Any> {
    return arrayOf("Default schema", schemaInfo)
  }
}
