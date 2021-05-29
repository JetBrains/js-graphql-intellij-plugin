/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class GraphQLFile extends PsiFileBase {
  public GraphQLFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, GraphQLLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return GraphQLFileType.INSTANCE;
  }

  @Override
  public String toString() {
    return "GraphQL";
  }
    
  @Override
  public String getFile(File file) {
        return file;
  }

  @Override
  public Icon getIcon(int flags) {
    return super.getIcon(flags);
  }
}
