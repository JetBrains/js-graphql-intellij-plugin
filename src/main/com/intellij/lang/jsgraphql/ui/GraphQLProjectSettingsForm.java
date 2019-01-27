/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ui;

import com.intellij.lang.jsgraphql.GraphQLSettings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.fields.ExpandableTextField;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.function.Predicate;

public class GraphQLProjectSettingsForm {

    private JPanel rootPanel;

    // introspection
    private JPanel introspectionPanel;
    private ExpandableTextField introspectionQueryTextField;
    private JCheckBox automaticallyUpdateGraphQLFilesCheckBox;
    JPanel relayModernPanel;
    JCheckBox enableRelayModernCheckBox;

    private GraphQLSettings mySettings;

    GraphQLProjectSettingsForm initialize(GraphQLSettings mySettings) {

        this.mySettings = mySettings;
        introspectionPanel.setBorder(IdeBorderFactory.createTitledBorder("GraphQL Introspection"));
        automaticallyUpdateGraphQLFilesCheckBox.setVisible(false);

        relayModernPanel.setBorder(IdeBorderFactory.createTitledBorder("GraphQL Frameworks"));

        return this;
    }

    JPanel getComponent() {
        return rootPanel;
    }

    void visit(Container container, Predicate<Component> componentPredicate) {
        for (int i = 0; i < container.getComponentCount(); i++) {
            Component child = container.getComponent(i);
            if (componentPredicate.test(child)) {
                return;
            }
            if (child instanceof Container) {
                visit((Container) child, componentPredicate);
            }
        }
    }

    void apply() throws ConfigurationException {
        mySettings.setIntrospectionQuery(introspectionQueryTextField.getText());
        mySettings.setEnableRelayModernFrameworkSupport(enableRelayModernCheckBox.isSelected());
    }

    void reset() {
        introspectionQueryTextField.setText(mySettings.getIntrospectionQuery());
        enableRelayModernCheckBox.setSelected(mySettings.isEnableRelayModernFrameworkSupport());
    }

    boolean isModified() {
        if (!Objects.equals(mySettings.getIntrospectionQuery(), introspectionQueryTextField.getText())) {
            return true;
        }
        if (mySettings.isEnableRelayModernFrameworkSupport() != enableRelayModernCheckBox.isSelected()) {
            return true;
        }
        return false;
    }

}
