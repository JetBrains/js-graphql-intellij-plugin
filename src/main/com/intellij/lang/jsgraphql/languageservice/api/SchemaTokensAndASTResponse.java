/**
 * Copyright (c) 2015, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.languageservice.api;

import java.util.Map;

public class SchemaTokensAndASTResponse extends TokensResponse {

    Map<String, Object> ast;

}
