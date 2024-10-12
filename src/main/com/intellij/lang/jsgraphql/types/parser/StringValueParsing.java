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
import org.jetbrains.annotations.NotNull;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Contains parsing code for the StringValue types in the grammar
 */
@Internal
public final class StringValueParsing {
  private static final String ESCAPED_TRIPLE_QUOTES = "\\\\\"\"\""; // ahh Java + Regex
  private static final String THREE_QUOTES = "\"\"\"";

  public static @NotNull String parseTripleQuotedString(@NotNull String strText) {
    if (strText.length() <= 6) {
      return "";
    }

    int end = strText.length() - 3;
    String s = strText.substring(3, end);
    s = s.replaceAll(ESCAPED_TRIPLE_QUOTES, THREE_QUOTES);
    return removeIndentation(s);
  }

  /*
     See https://github.com/facebook/graphql/pull/327/files#diff-fe406b08746616e2f5f00909488cce66R758
   */
  public static String removeIndentation(String rawValue) {
    String[] lines = rawValue.split("\\n");
    Integer commonIndent = null;
    for (int i = 0; i < lines.length; i++) {
      if (i == 0) continue;
      String line = lines[i];
      int length = line.length();
      int indent = leadingWhitespace(line);
      if (indent < length) {
        if (commonIndent == null || indent < commonIndent) {
          commonIndent = indent;
        }
      }
    }
    List<String> lineList = new ArrayList<>(Arrays.asList(lines));
    if (commonIndent != null) {
      for (int i = 0; i < lineList.size(); i++) {
        String line = lineList.get(i);
        if (i == 0) continue;
        if (line.length() > commonIndent) {
          line = line.substring(commonIndent);
          lineList.set(i, line);
        }
      }
    }
    while (!lineList.isEmpty()) {
      String line = lineList.get(0);
      if (containsOnlyWhiteSpace(line)) {
        lineList.remove(0);
      }
      else {
        break;
      }
    }
    while (!lineList.isEmpty()) {
      int endIndex = lineList.size() - 1;
      String line = lineList.get(endIndex);
      if (containsOnlyWhiteSpace(line)) {
        lineList.remove(endIndex);
      }
      else {
        break;
      }
    }
    StringBuilder formatted = new StringBuilder();
    for (int i = 0; i < lineList.size(); i++) {
      String line = lineList.get(i);
      if (i == 0) {
        formatted.append(line);
      }
      else {
        formatted.append("\n");
        formatted.append(line);
      }
    }
    return formatted.toString();
  }

  private static int leadingWhitespace(String str) {
    int count = 0;
    for (int i = 0; i < str.length(); i++) {
      char ch = str.charAt(i);
      if (ch != ' ' && ch != '\t') {
        break;
      }
      count++;
    }
    return count;
  }

  private static boolean containsOnlyWhiteSpace(String str) {
    // according to graphql spec and graphql-js - this is the definition
    return leadingWhitespace(str) == str.length();
  }

  public static @NotNull String parseSingleQuotedString(@NotNull String string) {
    if (string.length() <= 2) {
      return "";
    }
    if (string.charAt(0) != string.charAt(string.length() - 1)) {
      return "";
    }

    StringWriter writer = new StringWriter(string.length() - 2);
    int end = string.length() - 1;
    for (int i = 1; i < end; i++) {
      char c = string.charAt(i);
      if (c != '\\') {
        writer.write(c);
        continue;
      }
      char escaped = string.charAt(i + 1);
      i += 1;
      switch (escaped) {
        case '"':
          writer.write('"');
          continue;
        case '/':
          writer.write('/');
          continue;
        case '\\':
          writer.write('\\');
          continue;
        case 'b':
          writer.write('\b');
          continue;
        case 'f':
          writer.write('\f');
          continue;
        case 'n':
          writer.write('\n');
          continue;
        case 'r':
          writer.write('\r');
          continue;
        case 't':
          writer.write('\t');
          continue;
        case 'u':
          int endIndex = i + 5;
          if (endIndex > end) {
            return "";
          }

          String hexStr = string.substring(i + 1, endIndex);
          try {
            int codepoint = Integer.parseInt(hexStr, 16);
            i += 4;
            writer.write(codepoint);
          }
          catch (NumberFormatException e) {
            return "";
          }
          continue;
        default:
          return "";
      }
    }
    return writer.toString();
  }
}
