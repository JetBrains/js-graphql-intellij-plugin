/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.completion;

import com.intellij.lang.jsgraphql.v1.ide.configuration.JSGraphQLConfigurationProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang.StringUtils;

public class JSGraphQLEndpointImportUtil {


    public static String getImportName(Project project, PsiFile file) {
        final VirtualFile entryFile = JSGraphQLConfigurationProvider.getService(project).getEndpointEntryFile(file);
        final VirtualFile entryFileDir = entryFile != null ? entryFile.getParent() : null;
        final String name = StringUtils.substringBeforeLast(file.getName(), ".");
        return getImportName(entryFileDir, file.getVirtualFile(), name);
    }

    public static String getImportName(VirtualFile entryFileDir, VirtualFile virtualFile, String name) {
        if(entryFileDir != null) {
            VirtualFile parentDir = virtualFile.getParent();
            while(parentDir != null && !parentDir.equals(entryFileDir)) {
                name = parentDir.getName() + '/' + name;
                parentDir = parentDir.getParent();
            }
        }
        return name;
    }

}
