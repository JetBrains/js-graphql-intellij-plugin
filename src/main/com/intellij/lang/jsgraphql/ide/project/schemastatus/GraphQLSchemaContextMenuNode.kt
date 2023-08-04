package com.intellij.lang.jsgraphql.ide.project.schemastatus

import java.awt.Component

interface GraphQLSchemaContextMenuNode {
  fun handleContextMenu(component: Component?, x: Int, y: Int)
}
