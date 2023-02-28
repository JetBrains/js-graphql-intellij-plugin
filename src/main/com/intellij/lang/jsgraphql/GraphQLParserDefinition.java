/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lang.jsgraphql.psi.GraphQLElementTypes;
import com.intellij.lang.jsgraphql.psi.GraphQLExtendedElementTypes;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public class GraphQLParserDefinition implements ParserDefinition {

    public static final Key<Boolean> GRAPHQL_ACTIVATED = Key.create("graphql.activated");

    public static final IFileElementType FILE = new IFileElementType("GRAPHQL", GraphQLLanguage.INSTANCE);

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new GraphQLLexerAdapter();
    }

    @NotNull
    public TokenSet getWhitespaceTokens() {
        return GraphQLExtendedElementTypes.WHITE_SPACES;
    }

    @NotNull
    public TokenSet getCommentTokens() {
        return GraphQLExtendedElementTypes.COMMENTS;
    }

    @NotNull
    public TokenSet getStringLiteralElements() {
        return GraphQLExtendedElementTypes.STRING_LITERALS;
    }

    @NotNull
    public PsiParser createParser(final Project project) {
        return new GraphQLParser();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new GraphQLFile(viewProvider);
    }

    public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    @NotNull
    public PsiElement createElement(ASTNode node) {
        return GraphQLElementTypes.Factory.createElement(node);
    }
}
