/*
 * Copyright (c) 2019-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.findUsages;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.fileTypes.FileType;

import java.util.Collection;

/**
 * Contributor that enables the plugin to locate GraphQL in additional file types such as JavaScript
 */
public interface GraphQLFindUsagesFileTypeContributor {

    ExtensionPointName<GraphQLFindUsagesFileTypeContributor> EP_NAME = ExtensionPointName.create("com.intellij.lang.jsgraphql.findUsagesFileTypeContributor");

    /**
     * A list of additional file types that the plugin should process to locate GraphQL in a project
     */
    Collection<FileType> getFileTypes();

}
