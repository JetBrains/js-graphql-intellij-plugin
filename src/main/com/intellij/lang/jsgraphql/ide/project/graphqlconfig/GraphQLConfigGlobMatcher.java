/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.graphqlconfig;

/**
 * Glob Matcher which follows the same semantics as minimatch used by graphql-config
 */
@FunctionalInterface
public interface GraphQLConfigGlobMatcher {

    /**
     * Gets whether the file path matches the specified glob pattern
     *
     * @param filePath the file path relative to the graphql-config file, e.g. "src/queries/query.graphql"
     * @param glob     the glob pattern to match against, e.g. "*.{graphql,jsx}"
     *
     * @return true if the file path and glob matches, false otherwise
     */
    boolean matches(String filePath, String glob);

}
