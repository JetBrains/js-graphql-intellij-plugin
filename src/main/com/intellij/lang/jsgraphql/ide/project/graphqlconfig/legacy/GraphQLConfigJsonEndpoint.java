/*
 *  Copyright (c) 2019-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.graphqlconfig.legacy;

import java.util.Map;

/** Legacy "graphql.config.json" endpoint */
public class GraphQLConfigJsonEndpoint {

    public String name;
    public String url;
    public Map<String, Object> options;
    public Boolean postIntrospectionQuery;
}
