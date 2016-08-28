/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql;

import com.google.common.collect.Sets;

import java.util.Set;

public interface JSGraphQLKeywords {

    // graphql
    String QUERY = "query";
    String FRAGMENT = "fragment";
    String FRAGMENT_DOTS = "...";
    String FRAGMENT_ON = "on";
    String MUTATION = "mutation";
    String SUBSCRIPTION = "subscription";

    // graphql schema
    String TYPE = "type";
    String INTERFACE = "interface";
    String UNION = "union";
    String SCALAR = "scalar";
    String ENUM = "enum";
    String INPUT = "input";
    String EXTEND = "extend";
    String SCHEMA = "schema";
    String DIRECTIVE = "directive";

    Set<String> ALL = Sets.newHashSet(
            QUERY,
            FRAGMENT,
            FRAGMENT_DOTS,
            FRAGMENT_ON,
            MUTATION,
            SUBSCRIPTION,
            TYPE,
            INTERFACE,
            UNION,
            SCALAR,
            ENUM,
            INPUT,
            EXTEND,
            SCHEMA,
            DIRECTIVE
    );

    Set<String> GRAPHQL_ROOT_KEYWORDS = Sets.newHashSet(
            QUERY,
            FRAGMENT,
            MUTATION,
            SUBSCRIPTION
    );

    Set<String> SCHEMA_DEFINITION_KEYWORDS = Sets.newHashSet(
            TYPE,
            INTERFACE,
            UNION,
            SCALAR,
            ENUM,
            INPUT,
            EXTEND,
            SCHEMA,
            DIRECTIVE
    );
}
