/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.editor;

import com.intellij.lang.Commenter;
import org.jetbrains.annotations.Nullable;

public final class GraphQLCommenter implements Commenter {
  @Override
  public @Nullable String getLineCommentPrefix() {
    return "#";
  }

  @Override
  public @Nullable String getBlockCommentPrefix() {
    return null;
  }

  @Override
  public @Nullable String getBlockCommentSuffix() {
    return null;
  }

  @Override
  public @Nullable String getCommentedBlockCommentPrefix() {
    return null;
  }

  @Override
  public @Nullable String getCommentedBlockCommentSuffix() {
    return null;
  }
}
