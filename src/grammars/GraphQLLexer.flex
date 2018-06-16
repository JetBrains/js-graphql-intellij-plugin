/**
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql;

import java.util.Stack;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.intellij.lang.jsgraphql.psi.GraphQLElementTypes.*;

%%

%{

    private static final class State {
        final int lBraceCount;
        final int state;

        public State(int state, int lBraceCount) {
            this.state = state;
            this.lBraceCount = lBraceCount;
        }

        @Override
        public String toString() {
            return "yystate = " + state + (lBraceCount == 0 ? "" : "lBraceCount = " + lBraceCount);
        }
    }

    protected final Stack<State> myStateStack = new Stack<State>();
    protected int myLeftBraceCount;

    private void pushState(int state) {
        myStateStack.push(new State(yystate(), myLeftBraceCount));
        myLeftBraceCount = 0;
        yybegin(state);
    }

    private void popState() {
        State state = myStateStack.pop();
        myLeftBraceCount = state.lBraceCount;
        yybegin(state.state);
    }

    private IElementType keywordOrName(IElementType keyword) {
        if(GraphQLLexerUtil.isKeywordAtPos(zzBuffer, zzMarkedPos)) {
            if(SCHEMA_KEYWORD.equals(keyword)) {
                // the schema keyword, so handle the operation types correctly in that state
                pushState(SCHEMA_DEFINITION);
            }
            return keyword;
        }
        return NAME;
    }

  public GraphQLLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class GraphQLLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

SourceCharacter = [\u0009\u000A\u000D\u0020-\uFFFF]
UnicodeBOM = \uFEFF
WhiteSpace = \u0009|\u0020
LineTerminator = \u000A | (\u000D \u000A?)
Comment = "#" [\u0009\u0020-\uFFFF]*
Name = [_A-Za-z][_0-9A-Za-z]*
Variable = \${Name}
Number = -?([0-9]+|[0-9]*\.[0-9]+([eE][-+]?[0-9]+)?)

ANY_ESCAPE_SEQUENCE = \\[^]
ESCAPE_SEQUENCE=\\[^\r\n]

THREE_QUO = (\"\"\")
ONE_TWO_QUO = (\"[^\\\"]) | (\"\\[^]) | (\"\"[^\\\"]) | (\"\"\\[^])
QUO_STRING_CHAR = [^\\\"\r\n\u0009\u0020] | {ANY_ESCAPE_SEQUENCE} | {ONE_TWO_QUO}

DOUBLE_QUOTED_STRING_BODY = ([^\\\"\r\n]|{ESCAPE_SEQUENCE}|(\\[\r\n]))+
TRIPEL_QUOTED_STRING_BODY = {QUO_STRING_CHAR}+

%eof{
  myLeftBraceCount = 0;
  myStateStack.clear();
%eof}

%state QUO_STRING THREE_QUO_STRING VARIABLE_OR_TEMPLATE TEMPLATE SCHEMA_DEFINITION

%%

<SCHEMA_DEFINITION> {
  "query"            { return QUERY_KEYWORD; }
  "mutation"         { return MUTATION_KEYWORD; }
  "subscription"     { return SUBSCRIPTION_KEYWORD; }
  "}"                { popState(); return BRACE_R; }
}

<YYINITIAL, SCHEMA_DEFINITION> {
  // Ignored tokens
  {UnicodeBOM}       { return WHITE_SPACE; }
  {WhiteSpace}+      { return WHITE_SPACE; }
  {LineTerminator}   { return WHITE_SPACE; }
  {Comment}          { return COMMENT; }
  ","                { return WHITE_SPACE; }

  // Punctuators
  "!"                { return BANG; }
  "$"                { pushState(VARIABLE_OR_TEMPLATE); return DOLLAR; }
  "("                { return PAREN_L; }
  ")"                { return PAREN_R; }
  "..."              { return SPREAD; }
  ":"                { return COLON; }
  "="                { return EQUALS; }
  "@"                { return AT; }
  "["                { return BRACKET_L; }
  "]"                { return BRACKET_R; }
  "{"                { return BRACE_L; }
  "|"                { return PIPE; }
  "}"                { return BRACE_R; }
  "&"                { return AMP; }

  // keywords
  "query"            { return keywordOrName(QUERY_KEYWORD); }
  "mutation"         { return keywordOrName(MUTATION_KEYWORD); }
  "subscription"     { return keywordOrName(SUBSCRIPTION_KEYWORD); }
  "fragment"         { return keywordOrName(FRAGMENT_KEYWORD); }
  "on"               { return keywordOrName(ON_KEYWORD); }
  "schema"           { return keywordOrName(SCHEMA_KEYWORD); }
  "type"             { return keywordOrName(TYPE_KEYWORD); }
  "scalar"           { return keywordOrName(SCALAR_KEYWORD); }
  "interface"        { return keywordOrName(INTERFACE_KEYWORD); }
  "implements"       { return keywordOrName(IMPLEMENTS_KEYWORD); }
  "enum"             { return keywordOrName(ENUM_KEYWORD); }
  "union"            { return keywordOrName(UNION_KEYWORD); }
  "extend"           { return keywordOrName(EXTEND_KEYWORD); }
  "input"            { return keywordOrName(INPUT_KEYWORD); }
  "directive"        { return keywordOrName(DIRECTIVE_KEYWORD); }

  // string and number literals
  \"                 { pushState(QUO_STRING);        return OPEN_QUOTE;    }
  {THREE_QUO}        { pushState(THREE_QUO_STRING);  return OPEN_QUOTE;    }
  {Number}           { return NUMBER; }

  // identifiers
  {Name}             { return NAME; }
  {Variable}         { return VARIABLE_NAME; }

  [^]                { return BAD_CHARACTER; }
}

<VARIABLE_OR_TEMPLATE> {
  "{"                { pushState(TEMPLATE); return BRACE_L; }
  {Name}             { popState(); return NAME; }
  [^]                { popState(); return BAD_CHARACTER; }
}

<QUO_STRING> {
    {DOUBLE_QUOTED_STRING_BODY}     { return REGULAR_STRING_PART; }
    \"                              { popState(); return CLOSING_QUOTE; }
    [^]                             { popState(); return BAD_CHARACTER; }
}

<THREE_QUO_STRING> {
    {WhiteSpace}+                   { return WHITE_SPACE; }
    {LineTerminator}                { return WHITE_SPACE; }
    {TRIPEL_QUOTED_STRING_BODY}     { return REGULAR_STRING_PART; }
    {THREE_QUO}                     { popState(); return CLOSING_QUOTE; }
    [^]                             { popState(); return BAD_CHARACTER; }
}

<TEMPLATE> {
    "{"              { myLeftBraceCount++; return TEMPLATE_CHAR; }
    "}"              { if (myLeftBraceCount == 0) { popState(); popState(); return BRACE_R; } myLeftBraceCount--; return TEMPLATE_CHAR; }
   [^\{\}]+          { return TEMPLATE_CHAR; }
}
