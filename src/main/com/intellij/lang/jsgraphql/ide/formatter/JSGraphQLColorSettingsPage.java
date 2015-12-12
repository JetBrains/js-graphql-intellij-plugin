/**
 *  Copyright (c) 2015, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.formatter;

import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons;
import com.intellij.lang.jsgraphql.ide.highlighting.JSGraphQLSyntaxHighlighter;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class JSGraphQLColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Keyword", JSGraphQLSyntaxHighlighter.KEYWORD),
            new AttributesDescriptor("Punctuation", JSGraphQLSyntaxHighlighter.PUNCTUATION),
            new AttributesDescriptor("Field", JSGraphQLSyntaxHighlighter.PROPERTY),
            new AttributesDescriptor("Definition", JSGraphQLSyntaxHighlighter.DEF),
            new AttributesDescriptor("Attribute", JSGraphQLSyntaxHighlighter.ATTRIBUTE),
            new AttributesDescriptor("Variable", JSGraphQLSyntaxHighlighter.VARIABLE),
            new AttributesDescriptor("Qualifier", JSGraphQLSyntaxHighlighter.QUALIFIER),
            new AttributesDescriptor("Number", JSGraphQLSyntaxHighlighter.NUMBER),
            new AttributesDescriptor("String", JSGraphQLSyntaxHighlighter.STRING),
            new AttributesDescriptor("Builtin", JSGraphQLSyntaxHighlighter.BUILTIN),
            new AttributesDescriptor("Comment", JSGraphQLSyntaxHighlighter.COMMENT),
            new AttributesDescriptor("Type name", JSGraphQLSyntaxHighlighter.ATOM),
            new AttributesDescriptor("Invalid character", JSGraphQLSyntaxHighlighter.BAD_CHARACTER)
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return JSGraphQLIcons.Files.GraphQL;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new JSGraphQLSyntaxHighlighter(ProjectManager.getInstance().getOpenProjects()[0]);
    }

    @NotNull
    @Override
    public String getDemoText() {
        return "query MyQuery {\n" +
                "    __schema {\n" +
                "        types {\n" +
                "            ...FullType\n" +
                "        }\n" +
                "    }\n" +
                "    first: node(id: 1234) { id }\n" +
                "    second: node(id: \"foo\", option: true) { id }\n" +
                "}\n" +
                "\n" +
                "fragment FullType on __Type {\n" +
                "    # Note: __Type has a lot more fields than this\n" +
                "    name\n" +
                "}\n" +
                "\n" +
                "mutation MyMutation($input: MyInput!) {\n" +
                "    # Payload\n" +
                "}\n" +
                "\n" +
                "%invalid%";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @NotNull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @NotNull
    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "GraphQL";
    }
}