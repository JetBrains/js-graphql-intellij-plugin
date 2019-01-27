/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.graphqlconfig;

import java.util.EventListener;

/**
 * Events relating to GraphQL configuration (graphql-config .graphqlconfig files)
 */
public interface GraphQLConfigFileEventListener extends EventListener {

    /**
     * One or more changes occurred in the .graphqlconfig files
     * @see GraphQLConfigManager
     */
    void onGraphQLConfigurationFileChanged();
}
