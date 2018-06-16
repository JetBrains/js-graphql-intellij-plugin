/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql;

import com.intellij.lexer.FlexAdapter;

import java.io.Reader;

public class GraphQLLexerAdapter extends FlexAdapter {
  public GraphQLLexerAdapter() {
    super(new GraphQLLexer((Reader) null));
  }
}