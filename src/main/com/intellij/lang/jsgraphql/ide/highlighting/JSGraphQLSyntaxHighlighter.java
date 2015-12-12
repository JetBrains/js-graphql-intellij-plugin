/**
 *  Copyright (c) 2015, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.highlighting;

import com.intellij.lang.jsgraphql.JSGraphQLTokenTypes;
import com.intellij.lang.jsgraphql.lexer.JSGraphQLLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.openapi.project.Project;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class JSGraphQLSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey KEYWORD = createTextAttributesKey("JSGRAPHQL.KEYWORD", TextAttributesKey.find("JS.KEYWORD"));
    public static final TextAttributesKey PUNCTUATION = createTextAttributesKey("JSGRAPHQL.PUNCTUATION", TextAttributesKey.find("JS.BRACKETS"));
    public static final TextAttributesKey PROPERTY = createTextAttributesKey("JSGRAPHQL.PROPERTY", TextAttributesKey.find("JS.INSTANCE_MEMBER_VARIABLE"));
    public static final TextAttributesKey DEF = createTextAttributesKey("JSGRAPHQL.DEF", TextAttributesKey.find("JS.GLOBAL_FUNCTION"));
    public static final TextAttributesKey ATTRIBUTE = createTextAttributesKey("JSGRAPHQL.ATTRIBUTE", TextAttributesKey.find("JS.PARAMETER"));
    public static final TextAttributesKey VARIABLE = createTextAttributesKey("JSGRAPHQL.VARIABLE", TextAttributesKey.find("JS.INSTANCE_MEMBER_VARIABLE"));
    public static final TextAttributesKey QUALIFIER = createTextAttributesKey("JSGRAPHQL.QUALIFIER", TextAttributesKey.find("JS.GLOBAL_FUNCTION"));
    public static final TextAttributesKey NUMBER = createTextAttributesKey("JSGRAPHQL.NUMBER", TextAttributesKey.find("JS.NUMBER"));
    public static final TextAttributesKey STRING = createTextAttributesKey("JSGRAPHQL.STRING", TextAttributesKey.find("JS.STRING"));
    public static final TextAttributesKey BUILTIN = createTextAttributesKey("JSGRAPHQL.BUILTIN", TextAttributesKey.find("JS.KEYWORD"));
    public static final TextAttributesKey COMMENT = createTextAttributesKey("JSGRAPHQL.COMMENT", TextAttributesKey.find("JS.LINE_COMMENT"));
    public static final TextAttributesKey ATOM = createTextAttributesKey("JSGRAPHQL.ATOM", TextAttributesKey.find("JS.CLASS"));
    public static final TextAttributesKey BAD_CHARACTER = createTextAttributesKey("JSGRAPHQL.BADCHARACTER", TextAttributesKey.find("JS.BADCHARACTER"));


    private static final TextAttributesKey[] KEYWORD_KEYS = new TextAttributesKey[]{KEYWORD};
    private static final TextAttributesKey[] PUNCTUATION_KEYS = new TextAttributesKey[]{PUNCTUATION};
    private static final TextAttributesKey[] PROPERTY_KEYS = new TextAttributesKey[]{PROPERTY};
    private static final TextAttributesKey[] DEF_KEYS = new TextAttributesKey[]{DEF};
    private static final TextAttributesKey[] ATTRIBUTE_KEYS = new TextAttributesKey[]{ATTRIBUTE};
    private static final TextAttributesKey[] VARIABLE_KEYS = new TextAttributesKey[]{VARIABLE};
    private static final TextAttributesKey[] QUALIFIER_KEYS = new TextAttributesKey[]{QUALIFIER};
    private static final TextAttributesKey[] NUMBER_KEYS = new TextAttributesKey[]{NUMBER};
    private static final TextAttributesKey[] STRING_KEYS = new TextAttributesKey[]{STRING};
    private static final TextAttributesKey[] BUILTIN_KEYS = new TextAttributesKey[]{BUILTIN};
    private static final TextAttributesKey[] COMMENT_KEYS = new TextAttributesKey[]{COMMENT};
    private static final TextAttributesKey[] ATOM_KEYS = new TextAttributesKey[]{ATOM};
    private static final TextAttributesKey[] BAD_CHARACTER_KEYS = new TextAttributesKey[]{BAD_CHARACTER};
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    private final Project project;

    public JSGraphQLSyntaxHighlighter(Project project) {
        this.project = project;
    }

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new JSGraphQLLexer(project);
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        if (tokenType.equals(JSGraphQLTokenTypes.KEYWORD)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(JSGraphQLTokenTypes.PUNCTUATION)) {
            return PUNCTUATION_KEYS;
        } else if (tokenType.equals(JSGraphQLTokenTypes.PROPERTY)) {
            return PROPERTY_KEYS;
        } else if (tokenType.equals(JSGraphQLTokenTypes.DEF)) {
            return DEF_KEYS;
        } else if (tokenType.equals(JSGraphQLTokenTypes.ATTRIBUTE)) {
            return ATTRIBUTE_KEYS;
        } else if (tokenType.equals(JSGraphQLTokenTypes.VARIABLE)) {
            return VARIABLE_KEYS;
        } else if (tokenType.equals(JSGraphQLTokenTypes.QUALIFIER)) {
            return QUALIFIER_KEYS;
        } else if (tokenType.equals(JSGraphQLTokenTypes.NUMBER)) {
            return NUMBER_KEYS;
        } else if (tokenType.equals(JSGraphQLTokenTypes.STRING) || tokenType.equals(JSGraphQLTokenTypes.OPEN_QUOTE) || tokenType.equals(JSGraphQLTokenTypes.CLOSE_QUOTE)) {
            return STRING_KEYS;
        } else if (tokenType.equals(JSGraphQLTokenTypes.BUILTIN)) {
            return BUILTIN_KEYS;
        } else if (tokenType.equals(JSGraphQLTokenTypes.COMMENT)) {
            return COMMENT_KEYS;
        } else if (tokenType.equals(JSGraphQLTokenTypes.ATOM)) {
            return ATOM_KEYS;
        } else if (tokenType.equals(JSGraphQLTokenTypes.INVALIDCHAR)) {
            return BAD_CHARACTER_KEYS;
        } else {
            return EMPTY_KEYS;
        }
    }
}