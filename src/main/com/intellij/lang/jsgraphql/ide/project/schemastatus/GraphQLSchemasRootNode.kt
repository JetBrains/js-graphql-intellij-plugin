/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus

import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider.Companion.getInstance
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.ui.treeStructure.SimpleNode

/**
 * Root node in the GraphQL schemas status tree.
 * Has a child node for each schema, and if no schemas have been configured a single node representing the default project-wide schema.
 */
class GraphQLSchemasRootNode(project: Project?) : SimpleNode(project) {
  override fun getChildren(): Array<SimpleNode> {
    return try {
      val provider = getInstance(myProject)
      if (DumbService.getInstance(myProject).isDumb || !provider.isInitialized) {
        // empty the tree view during indexing and until the config has been initialized
        return NO_CHILDREN
      }
      val children = mutableListOf<SimpleNode>()
      for (config in provider.getAllConfigs()) {
        children.add(GraphQLConfigSchemaNode(myProject, this, config, null))
      }
      if (children.isEmpty()) {
        children.add(GraphQLDefaultSchemaNode(myProject, this))
      }
      children.sortBy { it.name }
      children.toTypedArray()
    }
    catch (e: IndexNotReadyException) {
      NO_CHILDREN
    }
  }

  override fun isAutoExpandNode(): Boolean {
    return true
  }
}
