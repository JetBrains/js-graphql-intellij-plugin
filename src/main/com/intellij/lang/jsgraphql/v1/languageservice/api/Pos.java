/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.languageservice.api;

public class Pos {

    private final int line;
    private final int ch;

    public Pos(int line, int ch) {
        this.line = line;
        this.ch = ch;
    }

    public int getLine() {
        return line;
    }

    public int getCh() {
        return ch;
    }
}
