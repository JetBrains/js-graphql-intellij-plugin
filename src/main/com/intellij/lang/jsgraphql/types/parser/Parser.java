/*
    The MIT License (MIT)

    Copyright (c) 2015 Andreas Marek and Contributors

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
    (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
    publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
    so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
    OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
    CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.intellij.lang.jsgraphql.types.parser;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.Document;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;
import com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlLexer;
import com.intellij.lang.jsgraphql.types.parser.antlr.GraphqlParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.List;

@PublicApi
public class Parser {


    public static Document parse(String input) {
        return new Parser().parseDocument(input);
    }

    public Document parseDocument(String input) throws InvalidSyntaxException {
        return parseDocument(input, null);
    }

    public Document parseDocument(String input, String sourceName) throws InvalidSyntaxException {
        MultiSourceReader multiSourceReader = MultiSourceReader.newMultiSourceReader()
                .string(input, sourceName)
                .trackData(true)
                .build();
        return parseDocument(multiSourceReader);
    }

    public Document parseDocument(Reader reader) throws InvalidSyntaxException {
        MultiSourceReader multiSourceReader;
        if (reader instanceof MultiSourceReader) {
            multiSourceReader = (MultiSourceReader) reader;
        } else {
            multiSourceReader = MultiSourceReader.newMultiSourceReader()
                    .reader(reader, null).build();
        }
        CodePointCharStream charStream;
        try {
            charStream = CharStreams.fromReader(multiSourceReader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        GraphqlLexer lexer = new GraphqlLexer(charStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                SourceLocation sourceLocation = AntlrHelper.createSourceLocation(multiSourceReader, line, charPositionInLine);
                String preview = AntlrHelper.createPreview(multiSourceReader, line);
                throw new InvalidSyntaxException(sourceLocation, "Invalid syntax: " + msg, preview, null, null);
            }
        });

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        GraphqlParser parser = new GraphqlParser(tokens);
        parser.removeErrorListeners();
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);

        ExtendedBailStrategy bailStrategy = new ExtendedBailStrategy(multiSourceReader);
        parser.setErrorHandler(bailStrategy);

        GraphqlAntlrToLanguage toLanguage = getAntlrToLanguage(tokens, multiSourceReader);
        GraphqlParser.DocumentContext documentContext = parser.document();

        Document doc = toLanguage.createDocument(documentContext);

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
                throw bailStrategy.mkMoreTokensException(last);
            }
        }
        return doc;
    }

    /**
     * Allows you to override the ANTLR to AST code.
     *
     * @param tokens            the token stream
     * @param multiSourceReader the source of the query document
     * @return a new GraphqlAntlrToLanguage instance
     */
    protected GraphqlAntlrToLanguage getAntlrToLanguage(CommonTokenStream tokens, MultiSourceReader multiSourceReader) {
        return new GraphqlAntlrToLanguage(tokens, multiSourceReader);
    }
}
