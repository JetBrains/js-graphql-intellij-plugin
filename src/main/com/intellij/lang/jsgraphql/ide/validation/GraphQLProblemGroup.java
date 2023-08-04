package com.intellij.lang.jsgraphql.ide.validation;

import com.intellij.lang.annotation.ProblemGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraphQLProblemGroup implements ProblemGroup {
  private final String myName;

  public GraphQLProblemGroup(@NotNull String name) {
    myName = name;
  }

  @Override
  public @Nullable String getProblemName() {
    return myName;
  }
}
