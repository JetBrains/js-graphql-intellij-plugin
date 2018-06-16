/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.schema.ide.project;

import com.intellij.openapi.fileEditor.impl.EditorTabColorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.scope.NonProjectFilesScope;
import com.intellij.ui.FileColorManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * Applies the "non-project file" color to the project schema editor tab.
 */
public class JSGraphQLSchemaEditorTabColorProvider implements EditorTabColorProvider {

    @Nullable
    @Override
    public Color getEditorTabColor(@NotNull Project project, @NotNull VirtualFile file) {
        if (JSGraphQLSchemaLanguageProjectService.isProjectSchemaFile(file)) {
            FileColorManager colorManager = FileColorManager.getInstance(project);
            if (colorManager.isEnabledForTabs()) {
                return colorManager.getScopeColor(NonProjectFilesScope.NAME);
            }
        }
        return null;
    }
}
