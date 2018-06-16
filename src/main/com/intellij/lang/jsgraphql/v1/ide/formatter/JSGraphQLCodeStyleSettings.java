/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.formatter;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

public class JSGraphQLCodeStyleSettings extends CustomCodeStyleSettings {
    public JSGraphQLCodeStyleSettings(CodeStyleSettings settings) {
        super("JSGraphQLCodeStyleSettings", settings);
    }
}