/*
 * Copyright (c) 2019-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.references.kotlin;

import com.google.common.collect.Sets;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil;
import com.intellij.lang.jsgraphql.ide.references.GraphQLFindUsagesFileTypeContributor;
import com.intellij.openapi.fileTypes.FileType;
import kotlin.script.experimental.annotations.KotlinScript;
import org.jetbrains.kotlin.idea.KotlinFileType;

import java.util.Collection;
import java.util.Set;

/**
 * Registers the various JavaScript languages to support GraphQL in tagged templates
 */
public class KotlinGraphQLFindUsagesFileTypeContributor implements GraphQLFindUsagesFileTypeContributor {
    @Override
    public Collection<FileType> getFileTypes() {
        final Set<FileType> fileTypeSet = Sets.newHashSet();
        fileTypeSet.add(KotlinFileType.INSTANCE);
        return fileTypeSet;
    }
}
