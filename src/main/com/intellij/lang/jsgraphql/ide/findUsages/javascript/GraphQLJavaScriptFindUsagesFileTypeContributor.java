/*
 * Copyright (c) 2019-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.findUsages.javascript;

import com.google.common.collect.Sets;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil;
import com.intellij.lang.jsgraphql.ide.findUsages.GraphQLFindUsagesFileTypeContributor;
import com.intellij.openapi.fileTypes.FileType;

import java.util.Collection;
import java.util.Set;

/**
 * Registers the various JavaScript languages to support GraphQL in tagged templates
 */
public class GraphQLJavaScriptFindUsagesFileTypeContributor implements GraphQLFindUsagesFileTypeContributor {
    @Override
    public Collection<FileType> getFileTypes() {
        final Set<FileType> fileTypeSet = Sets.newHashSet();
        fileTypeSet.addAll(TypeScriptUtil.TYPESCRIPT_FILE_TYPES);
        fileTypeSet.addAll(DialectDetector.JAVASCRIPT_FILE_TYPES);
        return fileTypeSet;
    }
}
