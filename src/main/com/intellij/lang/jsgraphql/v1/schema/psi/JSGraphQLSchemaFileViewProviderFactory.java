/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.schema.psi;

import com.intellij.lang.Language;
import com.intellij.lang.jsgraphql.v1.schema.JSGraphQLSchemaFileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.FileViewProviderFactory;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * Wraps a GraphQL LightVirtualFile in a {@link JSGraphQLSchemaLightVirtualFile} to enable proper navigation and selection in the project tree view
 */
public class JSGraphQLSchemaFileViewProviderFactory implements FileViewProviderFactory {

    @NotNull
    @Override
    public FileViewProvider createFileViewProvider(@NotNull VirtualFile file, Language language, @NotNull PsiManager manager, boolean eventSystemEnabled) {
        if(file instanceof LightVirtualFile) {
            final LightVirtualFile lightVirtualFile = (LightVirtualFile) file;
            final JSGraphQLSchemaLightVirtualFile schemaLightVirtualFile;
            if(file instanceof JSGraphQLSchemaLightVirtualFile) {
                schemaLightVirtualFile = (JSGraphQLSchemaLightVirtualFile) file;
            } else {
                schemaLightVirtualFile = new JSGraphQLSchemaLightVirtualFile(lightVirtualFile);
            }
            return new JSGraphQLSchemaFileViewProvider(manager, schemaLightVirtualFile, eventSystemEnabled, language, JSGraphQLSchemaFileType.INSTANCE);
        } else {
            return new JSGraphQLSchemaFileViewProvider(manager, file, eventSystemEnabled, language, JSGraphQLSchemaFileType.INSTANCE);
        }
    }

}
