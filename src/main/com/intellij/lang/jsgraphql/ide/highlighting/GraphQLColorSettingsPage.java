/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.highlighting;

import com.intellij.lang.jsgraphql.GraphQLConstants;
import com.intellij.lang.jsgraphql.icons.GraphQLIcons;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class GraphQLColorSettingsPage implements ColorSettingsPage {
  private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
      new AttributesDescriptor("Keyword", GraphQLSyntaxHighlighter.KEYWORD),
      new AttributesDescriptor("Number", GraphQLSyntaxHighlighter.NUMBER),
      new AttributesDescriptor("String", GraphQLSyntaxHighlighter.STRING),
      new AttributesDescriptor("Comment", GraphQLSyntaxHighlighter.COMMENT),
      new AttributesDescriptor("Operation definition", GraphQLSyntaxAnnotator.OPERATION_DEFINITION),
      new AttributesDescriptor("Fragment definition", GraphQLSyntaxAnnotator.FRAGMENT_DEFINITION),
      new AttributesDescriptor("Fragment spread", GraphQLSyntaxAnnotator.FRAGMENT_SPREAD),
      new AttributesDescriptor("Field name", GraphQLSyntaxAnnotator.FIELD_NAME),
      new AttributesDescriptor("Field alias", GraphQLSyntaxAnnotator.FIELD_ALIAS),
      new AttributesDescriptor("Argument", GraphQLSyntaxAnnotator.ARGUMENT),
      new AttributesDescriptor("Variable", GraphQLSyntaxAnnotator.VARIABLE),
      new AttributesDescriptor("Type name", GraphQLSyntaxAnnotator.TYPE_NAME),
      new AttributesDescriptor("Constant", GraphQLSyntaxAnnotator.CONSTANT),
      new AttributesDescriptor("Directive", GraphQLSyntaxAnnotator.DIRECTIVE),
      new AttributesDescriptor("Unused fragment", GraphQLSyntaxAnnotator.UNUSED_FRAGMENT),
  };

  private static final Map<String, TextAttributesKey> TAG_TO_DESCRIPTOR_MAP = new HashMap<>();
  static {
    TAG_TO_DESCRIPTOR_MAP.put("operationDefinition", GraphQLSyntaxAnnotator.OPERATION_DEFINITION);
    TAG_TO_DESCRIPTOR_MAP.put("fragmentDefinition", GraphQLSyntaxAnnotator.FRAGMENT_DEFINITION);
    TAG_TO_DESCRIPTOR_MAP.put("fragmentSpread", GraphQLSyntaxAnnotator.FRAGMENT_SPREAD);
    TAG_TO_DESCRIPTOR_MAP.put("fieldName", GraphQLSyntaxAnnotator.FIELD_NAME);
    TAG_TO_DESCRIPTOR_MAP.put("fieldAlias", GraphQLSyntaxAnnotator.FIELD_ALIAS);
    TAG_TO_DESCRIPTOR_MAP.put("argument", GraphQLSyntaxAnnotator.ARGUMENT);
    TAG_TO_DESCRIPTOR_MAP.put("variable", GraphQLSyntaxAnnotator.VARIABLE);
    TAG_TO_DESCRIPTOR_MAP.put("typeName", GraphQLSyntaxAnnotator.TYPE_NAME);
    TAG_TO_DESCRIPTOR_MAP.put("constant", GraphQLSyntaxAnnotator.CONSTANT);
    TAG_TO_DESCRIPTOR_MAP.put("directive", GraphQLSyntaxAnnotator.DIRECTIVE);
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return GraphQLIcons.FILE;
  }

  @NotNull
  @Override
  public SyntaxHighlighter getHighlighter() {
    return new GraphQLSyntaxHighlighter();
  }

  @NotNull
  @Override
  public String getDemoText() {
    return
        "# Comment\n" +
        "\n" +
        "query <operationDefinition>Hero</operationDefinition>(<variable>$episode</variable>: <typeName>Episode</typeName>!, <variable>$withFriends</variable>: <typeName>Boolean</typeName>!) {\n" +
        "  <fieldName>hero</fieldName>(<argument>episode</argument>: <variable>$episode</variable>) {\n" +
        "    <fieldName>name</fieldName>\n" +
        "    ...<fragmentSpread>HeroDetails</fragmentSpread>\n" +
        "    <fieldAlias>acquaintances</fieldAlias>: <fieldName>friends</fieldName> <directive>@include</directive>(<argument>if</argument>: <variable>$withFriends</variable>) {\n" +
        "      <fieldName>name</fieldName>\n" +
        "    }\n" +
        "  }\n" +
        "}\n" +
        "\n" +
        "fragment <fragmentDefinition>HeroDetails</fragmentDefinition> on <typeName>Character</typeName> {\n" +
        "  <fieldName>name</fieldName>\n" +
        "  ... on <typeName>Human</typeName> {\n" +
        "    <fieldName>height</fieldName>(<argument>unit</argument>: <constant>METER</constant>)\n" +
        "  }\n" +
        "  ... on <typeName>Droid</typeName> {\n" +
        "    <fieldName>primaryFunction</fieldName>\n" +
        "  }\n" +
        "}";
  }

  @Nullable
  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return TAG_TO_DESCRIPTOR_MAP;
  }

  @Override
  public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @NotNull
  @Override
  public ColorDescriptor @NotNull [] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return GraphQLConstants.GraphQL;
  }
}
