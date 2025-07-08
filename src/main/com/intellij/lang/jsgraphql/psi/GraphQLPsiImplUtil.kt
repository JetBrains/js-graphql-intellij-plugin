package com.intellij.lang.jsgraphql.psi;

import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.jsgraphql.types.parser.StringValueParsing.parseSingleQuotedString;
import static com.intellij.lang.jsgraphql.types.parser.StringValueParsing.parseTripleQuotedString;

public final class GraphQLPsiImplUtil {
  private GraphQLPsiImplUtil() { }

  public static @NotNull String getValueAsString(@NotNull GraphQLStringValue stringValue) {
    GraphQLStringLiteral stringLiteral = stringValue.getStringLiteral();
    String text = stringLiteral.getText();
    boolean multiLine = text.startsWith("\"\"\"");
    if (multiLine) {
      return parseTripleQuotedString(text);
    }
    else {
      return parseSingleQuotedString(text);
    }
  }
}
