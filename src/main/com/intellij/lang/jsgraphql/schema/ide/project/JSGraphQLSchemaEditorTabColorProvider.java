/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema.ide.project;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.impl.EditorTabColorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.scope.NonProjectFilesScope;
import com.intellij.ui.Colored;
import com.intellij.ui.FileColorManager;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * Applies the "non-project file" color to the project schema editor tab.
 */
public class JSGraphQLSchemaEditorTabColorProvider implements EditorTabColorProvider {

    private static final Logger log = Logger.getInstance(JSGraphQLSchemaEditorTabColorProvider.class);

    private Color light;
    private Color dark;

    public JSGraphQLSchemaEditorTabColorProvider() {
        Colored color = NonProjectFilesScope.class.getAnnotation(Colored.class);
        if (color != null) {
            try {
                dark = Color.decode("#" + color.darkVariant());
                light = Color.decode("#" + color.color());
            } catch (Exception e) {
                log.error("Unable to parse color config", e);
            }
        }
    }

    @Nullable
    @Override
    public Color getEditorTabColor(@NotNull Project project, @NotNull VirtualFile file) {
        if (JSGraphQLSchemaLanguageProjectService.isProjectSchemaFile(file)) {
            FileColorManager colorManager = FileColorManager.getInstance(project);
            if (colorManager.isEnabledForTabs()) {
                if (UIUtil.isUnderDarcula()) {
                    return dark;
                } else {
                    return light;
                }
            }
        }
        return null;
    }
}
