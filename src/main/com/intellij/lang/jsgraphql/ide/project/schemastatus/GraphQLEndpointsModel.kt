/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus

import com.intellij.ide.util.PropertiesComponent
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigEndpoint
import org.jdesktop.swingx.combobox.ListComboBoxModel
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener

class GraphQLEndpointsModel(
  list: List<GraphQLConfigEndpoint>,
  propertiesComponent: PropertiesComponent,
) : ListComboBoxModel<GraphQLConfigEndpoint>(
  ArrayList(list)
) {
  init {
    if (list.isNotEmpty()) {
      val defaultSelectedIndex = propertiesComponent.getInt(INDEX_PROPERTY_NAME + configPathPersistenceKey, 0)
      if (defaultSelectedIndex >= 0 && defaultSelectedIndex < list.size) {
        selectedItem = list[defaultSelectedIndex]
      }
    }

    addListDataListener(object : ListDataListener {
      override fun intervalAdded(listDataEvent: ListDataEvent) {}
      override fun intervalRemoved(listDataEvent: ListDataEvent) {}
      override fun contentsChanged(listDataEvent: ListDataEvent) {
        val selectedItem = selectedItem
        if (selectedItem != null) {
          propertiesComponent.setValue(
            INDEX_PROPERTY_NAME + configPathPersistenceKey,
            data.indexOf(selectedItem),
            0
          )
        }
      }
    })
  }

  fun reload(newEndpoints: List<GraphQLConfigEndpoint?>?) {
    if (data != newEndpoints) {
      data.clear()
      if (newEndpoints != null) {
        data.addAll(newEndpoints)
      }
    }
    val selectedItem = selectedItem
    if (selectedItem == null) {
      // default to the first endpoint if one is available
      if (data.isNotEmpty()) {
        setSelectedItem(data[0])
      }
    }
    else {
      // check that the selected endpoint is one of the available ones
      if (!data.contains(selectedItem)) {
        setSelectedItem(if (data.isEmpty()) null else data[0])
      }
    }

    // we have to let components that bind to the model know that the model has been changed
    fireContentsChanged(this, -1, -1)
  }

  private val configPathPersistenceKey: String
    get() = if (size > 0) {
      ":" + getElementAt(0)!!.dir.path
    }
    else {
      ""
    }

  companion object {
    private val INDEX_PROPERTY_NAME = GraphQLEndpointsModel::class.java.name + ".index"
  }
}
