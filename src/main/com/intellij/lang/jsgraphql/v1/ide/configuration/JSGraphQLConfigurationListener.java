/*
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.configuration;

import com.intellij.util.messages.Topic;

import java.util.EventListener;

/**
 * Listener interface for the JS GraphQL Configuration Provider
 */
public interface JSGraphQLConfigurationListener extends EventListener {

    Topic<JSGraphQLConfigurationListener> TOPIC = new Topic<>(
            "GraphQL Configuration Events",
            JSGraphQLConfigurationListener.class,
            Topic.BroadcastDirection.TO_PARENT
    );

    /**
     * Invoked when the GraphQL endpoints configuration is changed
     */
    void onEndpointsChanged();

}
