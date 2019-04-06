/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model;

import java.util.List;
import java.util.Map;

/**
 * graphql-config project config
 */
public class GraphQLResolvedConfigData {

    public String name;

    public String schemaPath;

    public List<String> includes;
    public List<String> excludes;

    public Map<String, Object> extensions;

}
