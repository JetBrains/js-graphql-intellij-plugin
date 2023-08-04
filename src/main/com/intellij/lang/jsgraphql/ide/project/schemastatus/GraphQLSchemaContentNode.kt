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
import com.intellij.lang.jsgraphql.ide.project.schemastatus.GraphQLTreeNodeNavigationUtil.openSourceLocation
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo
import com.intellij.lang.jsgraphql.types.language.*
import com.intellij.lang.jsgraphql.types.schema.idl.ScalarInfo
import com.intellij.openapi.application.ModalityState
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.treeStructure.CachingSimpleNode
import com.intellij.ui.treeStructure.SimpleNode
import com.intellij.ui.treeStructure.SimpleTree
import org.apache.commons.lang.StringUtils
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
    val registry = validatedSchema.registryInfo.typeDefinitionRegistry
    parts.add("${registry.getTypes(ObjectTypeDefinition::class.java).size} types")
    parts.add("${registry.getTypes(InterfaceTypeDefinition::class.java).size} interfaces")
    parts.add("${registry.getTypes(InputObjectTypeDefinition::class.java).size} inputs")
    parts.add("${registry.getTypes(EnumTypeDefinition::class.java).size} enums")
    parts.add("${registry.getTypes(UnionTypeDefinition::class.java).size} unions")
    parts.add("${(registry.scalars().size - ScalarInfo.GRAPHQL_SPECIFICATION_SCALARS.size)} scalars")
    parts.add("${registry.directiveDefinitions.size} directives")

    myName = "Schema discovery summary"

    val nonEmptyParts = parts.filter { it[0] != '0' }
    if (nonEmptyParts.isNotEmpty()) {
      templatePresentation.locationString = "- " + nonEmptyParts.joinToString()
    }
    else {
      templatePresentation.locationString = "- schema is empty"
    }

    templatePresentation.tooltip = "Double click or press enter to search the schema registry"
    icon = AllIcons.Nodes.ModuleGroup
  }

  override fun handleDoubleClickOrEnter(tree: SimpleTree, inputEvent: InputEvent) {
    val popup = ChooseByNamePopup.createPopup(
      myProject,
      object : SimpleChooseByNameModel(myProject, "Search schema registry \"${parent.name}\"", null) {
        override fun getNames(): Array<String> {
          val names: MutableList<String> = mutableListOf()
          val registry = validatedSchema.registryInfo.typeDefinitionRegistry
          registry.types().values.forEach { names.add(it.name) }
          registry.scalars().values.forEach { names.add(it.name) }
          registry.directiveDefinitions.values.forEach { names.add(it.name) }
          return names.toTypedArray()
        }

        override fun getElementsByName(name: String, pattern: String): Array<Any> {
          val registry = validatedSchema.registryInfo.typeDefinitionRegistry
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
              sourceName = StringUtils.substringAfterLast(sourceName, "/")
              append(" - $sourceName", SimpleTextAttributes.GRAYED_ATTRIBUTES)
            }
          }
        }

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
    }, ModalityState.NON_MODAL, false)
  }

  public override fun buildChildren(): Array<SimpleNode> {
    return NO_CHILDREN
  }

  override fun isAlwaysLeaf(): Boolean {
    return true
  }
}
