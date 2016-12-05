/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.lexer;

import com.google.common.collect.Lists;
import com.intellij.lang.jsgraphql.JSGraphQLDebugUtil;
import com.intellij.lang.jsgraphql.JSGraphQLKeywords;
import com.intellij.lang.jsgraphql.JSGraphQLTokenTypes;
import com.intellij.lang.jsgraphql.languageservice.JSGraphQLNodeLanguageServiceClient;
import com.intellij.lang.jsgraphql.languageservice.api.Token;
import com.intellij.lang.jsgraphql.languageservice.api.TokensResponse;
import com.intellij.lexer.LexerBase;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.tree.IElementType;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class JSGraphQLLexer extends LexerBase {

    private static final Logger log = Logger.getInstance(JSGraphQLLexer.class);

    private List<JSGraphQLToken> tokens = Lists.newArrayList();
    private JSGraphQLToken currentToken;

    private int currentTokenIndex = -1;
    private int currentTokenStart = 0;
    private int currentTokenEnd = 0;

    private CharSequence buffer;
    private int startOffset;
    private int endOffset;

    private TokensResponse response;

    private final Project project;

    public JSGraphQLLexer(Project project) {
        this.project = project;
    }

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {

        this.buffer = buffer;
        this.startOffset = startOffset;
        this.endOffset = endOffset;

        this.currentToken = null;
        this.currentTokenIndex = -1;
        this.currentTokenStart = 0;
        this.currentTokenEnd = 0;
        this.tokens = Lists.newArrayList();
        this.response = null;

        fetchTokensFromLanguageService();
    }

    private void fetchTokensFromLanguageService() {

        final String bufferAsString = buffer.toString();
        if (buffer.length() == 0) {
            response = new TokensResponse();
            return;
        }

        // get the response using the client
        response = JSGraphQLNodeLanguageServiceClient.getTokens(bufferAsString, project);

        if (response == null) {
            // blank
            response = new TokensResponse();
            Token dummy = new Token();
            dummy.setStart(startOffset);
            dummy.setEnd(endOffset);
            dummy.setText(bufferAsString.substring(startOffset, endOffset));
            dummy.setKind("unknown");
            dummy.setType("unknown");
            response.getTokens().add(dummy);
        }

        for (Token token : response.getTokens()) {
            if (token.getStart() < startOffset) {
                continue;
            } else if (token.getStart() > endOffset) {
                break;
            }
            final String text = token.getText();
            IElementType tokenType = JSGraphQLCodeMirrorTokenMapper.getTokenType(token.getType());
            if (tokenType.equals(JSGraphQLTokenTypes.PUNCTUATION)) {
                final IElementType punctuationTokenType = getPunctuationTokenType(text);
                if (punctuationTokenType != null) {
                    tokenType = punctuationTokenType;
                } else if(text.contains(",")) {
                    // separate out commas from surrounding whitespace to support indentation on ", field" lines
                    int offset = 0;
                    final String[] parts = StringUtils.splitByCharacterType(text);
                    for (String part : parts) {
                        final Token partSourceToken = token.withTextAndOffset(part, offset);
                        if(part.equals(",")) {
                            tokens.add(new JSGraphQLToken(tokenType, partSourceToken));
                        } else {
                            partSourceToken.setKind(JSGraphQLCodeMirrorTokenMapper.CODEMIRROR_WHITESPACE);
                            tokens.add(new JSGraphQLToken(JSGraphQLTokenTypes.WHITESPACE, partSourceToken));
                        }
                        offset += part.length();
                    }
                    continue; // already added the required tokens
                } else if (JSGraphQLKeywords.FRAGMENT_DOTS.equals(text)) {
                    // consider the "..." spread operator a keyword for highlighting
                    tokenType = JSGraphQLTokenTypes.KEYWORD;
                }
            } else if (tokenType.equals(JSGraphQLTokenTypes.INVALIDCHAR)) {
                // make sure we get the right tokenType for structural braces
                // to aid in brace matching and enter after unclosed opening brace
                IElementType punctuationTokenType = getPunctuationTokenType(text);
                if (punctuationTokenType != null) {
                    tokenType = punctuationTokenType;
                }
            }
            tokens.add(new JSGraphQLToken(tokenType, token));
        }
        verifyTokens();
        if (tokens.size() > 0) {
            advance();
        }
    }

    private IElementType getPunctuationTokenType(String text) {
        if ("{".equals(text)) {
            return JSGraphQLTokenTypes.LBRACE;
        } else if ("}".equals(text)) {
            return JSGraphQLTokenTypes.RBRACE;
        } else if ("(".equals(text)) {
            return JSGraphQLTokenTypes.LPAREN;
        } else if (")".equals(text)) {
            return JSGraphQLTokenTypes.RPAREN;
        } else if ("[".equals(text)) {
            return JSGraphQLTokenTypes.LBRACKET;
        } else if ("]".equals(text)) {
            return JSGraphQLTokenTypes.RBRACKET;
        }
        return null;
    }

    private void verifyTokens() {
        if (!JSGraphQLDebugUtil.debug) return;
        if (tokens.size() == 0) {
            if (buffer.length() > 0) {
                log.error("No tokens returned for non-empty buffer", buffer.toString());
            }
        } else {
            int start = tokens.get(0).sourceToken.getStart();
            int end = tokens.get(tokens.size() - 1).sourceToken.getEnd();
            if (start != startOffset) {
                log.error("First token " + tokens.get(0) + " starting at " + start + " should start at " + startOffset, buffer.toString());
            }
            if (end != endOffset) {
                log.error("Last token " + tokens.get(tokens.size() - 1) + " ending at " + end + " should end at " + endOffset, buffer.toString());
            }
            // verify that the tokens fully cover the requested range
            int expectedTokenStart = startOffset;
            int expectedTokenEnd = tokens.get(0).sourceToken.getEnd();
            int tokenIndex = 0;
            for (JSGraphQLToken token : tokens) {
                if (token.sourceToken.getStart() != expectedTokenStart) {
                    log.error("Invalid token start range, expected " + expectedTokenStart, token.toString(), buffer.toString());
                }
                if (token.sourceToken.getEnd() != expectedTokenEnd) {
                    log.error("Invalid token end range, expected " + expectedTokenEnd, token.toString(), buffer.toString());
                }
                if (token.sourceToken.getText().length() != token.sourceToken.getEnd() - token.sourceToken.getStart()) {
                    log.error("Token range doesn't match token length", token.toString());
                }
                tokenIndex++;
                if (tokenIndex < tokens.size()) {
                    expectedTokenStart = expectedTokenEnd;
                    expectedTokenEnd = tokens.get(tokenIndex).sourceToken.getEnd();
                }

            }
        }

    }

    @Nullable
    @Override
    public IElementType getTokenType() {
        return currentToken != null ? currentToken.tokenType : null;
    }

    @Override
    public void advance() {
        if (currentTokenIndex < tokens.size() - 1) {
            currentTokenIndex++;
            currentToken = tokens.get(currentTokenIndex);
            currentTokenStart = currentToken.sourceToken.getStart();
            currentTokenEnd = currentToken.sourceToken.getEnd();
        } else {
            currentTokenIndex++;
            currentTokenStart = currentTokenEnd;
            currentToken = null;
        }
    }


    @Override
    public int getTokenStart() {
        return currentTokenStart;
    }

    @Override
    public int getTokenEnd() {
        return currentTokenEnd;
    }

    @NotNull
    @Override
    public CharSequence getBufferSequence() {
        return buffer;
    }

    @Override
    public int getBufferEnd() {
        return endOffset;
    }

    @Override
    public int getState() {
        return startOffset; // not used
    }

    public List<JSGraphQLToken> getTokens() {
        return tokens;
    }

}
