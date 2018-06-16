/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.project;

import com.intellij.util.messages.Topic;

import java.util.EventListener;

/**
 * Listener interface for the JS GraphQL Language Service
 */
public interface JSGraphQLLanguageServiceListener extends EventListener {

    Topic<JSGraphQLLanguageServiceListener> TOPIC = new Topic<>(
            "JS GraphQL Language Service Instance Events",
            JSGraphQLLanguageServiceListener.class,
            Topic.BroadcastDirection.TO_PARENT
    );

    /**
     * Invoked when the language service instance process handler receives console output
     * @param text the console text that was received
     */
    void onProcessHandlerTextAvailable(String text);

}
