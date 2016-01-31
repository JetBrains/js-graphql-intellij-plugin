/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.findUsages;

import com.google.common.collect.Sets;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil;
import com.intellij.lang.jsgraphql.JSGraphQLFileType;
import com.intellij.lang.jsgraphql.schema.JSGraphQLSchemaFileType;
import com.intellij.openapi.fileTypes.FileType;

import java.util.Set;

public class JSGraphQLFindUsagesUtil {

    public final static Set<FileType> INCLUDED_FILE_TYPES = Sets.newHashSet();

    static {
        INCLUDED_FILE_TYPES.add(JSGraphQLFileType.INSTANCE);
        INCLUDED_FILE_TYPES.add(JSGraphQLSchemaFileType.INSTANCE);
        INCLUDED_FILE_TYPES.addAll(TypeScriptUtil.TYPESCRIPT_FILE_TYPES);
        INCLUDED_FILE_TYPES.addAll(DialectDetector.JAVASCRIPT_FILE_TYPES);
    }


}
