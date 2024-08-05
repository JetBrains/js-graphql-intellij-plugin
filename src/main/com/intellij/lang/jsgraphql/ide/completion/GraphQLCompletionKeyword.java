package com.intellij.lang.jsgraphql.ide.completion;

import org.jetbrains.annotations.NotNull;

public enum GraphQLCompletionKeyword {
  QUERY("query"),
  SUBSCRIPTION("subscription"),
  MUTATION("mutation"),
  FRAGMENT("fragment"),
  SCHEMA("schema"),
  SCALAR("scalar"),
  TYPE("type"),
  INTERFACE("interface"),
  INPUT("input"),
  ENUM("enum"),
  UNION("union"),
  DIRECTIVE("directive"),
  EXTEND("extend"),
  IMPLEMENTS("implements"),
  ON("on"),
  REPEATABLE("repeatable"),
  TRUE("true"),
  FALSE("false");

  private final @NotNull String myText;

  GraphQLCompletionKeyword(@NotNull String text) {
    myText = text;
  }

  public @NotNull String getText() {
    return myText;
  }
}
