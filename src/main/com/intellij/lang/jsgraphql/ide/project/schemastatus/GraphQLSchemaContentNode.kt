/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus

import com.intellij.icons.AllIcons
import com.intellij.ide.util.gotoByName.ChooseByNamePopup
import com.intellij.ide.util.gotoByName.ChooseByNamePopupComponent
import com.intellij.ide.util.gotoByName.SimpleChooseByNameModel
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.ide.project.schemastatus.GraphQLTreeNodeNavigationUtil.openSourceLocation
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo
import com.intellij.lang.jsgraphql.types.language.*
import com.intellij.lang.jsgraphql.types.schema.idl.ScalarInfo
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.util.NlsSafe
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.treeStructure.CachingSimpleNode
import com.intellij.ui.treeStructure.SimpleNode
import com.intellij.ui.treeStructure.SimpleTree
import java.awt.event.InputEvent
import javax.swing.JList
import javax.swing.ListCellRenderer

/**
 * Tree node which provides schema statistics
 */
class GraphQLSchemaContentNode(parent: SimpleNode, private val validatedSchema: GraphQLSchemaInfo) :
  CachingSimpleNode(parent) {

  init {
    val parts: MutableList<String> = mutableListOf()
    val registry = validatedSchema.registry
    parts.add(GraphQLBundle.message(
      "graphql.toolwindow.schema.content.types.count",
      registry.getTypes(ObjectTypeDefinition::class.java).size
    ))
    parts.add(GraphQLBundle.message(
      "graphql.toolwindow.schema.content.interfaces.count",
      registry.getTypes(InterfaceTypeDefinition::class.java).size
    ))
    parts.add(GraphQLBundle.message(
      "graphql.toolwindow.schema.content.inputs.count",
      registry.getTypes(InputObjectTypeDefinition::class.java).size
    ))
    parts.add(GraphQLBundle.message(
      "graphql.toolwindow.schema.content.enums.count",
      registry.getTypes(EnumTypeDefinition::class.java).size
    ))
    parts.add(GraphQLBundle.message(
      "graphql.toolwindow.schema.content.unions.count",
      registry.getTypes(UnionTypeDefinition::class.java).size
    ))
    parts.add(GraphQLBundle.message(
      "graphql.toolwindow.schema.content.scalars.count",
      registry.scalars().size - ScalarInfo.GRAPHQL_SPECIFICATION_SCALARS.size
    ))
    parts.add(GraphQLBundle.message(
      "graphql.toolwindow.schema.content.directives.count",
      registry.directiveDefinitions.size
    ))

    myName = GraphQLBundle.message("graphql.toolwindow.schema.content.node.name")

    val nonEmptyParts = parts.filter { it[0] != '0' }
    val locationDelimiter = "- "
    if (nonEmptyParts.isNotEmpty()) {
      templatePresentation.locationString = locationDelimiter + nonEmptyParts.joinToString()
    }
    else {
      templatePresentation.locationString = locationDelimiter + GraphQLBundle.message(
        "graphql.toolwindow.schema.content.empty.node.tooltip")
    }

    templatePresentation.tooltip = GraphQLBundle.message("graphql.tooltip.search.schema.registry")
    icon = AllIcons.Nodes.ModuleGroup
  }

  override fun handleDoubleClickOrEnter(tree: SimpleTree, inputEvent: InputEvent) {
    val popup = ChooseByNamePopup.createPopup(
      myProject,
      object : SimpleChooseByNameModel(myProject, GraphQLBundle.message("graphql.search.schema.registry.0", parent.name), null) {
        override fun getNames(): Array<String> {
          val names: MutableList<String> = mutableListOf()
          val registry = validatedSchema.registry
          registry.types().values.forEach { names.add(it.name) }
          registry.scalars().values.forEach { names.add(it.name) }
          registry.directiveDefinitions.values.forEach { names.add(it.name) }
          return names.toTypedArray()
        }

        override fun getElementsByName(name: String, pattern: String): Array<Any> {
          val registry = validatedSchema.registry
          val type = registry.getType(name)
          if (type.isPresent) {
            return arrayOf(type.get())
          }
          val scalarTypeDefinition = registry.scalars()[name]
          if (scalarTypeDefinition != null) {
            return arrayOf(scalarTypeDefinition)
          }
          val directiveDefinition = registry.getDirectiveDefinition(name)
          return if (directiveDefinition.isPresent) {
            arrayOf(directiveDefinition.get())
          }
          else {
            arrayOf(name)
          }
        }

        override fun getListCellRenderer(): ListCellRenderer<*> {
          return object : ColoredListCellRenderer<Any?>() {
            override fun customizeCellRenderer(
              list: JList<*>,
              value: Any?,
              index: Int,
              selected: Boolean,
              hasFocus: Boolean,
            ) {
              val elementName = value?.let { getElementName(it) } ?: return
              append(elementName)
              if (value !is AbstractNode<*>) return
              var sourceName = value.sourceLocation?.sourceName ?: return
              sourceName = sourceName.substringAfterLast("/")
              append(" - $sourceName", SimpleTextAttributes.GRAYED_ATTRIBUTES)
            }
          }
        }

        @NlsSafe
        override fun getElementName(element: Any): String? {
          return if (element is NamedNode<*>) element.name else null
        }
      },
      null
    )

    popup.invoke(object : ChooseByNamePopupComponent.Callback() {
      override fun elementChosen(element: Any) {
        if (element is AbstractNode<*>) {
          val sourceLocation = element.sourceLocation
          if (sourceLocation != null && sourceLocation.sourceName != null) {
            openSourceLocation(myProject, sourceLocation, true)
          }
        }
      }
    }, ModalityState.nonModal(), false)
  }

  public override fun buildChildren(): Array<SimpleNode> {
    return NO_CHILDREN
  }

  override fun isAlwaysLeaf(): Boolean {
    return true
  }
}
