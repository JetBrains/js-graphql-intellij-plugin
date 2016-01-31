/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class JSGraphQLTokenType extends IElementType {

    private String lexerTokenType;

    public JSGraphQLTokenType(@NotNull @NonNls String debugName) {
        super(debugName, JSGraphQLLanguage.INSTANCE);
        this.lexerTokenType = debugName.toLowerCase();
    }

    public String getLexerTokenType() {
        return lexerTokenType;
    }
}

