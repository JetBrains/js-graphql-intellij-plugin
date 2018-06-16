/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.endpoints;

import com.google.common.collect.Maps;

import java.util.Map;

public class JSGraphQLEndpoint {

    public String name;
    public String url;
    public Map<String, Object> options;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JSGraphQLEndpoint that = (JSGraphQLEndpoint) o;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return !(url != null ? !url.equals(that.url) : that.url != null);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Reuses an existing endpoint by applying new properties to it
     */
    public void withPropertiesFrom(JSGraphQLEndpoint other) {
        name = other.name;
        url = other.url;
        options = other.options != null ? Maps.newLinkedHashMap(other.options) : Maps.newLinkedHashMap();
    }
}
