/**
 *  Copyright (c) 2015, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.configuration;

import com.google.common.collect.Lists;
import com.intellij.lang.jsgraphql.ide.endpoints.JSGraphQLEndpoint;

import java.util.List;

public class JSGraphQLConfiguration {

    public List<JSGraphQLEndpoint> endpoints = Lists.newArrayList();

}
