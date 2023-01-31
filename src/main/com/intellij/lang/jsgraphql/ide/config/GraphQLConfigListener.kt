/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.graphqlconfig;

/**
 * Events relating to GraphQL configuration (graphql-config .graphqlconfig files)
 */
public interface GraphQLConfigurationListener {

    /**
     * One or more changes occurred in the .graphqlconfig files
     *
     * @see GraphQLConfigManager
     */
    void onConfigurationChanged();

}
