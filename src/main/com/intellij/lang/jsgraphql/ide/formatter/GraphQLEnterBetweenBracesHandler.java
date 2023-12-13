/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.formatter;

import com.intellij.codeInsight.editorActions.enter.EnterBetweenBracesDelegate;

/**
 * Adds a new indented line when pressing enter between [] in an empty GraphQL list
 * in addition to handling pairs of {} and () braces by a default implementation.
 */
public final class GraphQLEnterBetweenBracesHandler extends EnterBetweenBracesDelegate {

  @Override
  protected boolean isBracePair(char c1, char c2) {
    return super.isBracePair(c1, c2) || c1 == '[' && c2 == ']';
  }
}
