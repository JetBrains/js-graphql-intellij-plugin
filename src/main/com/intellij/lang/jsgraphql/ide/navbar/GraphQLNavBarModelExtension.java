/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.navbar;

import com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension;
import com.intellij.lang.Language;
import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.lang.jsgraphql.psi.GraphQLArgumentsDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLElement;
import com.intellij.lang.jsgraphql.psi.GraphQLFieldsDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.psi.GraphQLInlineFragment;
import com.intellij.lang.jsgraphql.psi.GraphQLNamedElement;
import com.intellij.lang.jsgraphql.psi.GraphQLObjectTypeDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLScalarTypeDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLSelectionSetOperationDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeCondition;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeNameDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLTypedOperationDefinition;
import com.intellij.openapi.actionSystem.DataMap;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * Adds GraphQL structure elements (operations, fragments, types, fields, ...) to the navigation bar.
 *
 * @see com.intellij.lang.jsgraphql.ide.structureView.GraphQLStructureViewTreeElement
 */
final class GraphQLNavBarModelExtension extends StructureAwareNavBarModelExtension {
  @Override
  protected @NotNull Language getLanguage() {
    return GraphQLLanguage.INSTANCE;
  }

  @Override
  public @Nullable Icon getIcon(Object object) {
    if (object instanceof GraphQLElement element) {
      return element.getIcon(0);
    }
    return null;
  }

  @SuppressWarnings("HardCodedStringLiteral")
  @Override
  public @Nullable String getPresentableText(Object object) {
    if (!(object instanceof GraphQLElement)) return null;

    if (object instanceof GraphQLFile) {
      return ((GraphQLFile)object).getName();
    }

    if (object instanceof GraphQLSelectionSetOperationDefinition) {
      return "anonymous query"; // "{}" selection as root, which corresponds to anonymous query
    }

    if (object instanceof GraphQLInlineFragment) {
      String text = "... on";
      GraphQLTypeCondition typeCondition = ((GraphQLInlineFragment)object).getTypeCondition();
      if (typeCondition != null && typeCondition.getTypeName() != null) {
        text += " " + typeCondition.getTypeName().getName();
      }
      return text;
    }

    if (object instanceof GraphQLNamedElement) {
      String name = ((GraphQLNamedElement)object).getName();
      if (name == null && object instanceof GraphQLTypedOperationDefinition) {
        return "anonymous query"; // "query(args) {}"
      }
      return name;
    }

    if (object instanceof PsiNameIdentifierOwner) {
      final PsiElement nameIdentifier = ((PsiNameIdentifierOwner)object).getNameIdentifier();
      if (nameIdentifier != null) {
        return nameIdentifier.getText();
      }
    }

    if (object instanceof GraphQLObjectTypeDefinition) {
      GraphQLTypeNameDefinition definition = ((GraphQLObjectTypeDefinition)object).getTypeNameDefinition();
      return definition != null ? definition.getName() : ""; // type reference, e.g. "String", "[Int!]"
    }

    if (object instanceof GraphQLScalarTypeDefinition) {
      GraphQLTypeNameDefinition definition = ((GraphQLScalarTypeDefinition)object).getTypeNameDefinition();
      return definition != null ? definition.getName() : "";
    }

    return null;
  }

  @Override
  public @Nullable PsiElement getLeafElement(@NotNull DataMap dataProvider) {
    PsiElement leafElement = super.getLeafElement(dataProvider);
    if (leafElement instanceof GraphQLFieldsDefinition) {
      return leafElement.getParent();
    }
    if (leafElement instanceof GraphQLArgumentsDefinition) {
      return leafElement.getParent();
    }
    return leafElement;
  }
}
