/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.intellij.mock.MockProject;
import com.intellij.openapi.Disposable;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class GraphQLConfigVariableAwareEndpointTest {

    private static final Disposable PARENT_DISPOSABLE = () -> {
    };
    private static final MockProject PROJECT = new MockProject(null, PARENT_DISPOSABLE);

    @Test
    public void getExpandedVariables() {

        GraphQLConfigEndpoint endpoint = new GraphQLConfigEndpoint(null, "", "http://localhost/");
        GraphQLConfigVariableAwareEndpoint variableAwareEndpoint = new GraphQLConfigVariableAwareEndpoint(endpoint, PROJECT);

        // setup env var resolver
        variableAwareEndpoint.GET_ENV_VAR = name -> name + "-value";

        // verify url with no variables
        Assert.assertEquals("http://localhost/", variableAwareEndpoint.getUrl());

        // add a variable and verify it's expanded
        endpoint.url += "${env:test-var}";
        Assert.assertEquals("http://localhost/test-var-value", variableAwareEndpoint.getUrl());

        // verify headers as well as nested header objects are expanded
        endpoint.headers = Maps.newLinkedHashMap();
        endpoint.headers.put("boolean", true);
        endpoint.headers.put("number", 3.14);
        endpoint.headers.put("auth", "$ some value before ${env:auth} ${test");
        HashMap<Object, Object> nested = Maps.newLinkedHashMap();
        nested.put("nested-auth", endpoint.headers.get("auth"));
        endpoint.headers.put("nested", nested);

        Assert.assertEquals(
                "{\"boolean\":true,\"number\":3.14,\"auth\":\"$ some value before auth-value ${test\",\"nested\":{\"nested-auth\":\"$ some value before auth-value ${test\"}}"
                , new Gson().toJson(variableAwareEndpoint.getHeaders())
        );

        // verify that as variables change values, the nested objects are expanded
        // this verifies that the values in the original maps are not overwritten as part of the expansion
        variableAwareEndpoint.GET_ENV_VAR = name -> name + "-new-value";
        Assert.assertEquals(
                "{\"boolean\":true,\"number\":3.14,\"auth\":\"$ some value before auth-new-value ${test\",\"nested\":{\"nested-auth\":\"$ some value before auth-new-value ${test\"}}"
                , new Gson().toJson(variableAwareEndpoint.getHeaders())
        );

    }

}