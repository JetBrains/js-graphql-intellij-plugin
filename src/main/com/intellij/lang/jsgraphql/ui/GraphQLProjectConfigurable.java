/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ui;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.lang.jsgraphql.GraphQLConstants;
import com.intellij.lang.jsgraphql.GraphQLSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * The GraphQL settings page under Languages & Frameworks
 */
public class GraphQLProjectConfigurable implements SearchableConfigurable {

    @NotNull
    @Override
    public String getId() {
        return "settings.jsgraphql";
    }

    @Nls
    @Override
    public String getDisplayName() {
        return GraphQLConstants.GraphQL;
    }

    private final Project myProject;
    private final GraphQLSettings mySettings;
    private volatile GraphQLProjectSettingsForm myForm;

    public GraphQLProjectConfigurable(Project project) {
        this.myProject = project;
        this.mySettings = GraphQLSettings.getSettings(project);
    }

    @Nullable
    public JComponent createComponent() {
        if (myForm == null) {
            myForm = new GraphQLProjectSettingsForm().initialize(mySettings);
        }
        return myForm.getComponent();
    }

    public void apply() throws ConfigurationException {
        if (myForm != null) {
            if (myProject.isDefault()) {
                myForm.apply();
            } else {
                WriteAction.run(() -> {
                    myForm.apply();
                });
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (!myProject.isDisposed()) {
                        DaemonCodeAnalyzer.getInstance(myProject).restart();
                        EditorNotifications.getInstance(myProject).updateAllNotifications();
                    }
                }, myProject.getDisposed());
            }
        }
    }

    public void reset() {
        if (myForm != null) {
            myForm.reset();
        }
    }

    public boolean isModified() {
        return myForm != null && myForm.isModified();
    }
}
