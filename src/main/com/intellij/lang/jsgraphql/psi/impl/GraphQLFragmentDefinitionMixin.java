/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeName;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeScopeProvider;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import org.jetbrains.annotations.NotNull;

public abstract class GraphQLFragmentDefinitionMixin extends GraphQLNamedElementImpl
  implements GraphQLFragmentDefinition, GraphQLTypeScopeProvider {
  public GraphQLFragmentDefinitionMixin(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public GraphQLType getTypeScope() {
    final GraphQLSchema schema = GraphQLSchemaProvider.getInstance(getProject()).getSchemaInfo(this).getSchema();
    if (getTypeCondition() != null) {
      final GraphQLTypeName typeName = getTypeCondition().getTypeName();
      if (typeName != null) {
        return schema.getType(typeName.getText());
      }
    }
    return null;
  }
}
