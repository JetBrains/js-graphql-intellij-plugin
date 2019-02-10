/*
 *  Copyright (c) 2019-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.graphqlconfig.legacy;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * The legacy "graphql.config.json" file format
 */
public class GraphQLConfigJsonConfiguration {

    public GraphQLConfigJsonSchemaConfiguration schema;

    public List<GraphQLConfigJsonEndpoint> endpoints = Lists.newArrayList();

}
