/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.lexer;

import com.intellij.lang.jsgraphql.v1.languageservice.api.Token;
import com.intellij.psi.tree.IElementType;

public class JSGraphQLToken {

    public final IElementType tokenType;
    public final Token sourceToken;

    public JSGraphQLToken(IElementType tokenType, Token sourceToken) {
        this.tokenType = tokenType;
        this.sourceToken = sourceToken;
    }

    @Override
    public String toString() {
        return "JSGraphQLToken{" +
                "tokenType=" + tokenType +
                ", sourceToken=" + sourceToken +
                '}';
    }
}
