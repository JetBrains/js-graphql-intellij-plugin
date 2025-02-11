/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.formatter;

import com.intellij.formatting.*;
import org.jetbrains.annotations.NotNull;

public final class GraphQLFormattingModelBuilder implements FormattingModelBuilder {
  @Override
  public @NotNull FormattingModel createModel(@NotNull FormattingContext formattingContext) {
    return FormattingModelProvider.createFormattingModelForPsiFile(
      formattingContext.getContainingFile(),
      new GraphQLBlock(formattingContext.getNode(), Wrap.createWrap(WrapType.NONE, false), null),
      formattingContext.getCodeStyleSettings());
  }
}
