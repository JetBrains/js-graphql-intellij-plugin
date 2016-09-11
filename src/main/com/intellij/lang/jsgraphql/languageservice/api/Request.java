/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.languageservice.api;

import org.jetbrains.annotations.NotNull;

public abstract class Request {

    protected String command;
    protected String env;

    protected Request(@NotNull String command, String environment) {
        this.command = command;
        this.env = environment;
    }
}
