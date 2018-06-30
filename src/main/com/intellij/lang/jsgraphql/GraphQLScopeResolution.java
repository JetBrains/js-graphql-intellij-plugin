/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql;

/**
 * Represents the scoping rules for how GraphQL references are resolved.
 * The main use case for applying scopes is to have multiple schemas inside a single project.
 */
public enum GraphQLScopeResolution {

    /**
     * All GraphQL definitions in the project are within scope
     */
    ENTIRE_PROJECT,

    /**
     * The scopes configured in "Settings" > "Appearance & Behavior" > "Scopes" are used.
     */
    PROJECT_SCOPES,

    /**
     * The schemaPath and include/exclude globs in .graphqlconfig*.graphqlconfig.yml are used
     */
    GRAPHQL_CONFIG_GLOBS;

}
