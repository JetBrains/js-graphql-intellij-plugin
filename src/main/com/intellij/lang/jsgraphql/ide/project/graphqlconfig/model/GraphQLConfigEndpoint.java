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
 * graphql-config endpoint extension
 *
 * See https://github.com/prismagraphql/graphql-config/blob/master/specification.md#configuring-graphql-endpoint
 */
public class GraphQLConfigEndpoint {

    public String name;

    public String configPath;

    public String url;

    public Map<String, Object> headers;

    public GraphQLConfigEndpoint(String configPath, String name, String url) {
        this.configPath = configPath;
        this.name = name;
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GraphQLConfigEndpoint that = (GraphQLConfigEndpoint) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (configPath != null ? !configPath.equals(that.configPath) : that.configPath != null) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        return headers != null ? headers.equals(that.headers) : that.headers == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (configPath != null ? configPath.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return name + " - " + url;
    }
}
