/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.utils;

import graphql.language.Document;
import graphql.language.SourceLocation;
import graphql.parser.GraphqlAntlrToLanguage;
import graphql.parser.MultiSourceReader;
import graphql.parser.antlr.GraphqlLexer;
import graphql.parser.antlr.GraphqlParser;
import graphql.schema.GraphQLModifiedType;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLUnmodifiedType;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.List;

public final class GraphQLUtil {


    /**
     * Gets the raw named type that sits within a non-null/list modifier type, or the type as-is if no unwrapping is needed
     * @param graphQLType the type to unwrap
     * @return the raw type as-is, or the type wrapped inside a non-null/list modifier type
     */
    public static GraphQLUnmodifiedType getUnmodifiedType(GraphQLType graphQLType) {
        if (graphQLType instanceof GraphQLModifiedType) {
            return getUnmodifiedType(((GraphQLModifiedType) graphQLType).getWrappedType());
        }
        return (GraphQLUnmodifiedType) graphQLType;
    }


    public static Document parseDocument(String input, int lineDelta, int firstLineColumnDelta) {
        return parseDocument(input, null, lineDelta, firstLineColumnDelta);
    }

    /**
     *
     * Creates a source location based on a token and line/column offsets
     * @param token the token to create a location for
     * @param lineDelta the delta line to apply to the document and all child nodes
     * @param firstLineColumnDelta the column delta for the first line
     * @return the offset location for the token
     */
    public static SourceLocation createSourceLocationFromDelta(Token token, int lineDelta, int firstLineColumnDelta) {
        String sourceName = token.getTokenSource().getSourceName();
        if (IntStream.UNKNOWN_SOURCE_NAME.equals(sourceName)) {
            // UNKNOWN_SOURCE_NAME is Antrl's way of indicating that no source name was given during parsing --
            // which is the case when queries and other operations are parsed. We don't want this hardcoded
            // '<unknown>' sourceName to leak to clients when the response is serialized as JSON, so we null it.
            sourceName = null;
        }
        int line = token.getLine();
        int column = token.getCharPositionInLine() + 1;
        if(line == 0 && firstLineColumnDelta > 0) {
            column += firstLineColumnDelta;
        }
        line += lineDelta;
        return new SourceLocation(line, column, sourceName);
    }

    /**
     * Parses GraphQL string input into a graphql-java Document, shifting the source locations in the specified document with the specified line delta.
     * Shifting of the sourceLocation is required for proper error reporting locations for GraphQL language injections, e.g. GraphQL in a JavaScript file.
     * @param input a GraphQL document represented as a string to be parsed
     * @param sourceName the file name of the source
     * @param lineDelta the delta line to apply to the document and all child nodes
     * @param firstLineColumnDelta the column delta for the first line
     */
    public static Document parseDocument(String input, String sourceName, int lineDelta, int firstLineColumnDelta) {

        CharStream charStream;
        if(sourceName == null) {
            charStream = CharStreams.fromString(input);
        } else{
            charStream = CharStreams.fromString(input, sourceName);
        }

        GraphqlLexer lexer = new GraphqlLexer(charStream);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        GraphqlParser parser = new GraphqlParser(tokens);
        parser.removeErrorListeners();
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        parser.setErrorHandler(new BailErrorStrategy());
        GraphqlParser.DocumentContext documentContext = parser.document();

        MultiSourceReader multiSourceReader = MultiSourceReader.newMultiSourceReader()
                .string(input, sourceName)
                .trackData(true)
                .build();

        GraphqlAntlrToLanguage antlrToLanguage = new GraphqlAntlrToLanguage(tokens, multiSourceReader) {
            @Override
            protected SourceLocation getSourceLocation(ParserRuleContext parserRuleContext) {
                return createSourceLocationFromDelta(parserRuleContext.getStart(), lineDelta, firstLineColumnDelta);
            }

            @Override
            protected SourceLocation getSourceLocation(Token token) {
                return createSourceLocationFromDelta(token, lineDelta, firstLineColumnDelta);
            }
        };
        Document doc = antlrToLanguage.createDocument(documentContext);

        Token stop = documentContext.getStop();
        List<Token> allTokens = tokens.getTokens();
        if (stop != null && allTokens != null && !allTokens.isEmpty()) {
            Token last = allTokens.get(allTokens.size() - 1);
            //
            // do we have more tokens in the stream than we consumed in the parse?
            // if yes then its invalid.  We make sure its the same channel
            boolean notEOF = last.getType() != Token.EOF;
            boolean lastGreaterThanDocument = last.getTokenIndex() > stop.getTokenIndex();
            boolean sameChannel = last.getChannel() == stop.getChannel();
            if (notEOF && lastGreaterThanDocument && sameChannel) {
                throw new ParseCancellationException("There are more tokens in the query that have not been consumed");
            }
        }
        return doc;
    }

    public static String getName(GraphQLType graphQLType) {
        if (graphQLType instanceof GraphQLNamedType) {
            return ((GraphQLNamedType) graphQLType).getName();
        }
        return "";
    }
}
