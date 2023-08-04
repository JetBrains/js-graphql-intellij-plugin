package com.intellij.lang.jsgraphql.ide.project.schemastatus

import com.intellij.openapi.project.Project
import com.intellij.ui.treeStructure.SimpleTreeStructure

class GraphQLSchemaTreeStructure(project: Project) : SimpleTreeStructure() {

  private val root = GraphQLSchemasRootNode(project)

  override fun getRootElement() = root
}
