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

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;

@Internal
public class ExtendedBailStrategy extends BailErrorStrategy {
    private final MultiSourceReader multiSourceReader;

    public ExtendedBailStrategy(MultiSourceReader multiSourceReader) {
        this.multiSourceReader = multiSourceReader;
    }

    @Override
    public void recover(Parser recognizer, RecognitionException e) {
        try {
            super.recover(recognizer, e);
        } catch (ParseCancellationException parseException) {
            throw mkException(recognizer, e);
        }
    }

    @Override
    public Token recoverInline(Parser recognizer) throws RecognitionException {
        try {
            return super.recoverInline(recognizer);
        } catch (ParseCancellationException parseException) {
            throw mkException(recognizer, null);
        }
    }

    InvalidSyntaxException mkMoreTokensException(Token token) {
        SourceLocation sourceLocation = AntlrHelper.createSourceLocation(multiSourceReader, token);
        String sourcePreview = AntlrHelper.createPreview(multiSourceReader, token.getLine());
        return new InvalidSyntaxException(sourceLocation,
            "There are more tokens in the query that have not been consumed",
            sourcePreview, token.getText(), null);
    }


    private InvalidSyntaxException mkException(Parser recognizer, RecognitionException cause) {
        String sourcePreview = null;
        String offendingToken = null;
        SourceLocation sourceLocation = null;
        Token currentToken = recognizer.getCurrentToken();
        if (currentToken != null) {
            sourceLocation = AntlrHelper.createSourceLocation(multiSourceReader, currentToken);
            offendingToken = currentToken.getText();
            sourcePreview = AntlrHelper.createPreview(multiSourceReader, currentToken.getLine());
        }
        return new InvalidSyntaxException(sourceLocation, null, sourcePreview, offendingToken, cause);
    }

}
