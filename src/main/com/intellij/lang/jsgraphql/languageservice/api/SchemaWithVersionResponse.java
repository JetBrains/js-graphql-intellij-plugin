/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.languageservice.api;

public class SchemaWithVersionResponse {

    private String schema;

    private String queryType;
    private String mutationType;
    private String subscriptionType;
    private String url;
    private int version;

    public String getSchema() {
        return schema;
    }

    public String getQueryType() {
        return queryType;
    }

    public String getMutationType() {
        return mutationType;
    }

    public String getSubscriptionType() {
        return subscriptionType;
    }

    public String getUrl() {
        return url;
    }

    public int getVersion() {
        return version;
    }
}
