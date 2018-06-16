/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.configuration;

import com.google.common.collect.Lists;
import com.intellij.lang.jsgraphql.v1.ide.endpoints.JSGraphQLEndpoint;

import java.util.List;

public class JSGraphQLConfiguration {

    public List<JSGraphQLEndpoint> endpoints = Lists.newArrayList();

    public JSGraphQLSchemaConfiguration schema;

}
