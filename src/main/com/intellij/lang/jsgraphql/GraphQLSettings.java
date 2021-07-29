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

    /**
     * Tracks only the changes which could affect schema building.
     */
    private final SimpleModificationTracker mySchemaSettingsTracker = new SimpleModificationTracker();

    private GraphQLSettingsState myState = new GraphQLSettingsState();

    public static GraphQLSettings getSettings(Project project) {
        return ServiceManager.getService(project, GraphQLSettings.class);
    }

    @Nullable
    @Override
    public GraphQLSettings.GraphQLSettingsState getState() {
        return this.myState;
    }

    @Override
    public void loadState(@NotNull GraphQLSettings.GraphQLSettingsState state) {
        this.myState = state;
    }

    public String getIntrospectionQuery() {
        return myState.introspectionQuery;
    }

    public void setIntrospectionQuery(String introspectionQuery) {
        myState.introspectionQuery = introspectionQuery;
    }

    public boolean isEnableRelayModernFrameworkSupport() {
        return myState.enableRelayModernFrameworkSupport;
    }

    public void setEnableRelayModernFrameworkSupport(boolean enableRelayModernFrameworkSupport) {
        myState.enableRelayModernFrameworkSupport = enableRelayModernFrameworkSupport;
    }

    public boolean isEnableIntrospectionDefaultValues() {
        return myState.enableIntrospectionDefaultValues;
    }

    public void setEnableIntrospectionDefaultValues(boolean enableIntrospectionDefaultValues) {
        mySchemaSettingsTracker.incModificationCount();
        myState.enableIntrospectionDefaultValues = enableIntrospectionDefaultValues;
    }

    public ModificationTracker getSchemaSettingsModificationTracker() {
        return mySchemaSettingsTracker;
    }

    /**
     * The state class that is persisted as XML
     * NOTE!!!: 1. Class must be static, and 2. Fields must be public for settings serialization to work
     */
    static class GraphQLSettingsState {
        public String introspectionQuery = "";
        public boolean enableIntrospectionDefaultValues = true;
        public boolean enableRelayModernFrameworkSupport;
    }
}

