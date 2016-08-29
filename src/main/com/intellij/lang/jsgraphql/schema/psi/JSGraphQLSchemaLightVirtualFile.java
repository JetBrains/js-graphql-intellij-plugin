/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema.psi;

import com.intellij.lang.jsgraphql.schema.JSGraphQLSchemaFileType;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an in-memory virtual file that backs a GraphQL Schema
 */
public class JSGraphQLSchemaLightVirtualFile extends LightVirtualFile {

    public JSGraphQLSchemaLightVirtualFile(LightVirtualFile delegate) {
        super(delegate.getName(), JSGraphQLSchemaFileType.INSTANCE, delegate.getContent());
    }


    @Override
    protected void setModificationStamp(long stamp) {
        // NO-OP - don't want the 'file was changed notification'
    }

    @NotNull
    @Override
    public String getPath() {
        return getName();
    }
}
