/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model;

import java.util.Map;

/**
 * graphql-config root config
 */
public class GraphQLConfigData extends GraphQLResolvedConfigData {
    public Map<String, GraphQLResolvedConfigData> projects;
}
