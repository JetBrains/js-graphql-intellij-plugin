/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.notifications;

import com.intellij.application.options.ModulesComboBox;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.panels.NonOpaquePanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class JSGraphQLConfigModuleDialog extends DialogWrapper {

    @NotNull
    private final Project project;
    private ModulesComboBox modulesComboBox;

    protected JSGraphQLConfigModuleDialog(@NotNull Project project) {
        super(project);
        this.project = project;
        setTitle("Select GraphQL Configuration Home");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        modulesComboBox = new ModulesComboBox();
        modulesComboBox.setMinimumAndPreferredWidth(300);
        modulesComboBox.fillModules(project);
        if(modulesComboBox.getItemCount() > 0) {
            modulesComboBox.setSelectedIndex(0);
        }
        final NonOpaquePanel panel = new NonOpaquePanel();
        panel.add(modulesComboBox, BorderLayout.NORTH);
        return panel;
    }

    public Module getSelectedModule() {
        return modulesComboBox.getSelectedModule();
    }
}
