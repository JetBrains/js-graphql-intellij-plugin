/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.references;

import com.google.common.collect.Sets;
import com.intellij.json.JsonFileType;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileTypes.FileType;

import java.util.Set;

public class GraphQLFindUsagesUtil {

    private final Set<FileType> includedFileTypes = Sets.newHashSet();

    public GraphQLFindUsagesUtil() {
        includedFileTypes.add(GraphQLFileType.INSTANCE);
        includedFileTypes.add(JsonFileType.INSTANCE);
        final GraphQLFindUsagesFileTypeContributor[] contributors = GraphQLFindUsagesFileTypeContributor.EP_NAME.getExtensions();
        for (GraphQLFindUsagesFileTypeContributor contributor : contributors) {
            includedFileTypes.addAll(contributor.getFileTypes());
        }
    }

    public static GraphQLFindUsagesUtil getService() {
        return ServiceManager.getService(GraphQLFindUsagesUtil.class);
    }

    public Set<FileType> getIncludedFileTypes() {
        return includedFileTypes;
    }


}
