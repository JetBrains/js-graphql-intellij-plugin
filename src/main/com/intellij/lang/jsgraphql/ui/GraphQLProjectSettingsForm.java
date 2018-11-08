/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ui;

import com.google.common.collect.Maps;
import com.intellij.ide.util.scopeChooser.ScopeChooserConfigurable;
import com.intellij.lang.jsgraphql.GraphQLScopeResolution;
import com.intellij.lang.jsgraphql.GraphQLSettings;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.newEditor.SettingsTreeView;
import com.intellij.ui.HoverHyperlinkLabel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.fields.ExpandableTextField;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class GraphQLProjectSettingsForm {

    private JPanel rootPanel;

    private JPanel schemasPanel;
    private JPanel scopedSchemasPanel;

    // schema discovery
    private JRadioButton singleSchemaRadioButton;
    private JRadioButton graphqlConfigSchemasRadioButton;
    private JRadioButton scopedSchemasRadioButton;

    // introspection
    private JPanel introspectionPanel;
    private ExpandableTextField introspectionQueryTextField;
    private JCheckBox automaticallyUpdateGraphQLFilesCheckBox;
    JPanel relayModernPanel;
    JCheckBox enableRelayModernCheckBox;

    private GraphQLSettings mySettings;

    private Map<GraphQLScopeResolution, JRadioButton> scopes = Maps.newHashMap();

    GraphQLProjectSettingsForm initialize(GraphQLSettings mySettings) {

        scopes.put(GraphQLScopeResolution.ENTIRE_PROJECT, singleSchemaRadioButton);
        scopes.put(GraphQLScopeResolution.PROJECT_SCOPES, scopedSchemasRadioButton);
        scopes.put(GraphQLScopeResolution.GRAPHQL_CONFIG_GLOBS, graphqlConfigSchemasRadioButton);

        this.mySettings = mySettings;

        schemasPanel.setBorder(IdeBorderFactory.createTitledBorder("GraphQL Project Structure and Schemas"));

        introspectionPanel.setBorder(IdeBorderFactory.createTitledBorder("GraphQL Introspection"));
        automaticallyUpdateGraphQLFilesCheckBox.setVisible(false);

        relayModernPanel.setBorder(IdeBorderFactory.createTitledBorder("GraphQL Frameworks"));

        final HoverHyperlinkLabel editScopesLink = new HoverHyperlinkLabel("Edit scopes");
        editScopesLink.addHyperlinkListener(hyperlinkEvent -> {

            // focus the scopes radio button and select it
            scopedSchemasRadioButton.requestFocus();
            scopedSchemasRadioButton.setSelected(true);

            // locate the settings tree and select the "Scopes" settings card.
            Container parent = rootPanel.getParent();
            while (parent != null) {
                if (parent instanceof OnePixelSplitter) {
                    visit(parent, component -> {
                        if (component instanceof SettingsTreeView) {
                            SettingsTreeView treeView = (SettingsTreeView) component;
                            try {
                                // have to use package private method not part of the Open SDK
                                final Method findConfigurable = treeView.getClass().getDeclaredMethod("findConfigurable", Class.class);
                                findConfigurable.setAccessible(true);
                                Configurable configurable = (Configurable) findConfigurable.invoke(treeView, ScopeChooserConfigurable.class);
                                if (configurable != null) {
                                    treeView.onSelected(configurable, null);
                                }
                            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
                                // internal API changed
                            }
                            return true;
                        }
                        return false;
                    });
                    return;
                }
                parent = parent.getParent();

            }
        });

        scopedSchemasPanel.add(editScopesLink);

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
        scopes.forEach((graphQLScopeResolution, jRadioButton) -> {
            if (jRadioButton.isSelected()) {
                mySettings.setScopeResolution(graphQLScopeResolution);
            }
        });
        mySettings.setIntrospectionQuery(introspectionQueryTextField.getText());
        mySettings.setEnableRelayModernFrameworkSupport(enableRelayModernCheckBox.isSelected());
    }

    void reset() {
        scopes.get(mySettings.getScopeResolution()).setSelected(true);
        introspectionQueryTextField.setText(mySettings.getIntrospectionQuery());
        enableRelayModernCheckBox.setSelected(mySettings.isEnableRelayModernFrameworkSupport());
    }

    boolean isModified() {
        if (!scopes.get(mySettings.getScopeResolution()).isSelected()) {
            return true;
        }
        if (!Objects.equals(mySettings.getIntrospectionQuery(), introspectionQueryTextField.getText())) {
            return true;
        }
        if (mySettings.isEnableRelayModernFrameworkSupport() != enableRelayModernCheckBox.isSelected()) {
            return true;
        }
        return false;
    }

}
