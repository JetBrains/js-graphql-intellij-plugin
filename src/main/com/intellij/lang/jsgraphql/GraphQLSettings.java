/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.SimpleModificationTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Project-wide GraphQL settings persisted in the .idea folder as graphql-settings.xml
 */
@State(name = "GraphQLSettings", storages = {@Storage("graphql-settings.xml")})
public class GraphQLSettings implements PersistentStateComponent<GraphQLSettings.GraphQLSettingsState> {

    private final Project myProject;

    private final SimpleModificationTracker myModificationTracker = new SimpleModificationTracker();
    private GraphQLSettingsState myState = new GraphQLSettingsState();

    public GraphQLSettings(@NotNull Project project) {
        myProject = project;
    }

    public static GraphQLSettings getSettings(Project project) {
        return ServiceManager.getService(project, GraphQLSettings.class);
    }

    @Nullable
    @Override
    public GraphQLSettings.GraphQLSettingsState getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull GraphQLSettings.GraphQLSettingsState state) {
        myState = state;
        settingsChanged();
    }

    public ModificationTracker getModificationTracker() {
        return myModificationTracker;
    }

    private void settingsChanged() {
        myModificationTracker.incModificationCount();
    }

    /* Introspection */

    public String getIntrospectionQuery() {
        return myState.introspectionQuery;
    }

    public String getOperationName() {
        return myState.operationName;
    }

    public void setIntrospectionQuery(String introspectionQuery) {
        myState.introspectionQuery = introspectionQuery;
        settingsChanged();
    }

    public void setOperationName(String operationName) {
        myState.operationName = operationName;
        settingsChanged();
    }

    public boolean isEnableIntrospectionDefaultValues() {
        return myState.enableIntrospectionDefaultValues;
    }

    public void setEnableIntrospectionDefaultValues(boolean enableIntrospectionDefaultValues) {
        myState.enableIntrospectionDefaultValues = enableIntrospectionDefaultValues;
        settingsChanged();
    }

    public boolean isEnableIntrospectionRepeatableDirectives() {
        return myState.enableIntrospectionRepeatableDirectives;
    }

    public void setEnableIntrospectionRepeatableDirectives(boolean enableIntrospectionRepeatableDirectives) {
        myState.enableIntrospectionRepeatableDirectives = enableIntrospectionRepeatableDirectives;
        settingsChanged();
    }

    public boolean isOpenEditorWithIntrospectionResult() {
        return myState.openEditorWithIntrospectionResult;
    }

    public void setOpenEditorWithIntrospectionResult(boolean openEditorWithIntrospectionResult) {
        myState.openEditorWithIntrospectionResult = openEditorWithIntrospectionResult;
        settingsChanged();
    }

    /* Frameworks */

    public boolean isRelaySupportEnabled() {
        return myState.enableRelayModernFrameworkSupport;
    }

    public void setRelaySupportEnabled(boolean enableRelayModernFrameworkSupport) {
        myState.enableRelayModernFrameworkSupport = enableRelayModernFrameworkSupport;
        settingsChanged();
    }

    public boolean isFederationSupportEnabled() {
        return myState.enableFederationSupport;
    }

    public void setFederationSupportEnabled(boolean enableFederationSupport) {
        myState.enableFederationSupport = enableFederationSupport;
        settingsChanged();
    }

    public boolean isOperationNameEnabled() {
        return myState.enableOperationName;
    }

    public void setOperationNameEnabled(boolean enableOperationName) {
        myState.enableOperationName = enableOperationName;
        settingsChanged();
    }

    /**
     * The state class that is persisted as XML
     * NOTE!!!: 1. Class must be static, and 2. Fields must be public for settings serialization to work
     */
    public static class GraphQLSettingsState {
        public String introspectionQuery = "";
        public String operationName = "";
        public boolean enableIntrospectionDefaultValues = true;
        public boolean enableIntrospectionRepeatableDirectives = false;
        public boolean openEditorWithIntrospectionResult = true;

        public boolean enableRelayModernFrameworkSupport;
        public boolean enableFederationSupport = false;
        public boolean enableOperationName = false;
    }
}

