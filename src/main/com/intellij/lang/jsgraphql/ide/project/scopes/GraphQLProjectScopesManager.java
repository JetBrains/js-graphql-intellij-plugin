/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.scopes;

import com.google.common.collect.Maps;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.psi.search.scope.packageSet.NamedScopesHolder;
import com.intellij.ui.EditorNotifications;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Provides scopes for GraphQL virtual files based on the scopes configured in "Settings" > "Appearance & Behavior" > "Scopes"
 */
public class GraphQLProjectScopesManager {

    private final Project myProject;
    private final NamedScopesHolder[] holders;

    private final Map<String, NamedScope> filePathToScope = Maps.newConcurrentMap();

    public GraphQLProjectScopesManager(Project myProject) {
        this.myProject = myProject;
        holders = NamedScopesHolder.getAllNamedScopeHolders(myProject);
        for (NamedScopesHolder holder : holders) {
            holder.addScopeListener(() -> {
                filePathToScope.clear();
                UIUtil.invokeLaterIfNeeded(() -> {
                    EditorNotifications.getInstance(myProject).updateAllNotifications();
                });
            });
        }
    }

    public static GraphQLProjectScopesManager getService(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLProjectScopesManager.class);
    }

    public boolean isConfigured() {
        for (NamedScopesHolder holder : holders) {
            final NamedScope[] scopes = holder.getEditableScopes();
            if (scopes.length > 0) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public NamedScope getSchemaScope(VirtualFile virtualFile) {
        final Ref<VirtualFile> virtualFileWithPath = new Ref<>(virtualFile);
        if (virtualFile instanceof VirtualFileWindow) {
            // injected virtual files
            virtualFileWithPath.set(((VirtualFileWindow) virtualFile).getDelegate());
        }
        return filePathToScope.computeIfAbsent(virtualFileWithPath.get().getPath(), (path) -> {
            for (NamedScopesHolder holder : holders) {
                final NamedScope[] scopes = holder.getEditableScopes();  // don't need predefined scopes as we default to entire project
                for (NamedScope scope : scopes) {
                    final GlobalSearchScope filterSearchScope = GlobalSearchScopesCore.filterScope(myProject, scope);
                    if (filterSearchScope.contains(virtualFileWithPath.get())) {
                        return scope;
                    }
                }
            }
            return null;
        });
    }

}
