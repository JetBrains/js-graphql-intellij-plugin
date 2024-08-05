/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql;

import com.intellij.ide.scratch.ScratchUtil;
import com.intellij.lang.jsgraphql.icons.GraphQLIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public final class GraphQLFileType extends LanguageFileType {
  public static final GraphQLFileType INSTANCE = new GraphQLFileType();

  private GraphQLFileType() {
    super(GraphQLLanguage.INSTANCE);
  }

  @Override
  public @NonNls @NotNull String getName() {
    return GraphQLConstants.GraphQL;
  }

  @Override
  public @NlsContexts.Label @NotNull String getDescription() {
    return GraphQLConstants.GraphQL;
  }

  @Override
  public @NotNull String getDefaultExtension() {
    return "graphql";
  }

  @Override
  public @NotNull Icon getIcon() {
    return GraphQLIcons.FILE;
  }

  /**
   * @deprecated Use {@link GraphQLFileType#isGraphQLScratchFile(VirtualFile)} instead.
   */
  @Deprecated(forRemoval = true)
  public static boolean isGraphQLScratchFile(@SuppressWarnings("unused") @NotNull Project project, @Nullable VirtualFile file) {
    return isGraphQLScratchFile(file);
  }

  public static boolean isGraphQLScratchFile(@Nullable VirtualFile file) {
    if (file == null) {
      return false;
    }
    return ScratchUtil.isScratch(file) && file.getFileType() == INSTANCE;
  }

  /**
   * @deprecated Use {@link GraphQLFileType#isGraphQLFile(VirtualFile)} instead.
   */
  @Deprecated(forRemoval = true)
  public static boolean isGraphQLFile(@SuppressWarnings("unused") @NotNull Project project, @Nullable VirtualFile file) {
    return isGraphQLFile(file);
  }

  public static boolean isGraphQLFile(@Nullable VirtualFile file) {
    if (file == null) {
      return false;
    }
    return file.getFileType() == INSTANCE;
  }
}
