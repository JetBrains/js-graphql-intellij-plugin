package com.intellij.lang.jsgraphql.psi;

import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiParserFacade;
import org.jetbrains.annotations.NotNull;

public final class GraphQLElementFactory {
  public static @NotNull GraphQLFile createFile(@NotNull Project project, @NotNull String text) {
    return createFile(project, "dummy", text);
  }

  public static @NotNull GraphQLFile createFile(@NotNull Project project, @NotNull String fileName, @NotNull String text) {
    String filename = String.format("%s.%s", fileName, GraphQLFileType.INSTANCE.getDefaultExtension());
    return (GraphQLFile)PsiFileFactory.getInstance(project)
      .createFileFromText(filename, GraphQLFileType.INSTANCE, text);
  }

  public static @NotNull PsiElement createWhiteSpace(@NotNull Project project) {
    return createWhiteSpace(project, " ");
  }

  public static @NotNull PsiElement createNewLine(@NotNull Project project) {
    return createWhiteSpace(project, "\n");
  }

  public static @NotNull PsiElement createWhiteSpace(@NotNull Project project, @NotNull String text) {
    return PsiParserFacade.SERVICE.getInstance(project).createWhiteSpaceFromText(text);
  }
}
