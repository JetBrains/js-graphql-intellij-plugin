/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigEndpoint;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GraphQLEndpointsModel extends ListComboBoxModel<GraphQLConfigEndpoint> {

    private final static String INDEX_PROPERTY_NAME = GraphQLEndpointsModel.class.getName() + ".index";

    public GraphQLEndpointsModel(List<GraphQLConfigEndpoint> list, PropertiesComponent propertiesComponent) {
        super(new ArrayList<>(list)); // ensure it's not readonly

        if (!list.isEmpty()) {
            int defaultSelectedIndex = propertiesComponent.getInt(INDEX_PROPERTY_NAME + getConfigPathPersistenceKey(), 0);
            if (defaultSelectedIndex >= 0 && defaultSelectedIndex < list.size()) {
                setSelectedItem(list.get(defaultSelectedIndex));
            }
        }

        this.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent listDataEvent) {
            }

            @Override
            public void intervalRemoved(ListDataEvent listDataEvent) {
            }

            @Override
            public void contentsChanged(ListDataEvent listDataEvent) {
                final GraphQLConfigEndpoint selectedItem = getSelectedItem();
                if (selectedItem != null) {
                    propertiesComponent.setValue(INDEX_PROPERTY_NAME + getConfigPathPersistenceKey(), data.indexOf(selectedItem), 0);
                }
            }
        });
    }

    public void reload(List<GraphQLConfigEndpoint> newEndpoints) {
        if (!Objects.equals(data, newEndpoints)) {
            data.clear();
            if (newEndpoints != null) {
                data.addAll(newEndpoints);
            }
        }

        final GraphQLConfigEndpoint selectedItem = getSelectedItem();

        if (selectedItem == null) {
            // default to the first endpoint if one is available
            if (!data.isEmpty()) {
                setSelectedItem(data.get(0));
            }
        } else {
            // check that the selected endpoint is one of the available ones
            if (!data.contains(selectedItem)) {
                setSelectedItem(data.isEmpty() ? null : data.get(0));
            }
        }

        // we have to let components that bind to the model know that the model has been changed
        this.fireContentsChanged(this, -1, -1);
    }

    private String getConfigPathPersistenceKey() {
        if (getSize() > 0) {
            return ":" + getElementAt(0).getDir().getPath();
        } else {
            return "";
        }
    }
}
