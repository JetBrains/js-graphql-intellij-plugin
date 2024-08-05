/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.formatter;

import com.google.common.collect.Sets;
import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.psi.GraphQLElementTypes;
import com.intellij.psi.TokenType;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class GraphQLBlock extends AbstractBlock {

  private List<Block> blocks = null;

  public static Set<IElementType> INDENT_PARENTS = Sets.newHashSet(
    GraphQLElementTypes.SELECTION_SET,
    GraphQLElementTypes.OPERATION_TYPE_DEFINITIONS,
    GraphQLElementTypes.FIELDS_DEFINITION,
    GraphQLElementTypes.ENUM_VALUE_DEFINITIONS,
    GraphQLElementTypes.INPUT_OBJECT_VALUE_DEFINITIONS,
    GraphQLElementTypes.VARIABLE_DEFINITIONS,
    GraphQLElementTypes.ARGUMENTS,
    GraphQLElementTypes.ARGUMENTS_DEFINITION,
    GraphQLElementTypes.ARRAY_VALUE,
    GraphQLElementTypes.OBJECT_VALUE,
    GraphQLElementTypes.DIRECTIVE_LOCATIONS,
    GraphQLElementTypes.UNION_MEMBERS
  );

  private static final Set<IElementType> NO_INDENT_ELEMENT_TYPES = Sets.newHashSet(
    GraphQLElementTypes.BRACE_R,
    GraphQLElementTypes.BRACE_L,
    GraphQLElementTypes.BRACKET_R,
    GraphQLElementTypes.BRACKET_L,
    GraphQLElementTypes.PAREN_L,
    GraphQLElementTypes.PAREN_R
  );

  public GraphQLBlock(@NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment) {
    super(node, wrap, alignment);
  }

  @Override
  protected List<Block> buildChildren() {
    if (blocks == null) {
      blocks = ContainerUtil.mapNotNull(myNode.getChildren(null), node -> {
        if (node.getTextLength() == 0) {
          return null;
        }
        if (node.getElementType() == TokenType.WHITE_SPACE) {
          if (node.getText().contains(",")) {
            // non-significant comma, but not empty text according to IDEA
            return new GraphQLBlock(node, null, null);
          }
          return null;
        }
        return new GraphQLBlock(node, null, null);
      });
    }
    return blocks;
  }

  @Override
  public Indent getIndent() {
    if (NO_INDENT_ELEMENT_TYPES.contains(myNode.getElementType())) {
      return Indent.getNoneIndent();
    }
    final ASTNode treeParent = myNode.getTreeParent();
    if (treeParent != null) {
      if (INDENT_PARENTS.contains(treeParent.getElementType())) {
        return Indent.getNormalIndent();
      }
    }
    return Indent.getNoneIndent();
  }

  @Override
  protected @Nullable Indent getChildIndent() {
    final IElementType elementType = myNode.getElementType();
    if (elementType == GraphQLElementTypes.BRACE_R) {
      return Indent.getNoneIndent();
    }
    if (INDENT_PARENTS.contains(elementType)) {
      return Indent.getNormalIndent();
    }
    return Indent.getNoneIndent();
  }

  @Override
  public @Nullable Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
    return null;
  }

  public boolean isLeaf() {
    return myNode.getFirstChildNode() == null;
  }
}
