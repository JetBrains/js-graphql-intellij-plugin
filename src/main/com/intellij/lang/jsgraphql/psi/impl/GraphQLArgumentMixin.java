/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.psi.GraphQLArgument;
import com.intellij.lang.jsgraphql.psi.GraphQLDirective;
import com.intellij.lang.jsgraphql.psi.GraphQLField;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeScopeProvider;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaUtil;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldsContainer;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public abstract class GraphQLArgumentMixin extends GraphQLNamedElementImpl implements GraphQLArgument, GraphQLTypeScopeProvider {
  public GraphQLArgumentMixin(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public GraphQLType getTypeScope() {
    final GraphQLSchema schema = GraphQLSchemaProvider.getInstance(getProject()).getSchemaInfo(this).getSchema();
    final String argumentName = getName();
    if (argumentName != null) {
      // the type scope for an argument is the argument definition type in a field or directive definition
      final GraphQLDirective directive = PsiTreeUtil.getParentOfType(this, GraphQLDirective.class);
      if (directive != null) {
        final String directiveName = directive.getName();
        if (directiveName != null) {
          final com.intellij.lang.jsgraphql.types.schema.GraphQLDirective schemaDirective = schema.getFirstDirective(directiveName);
          if (schemaDirective != null) {
            final com.intellij.lang.jsgraphql.types.schema.GraphQLArgument schemaDirectiveArgument =
              schemaDirective.getArgument(argumentName);
            if (schemaDirectiveArgument != null) {
              return schemaDirectiveArgument.getType();
            }
          }
        }
        return null;
      }
      final GraphQLField field = PsiTreeUtil.getParentOfType(this, GraphQLField.class);
      final GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(field, GraphQLTypeScopeProvider.class);
      if (field != null && typeScopeProvider != null) {
        GraphQLType typeScope = typeScopeProvider.getTypeScope();
        if (typeScope != null) {
          typeScope = GraphQLSchemaUtil.getUnmodifiedType(typeScope); // unwrap list, non-null since we want a specific field
          if (typeScope instanceof GraphQLFieldsContainer) {
            final GraphQLFieldDefinition fieldDefinition = ((GraphQLFieldsContainer)typeScope).getFieldDefinition(field.getName());
            if (fieldDefinition != null) {
              com.intellij.lang.jsgraphql.types.schema.GraphQLArgument argumentDefinition = fieldDefinition.getArgument(argumentName);
              if (argumentDefinition != null) {
                return argumentDefinition.getType();
              }
            }
          }
        }
      }
    }

    return null;
  }
}
