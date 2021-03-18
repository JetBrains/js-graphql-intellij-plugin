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
import org.antlr.v4.runtime.Token;

import java.util.List;

@Internal
public class AntlrHelper {

    public static SourceLocation createSourceLocation(MultiSourceReader multiSourceReader, int antrlLine, int charPositionInLine) {
        // multi source reader lines are 0 based while Antler lines are 1's based
        //
        // Antler columns ironically are 0 based - go figure!
        //
        int tokenLine = antrlLine - 1;
        MultiSourceReader.SourceAndLine sourceAndLine = multiSourceReader.getSourceAndLineFromOverallLine(tokenLine);
        //
        // graphql spec says line numbers and columns start at 1
        int line = sourceAndLine.getLine() + 1;
        int column = charPositionInLine + 1;
        return new SourceLocation(line, column, sourceAndLine.getSourceName());

    }

    public static SourceLocation createSourceLocation(MultiSourceReader multiSourceReader, Token token) {
        return AntlrHelper.createSourceLocation(multiSourceReader, token.getLine(), token.getCharPositionInLine());
    }


    /* grabs 3 lines before and after the syntax error */
    public static String createPreview(MultiSourceReader multiSourceReader, int antrlLine) {
        int line = antrlLine - 1;
        StringBuilder sb = new StringBuilder();
        int startLine = line - 3;
        int endLine = line + 3;
        List<String> lines = multiSourceReader.getData();
        for (int i = 0; i < lines.size(); i++) {
            if (i >= startLine && i <= endLine) {
                sb.append(lines.get(i)).append('\n');
            }
        }
        return sb.toString();

    }

}
