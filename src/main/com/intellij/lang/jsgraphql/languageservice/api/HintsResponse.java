/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.languageservice.api;

import com.google.common.collect.Lists;

import java.util.List;

public class HintsResponse {

    private List<Hint> hints = Lists.newArrayList();
    private Pos from;
    private Pos to;

    public List<Hint> getHints() {
        return hints;
    }

    public Pos getFrom() {
        return from;
    }

    public Pos getTo() {
        return to;
    }
}
