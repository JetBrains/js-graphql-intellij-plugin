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
        new AttributesDescriptor("Parameter", GraphQLSyntaxAnnotator.PARAMETER),
        new AttributesDescriptor("Argument", GraphQLSyntaxAnnotator.ARGUMENT),
        new AttributesDescriptor("Object field", GraphQLSyntaxAnnotator.OBJECT_FIELD),
        new AttributesDescriptor("Variable definition", GraphQLSyntaxAnnotator.VARIABLE_DEFINITION),
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
