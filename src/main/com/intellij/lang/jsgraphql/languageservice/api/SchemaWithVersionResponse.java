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

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public void setMutationType(String mutationType) {
        this.mutationType = mutationType;
    }

    public void setSubscriptionType(String subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
