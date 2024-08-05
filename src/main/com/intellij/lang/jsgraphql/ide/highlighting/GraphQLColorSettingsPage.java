/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.highlighting;

import com.intellij.lang.jsgraphql.GraphQLBundle;
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

public final class GraphQLColorSettingsPage implements ColorSettingsPage {
  private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
    new AttributesDescriptor(
      GraphQLBundle.messagePointer("graphql.attribute.descriptor.keyword"), GraphQLSyntaxHighlighter.KEYWORD),
    new AttributesDescriptor(
      GraphQLBundle.messagePointer("graphql.attribute.descriptor.number"), GraphQLSyntaxHighlighter.NUMBER),
    new AttributesDescriptor(
      GraphQLBundle.messagePointer("graphql.attribute.descriptor.string"), GraphQLSyntaxHighlighter.STRING),
    new AttributesDescriptor(
      GraphQLBundle.messagePointer("graphql.attribute.descriptor.comment"), GraphQLSyntaxHighlighter.COMMENT),
    new AttributesDescriptor(
      GraphQLBundle.messagePointer("graphql.attribute.descriptor.operation.definition"), GraphQLSyntaxAnnotator.OPERATION_DEFINITION),
    new AttributesDescriptor(
      GraphQLBundle.messagePointer("graphql.attribute.descriptor.fragment.definition"), GraphQLSyntaxAnnotator.FRAGMENT_DEFINITION),
    new AttributesDescriptor(
      GraphQLBundle.messagePointer("graphql.attribute.descriptor.fragment.spread"), GraphQLSyntaxAnnotator.FRAGMENT_SPREAD),
    new AttributesDescriptor(
      GraphQLBundle.messagePointer("graphql.attribute.descriptor.field.name"), GraphQLSyntaxAnnotator.FIELD_NAME),
    new AttributesDescriptor(
      GraphQLBundle.messagePointer("graphql.attribute.descriptor.field.alias"), GraphQLSyntaxAnnotator.FIELD_ALIAS),
    new AttributesDescriptor(
      GraphQLBundle.messagePointer("graphql.attribute.descriptor.parameter"), GraphQLSyntaxAnnotator.PARAMETER),
    new AttributesDescriptor(
      GraphQLBundle.messagePointer("graphql.attribute.descriptor.argument"), GraphQLSyntaxAnnotator.ARGUMENT),
    new AttributesDescriptor(
      GraphQLBundle.messagePointer("graphql.attribute.descriptor.object.field"), GraphQLSyntaxAnnotator.OBJECT_FIELD),
    new AttributesDescriptor(
      GraphQLBundle.messagePointer("graphql.attribute.descriptor.variable.definition"), GraphQLSyntaxAnnotator.VARIABLE_DEFINITION),
    new AttributesDescriptor(
      GraphQLBundle.messagePointer("graphql.attribute.descriptor.variable"), GraphQLSyntaxAnnotator.VARIABLE),
    new AttributesDescriptor(
      GraphQLBundle.messagePointer("graphql.attribute.descriptor.type.name"), GraphQLSyntaxAnnotator.TYPE_NAME),
    new AttributesDescriptor(
      GraphQLBundle.messagePointer("graphql.attribute.descriptor.constant"), GraphQLSyntaxAnnotator.CONSTANT),
    new AttributesDescriptor(
      GraphQLBundle.messagePointer("graphql.attribute.descriptor.directive"), GraphQLSyntaxAnnotator.DIRECTIVE),
    new AttributesDescriptor(
      GraphQLBundle.messagePointer("graphql.attribute.descriptor.unused.fragment"), GraphQLSyntaxAnnotator.UNUSED_FRAGMENT),
  };

  private static final Map<String, TextAttributesKey> TAG_TO_DESCRIPTOR_MAP = new HashMap<>();

  static {
    TAG_TO_DESCRIPTOR_MAP.put("operationDefinition", GraphQLSyntaxAnnotator.OPERATION_DEFINITION);
    TAG_TO_DESCRIPTOR_MAP.put("fragmentDefinition", GraphQLSyntaxAnnotator.FRAGMENT_DEFINITION);
    TAG_TO_DESCRIPTOR_MAP.put("fragmentSpread", GraphQLSyntaxAnnotator.FRAGMENT_SPREAD);
    TAG_TO_DESCRIPTOR_MAP.put("fieldName", GraphQLSyntaxAnnotator.FIELD_NAME);
    TAG_TO_DESCRIPTOR_MAP.put("fieldAlias", GraphQLSyntaxAnnotator.FIELD_ALIAS);
    TAG_TO_DESCRIPTOR_MAP.put("parameter", GraphQLSyntaxAnnotator.PARAMETER);
    TAG_TO_DESCRIPTOR_MAP.put("argument", GraphQLSyntaxAnnotator.ARGUMENT);
    TAG_TO_DESCRIPTOR_MAP.put("objectField", GraphQLSyntaxAnnotator.OBJECT_FIELD);
    TAG_TO_DESCRIPTOR_MAP.put("variableDefinition", GraphQLSyntaxAnnotator.VARIABLE_DEFINITION);
    TAG_TO_DESCRIPTOR_MAP.put("variable", GraphQLSyntaxAnnotator.VARIABLE);
    TAG_TO_DESCRIPTOR_MAP.put("typeName", GraphQLSyntaxAnnotator.TYPE_NAME);
    TAG_TO_DESCRIPTOR_MAP.put("constant", GraphQLSyntaxAnnotator.CONSTANT);
    TAG_TO_DESCRIPTOR_MAP.put("directive", GraphQLSyntaxAnnotator.DIRECTIVE);
    TAG_TO_DESCRIPTOR_MAP.put("string", GraphQLSyntaxHighlighter.STRING);
  }

  @Override
  public @Nullable Icon getIcon() {
    return GraphQLIcons.FILE;
  }

  @Override
  public @NotNull SyntaxHighlighter getHighlighter() {
    return new GraphQLSyntaxHighlighter();
  }

  @Override
  public @NotNull String getDemoText() {
    return """
      # Comment

      query <operationDefinition>Hero</operationDefinition>(<variableDefinition>$episode</variableDefinition>: <typeName>Episode</typeName>!, <variableDefinition>$withFriends</variableDefinition>: <typeName>Boolean</typeName>!) {
        <fieldName>hero</fieldName>(<argument>episode</argument>: <variable>$episode</variable>) {
          <fieldName>name</fieldName>
          ...<fragmentSpread>HeroDetails</fragmentSpread>
          <fieldAlias>acquaintances</fieldAlias>: <fieldName>friends</fieldName> <directive>@include</directive>(<argument>if</argument>: <variable>$withFriends</variable>) {
            <fieldName>name</fieldName>
          }
        }
      }

      fragment <fragmentDefinition>HeroDetails</fragmentDefinition> on <typeName>Character</typeName> {
        <fieldName>name</fieldName>
        ... on <typeName>Human</typeName> {
          <fieldName>height</fieldName>(<argument>unit</argument>: <constant>METER</constant>)
        }
        ... on <typeName>Droid</typeName> {
          <fieldName>primaryFunction</fieldName>
        }
      }

      mutation <typeName>CreateUser</typeName> {
          <fieldName>createUser</fieldName>(<argument>userInput</argument>: {
              <objectField>name</objectField>: <string>"John"</string>
              <objectField>friends</objectField>: [{ <objectField>name</objectField>: <string>"Bob"</string> }]
          })
      }

      type <typeName>Mutation</typeName> {
          <fieldName>createUser</fieldName>(<parameter>id</parameter>: <typeName>String</typeName>, <parameter>userInput</parameter>: <typeName>UserInput</typeName>): <typeName>ID</typeName>
      }""";
  }

  @Override
  public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return TAG_TO_DESCRIPTOR_MAP;
  }

  @Override
  public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @Override
  public @NotNull ColorDescriptor @NotNull [] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @Override
  public @NotNull String getDisplayName() {
    return GraphQLConstants.GraphQL;
  }
}
