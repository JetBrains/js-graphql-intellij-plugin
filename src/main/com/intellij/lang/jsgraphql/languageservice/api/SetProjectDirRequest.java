/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.languageservice.api;

import org.jetbrains.annotations.NotNull;

public class SetProjectDirRequest extends Request {

    private final String projectDir;

    public SetProjectDirRequest(@NotNull String projectDir) {
        super("setProjectDir", false);
        this.projectDir = projectDir;
    }
}
