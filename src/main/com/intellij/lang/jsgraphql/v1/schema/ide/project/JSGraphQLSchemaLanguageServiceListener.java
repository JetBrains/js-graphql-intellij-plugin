/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.schema.ide.project;

import com.intellij.util.messages.Topic;

import java.util.EventListener;

/**
 * Listener interface for the JS GraphQL Schema Language Service
 */
public interface JSGraphQLSchemaLanguageServiceListener extends EventListener {

    Topic<JSGraphQLSchemaLanguageServiceListener> TOPIC = new Topic<>(
            "GraphQL Schema Events",
            JSGraphQLSchemaLanguageServiceListener.class,
            Topic.BroadcastDirection.TO_PARENT
    );

    /**
     * Invoked when the GraphQL schema is reloaded
     */
    void onSchemaReloaded();

}
