/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project;

import com.intellij.ide.projectView.ProjectViewNestingRulesProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Nests GraphQL files created using introspection under their source JSON files.
 */
public final class GraphQLIntrospectionProjectViewNestingRulesProvider implements ProjectViewNestingRulesProvider {

  @Override
  public void addFileNestingRules(@NotNull Consumer consumer) {
    consumer.addNestingRule(".json", ".json.graphql");
  }
}
