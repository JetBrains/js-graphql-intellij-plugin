/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model;

import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigPackageSet;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * graphql-config endpoint extension
 *
 * See https://github.com/prismagraphql/graphql-config/blob/master/specification.md#configuring-graphql-endpoint
 */
public class GraphQLConfigEndpoint {

    public final String name;

    public final GraphQLConfigPackageSet configPackageSet;

    public String url;

    public Boolean introspect;

    public Map<String, Object> headers;

    public GraphQLConfigEndpoint(@Nullable GraphQLConfigPackageSet configPackageSet, String name, String url) {
        this.configPackageSet = configPackageSet;
        this.name = name;
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphQLConfigEndpoint that = (GraphQLConfigEndpoint) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(configPackageSet, that.configPackageSet) &&
                Objects.equals(url, that.url) &&
                Objects.equals(introspect, that.introspect) &&
                Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, configPackageSet, url, introspect, headers);
    }

    @Override
    public String toString() {
        return name + " - " + url;
    }
}
