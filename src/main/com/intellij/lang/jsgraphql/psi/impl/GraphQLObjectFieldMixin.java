/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.psi.GraphQLObjectField;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeScopeProvider;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaUtil;
import com.intellij.lang.jsgraphql.types.schema.GraphQLInputFieldsContainer;
import com.intellij.lang.jsgraphql.types.schema.GraphQLInputObjectField;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public abstract class GraphQLObjectFieldMixin extends GraphQLNamedElementImpl implements GraphQLObjectField, GraphQLTypeScopeProvider {
  public GraphQLObjectFieldMixin(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public GraphQLType getTypeScope() {
    if (getName() != null) {
      // the type scope for an object field the type of the field as defined in the parent type scope
      final GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(this, GraphQLTypeScopeProvider.class);
      if (typeScopeProvider != null) {
        GraphQLType typeScope = typeScopeProvider.getTypeScope();
        if (typeScope != null) {
          typeScope = GraphQLSchemaUtil.getUnmodifiedType(typeScope); // unwrap list, non-null since we want a specific field
          if (typeScope instanceof GraphQLInputFieldsContainer) {
            final GraphQLInputObjectField inputObjectField = ((GraphQLInputFieldsContainer)typeScope).getFieldDefinition(this.getName());
            if (inputObjectField != null) {
              return inputObjectField.getType();
            }
          }
        }
      }
    }

    return null;
  }
}
