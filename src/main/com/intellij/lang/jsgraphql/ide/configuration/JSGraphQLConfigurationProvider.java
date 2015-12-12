/**
 *  Copyright (c) 2015, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.configuration;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.lang.jsgraphql.ide.endpoints.JSGraphQLEndpoint;
import com.intellij.lang.jsgraphql.ide.notifications.JSGraphQLConfigEditorNotificationProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class JSGraphQLConfigurationProvider extends VirtualFileAdapter {

    private final static Logger log = Logger.getInstance(JSGraphQLConfigurationProvider.class);

    private Consumer<List<JSGraphQLEndpoint>> onEndpointsChanged;
    private Project myProject;
    private List<JSGraphQLEndpoint> endpoints;
    private final Object endpointsLock = new Object();

    public JSGraphQLConfigurationProvider(@NotNull Project myProject, @Nullable Consumer<List<JSGraphQLEndpoint>> onEndpointsChanged) {
        this.myProject = myProject;
        this.onEndpointsChanged = onEndpointsChanged;
        VirtualFileManager.getInstance().addVirtualFileListener(this);
    }

    public List<JSGraphQLEndpoint> getEndpoints() {
        final VirtualFile configFile = myProject.getBaseDir().findChild(JSGraphQLConfigEditorNotificationProvider.GRAPHQL_CONFIG_JSON);
        if(configFile != null) {
            final JSGraphQLConfiguration configuration = getConfiguration(configFile);
            if(configuration != null) {
                synchronized (endpointsLock) {
                    if(endpoints == null) {
                        endpoints = configuration.endpoints;
                    }
                }
                return endpoints;
            }
        }
        return Lists.newArrayList(); // no config yet
    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent event) {
        final VirtualFile file = event.getFile();
        if(JSGraphQLConfigEditorNotificationProvider.GRAPHQL_CONFIG_JSON.equals(file.getName())) {
            if(myProject.isDisposed()) {
                // the project has been disposed, so this instance isn't needed anymore
                VirtualFileManager.getInstance().removeVirtualFileListener(this);
                return;
            }
            if(myProject.getBaseDir().equals(file.getParent())) {
                final JSGraphQLConfiguration configuration = getConfiguration(file);
                if(configuration != null && configuration.endpoints != null) {
                    synchronized (endpointsLock) {
                        if(endpoints == null) {
                            endpoints = configuration.endpoints;
                        } else {
                            for(int i = 0; i < configuration.endpoints.size(); i++) {
                                if(i < endpoints.size()) {
                                    // update existing instances since they may already be selected in open editors
                                    endpoints.get(i).withPropertiesFrom(configuration.endpoints.get(i));
                                } else {
                                    endpoints.add(configuration.endpoints.get(i));
                                }
                            }
                            if(configuration.endpoints.size() < endpoints.size()) {
                                // one or more endpoints deleted
                                endpoints.removeIf(ep -> !configuration.endpoints.contains(ep));
                            }
                        }
                    }
                    if(onEndpointsChanged != null) {
                        onEndpointsChanged.accept(endpoints);
                    }
                }
            }
        }
    }

    private JSGraphQLConfiguration getConfiguration(VirtualFile file) {
        try {
            try(InputStream inputStream = file.getInputStream()) {
                final String json = IOUtils.toString(inputStream, "UTF-8");
                try {
                    return new Gson().fromJson(json, JSGraphQLConfiguration.class);
                } catch (JsonSyntaxException je) {
                    log.warn("Invalid JSON in config file", je);
                }
            }
        } catch (IOException e) {
            log.warn("Unable to read from config file", e);
        }
        return null;
    }
}
