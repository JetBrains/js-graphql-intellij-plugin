/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.endpoints;

import org.jdesktop.swingx.combobox.ListComboBoxModel;

import java.util.List;

public class JSGraphQLEndpointsModel extends ListComboBoxModel<JSGraphQLEndpoint> {

    public JSGraphQLEndpointsModel(List<JSGraphQLEndpoint> list) {
        super(list);
    }

    public void reload(List<JSGraphQLEndpoint> newEndpoints) {

        if(data != newEndpoints) {
            // once there's a config the endpoint list instance is shared,
            // but in this case we created the model before that, and we need to add the endpoints at this point
            data.clear();
            data.addAll(newEndpoints);
        }

        final JSGraphQLEndpoint selectedItem = getSelectedItem();

        if (selectedItem == null) {
            // default to the first endpoint if one is available
            if(!data.isEmpty()) {
                setSelectedItem(data.get(0));
            }
        } else {
            // check that the selected endpoint is one of the available ones
            if(!data.contains(selectedItem)) {
                setSelectedItem(data.isEmpty() ? null : data.get(0));
            }
        }

        // we have to let components that bind to the model know that the model has been changed
        this.fireContentsChanged(this, -1, -1);
    }
}
