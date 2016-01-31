/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.findUsages;

import com.intellij.find.findUsages.*;
import com.intellij.ide.DataManager;
import com.intellij.lang.jsgraphql.psi.JSGraphQLNamedPsiElement;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Find usages handler factory which includes the GraphQL schema virtual file in the "Project and Libraries" scope
 */
public class JSGraphQLFindUsagesHandlerFactory extends FindUsagesHandlerFactory {

    @Override
    public boolean canFindUsages(@NotNull PsiElement element) {
        if(!element.isValid()) {
            return false;
        }
        return element instanceof JSGraphQLNamedPsiElement;
    }

    @Nullable
    @Override
    public FindUsagesHandler createFindUsagesHandler(@NotNull PsiElement element, boolean forHighlightUsages) {

        if (canFindUsages(element)) {
            return new FindUsagesHandler(element){

                @NotNull
                @Override
                public AbstractFindUsagesDialog getFindUsagesDialog(boolean isSingleFile, boolean toShowInNewTab, boolean mustOpenInNewTab) {
                    final FindUsagesOptions options = getFindUsagesOptions(DataManager.getInstance().getDataContext());
                    return new JSGraphQLFindUsagesDialog(element, getProject(), options, toShowInNewTab, mustOpenInNewTab, isSingleFile, this);
                }

                @NotNull
                @Override
                public FindUsagesOptions getFindUsagesOptions(@Nullable DataContext dataContext) {
                    final FindUsagesOptions options = super.getFindUsagesOptions(dataContext);
                    addGraphQLSchemaFileToProjectAndLibrariesScope(options);
                    return options;
                }

            };
        }

        return null;

    }

    /**
     * includes the GraphQL schema virtual file in the provided "Project and Libraries" scope
     * @see JSGraphQLProjectAndLibrariesScope
     */
    private static void addGraphQLSchemaFileToProjectAndLibrariesScope(FindUsagesOptions options) {
        if(options.searchScope instanceof ProjectAndLibrariesScope) {
            final ProjectAndLibrariesScope projectAndLibrariesScope = (ProjectAndLibrariesScope) options.searchScope;
            options.searchScope = new JSGraphQLProjectAndLibrariesScope(projectAndLibrariesScope);
        }
    }

    /**
     * GraphQL find usages dialog which includes the GraphQL schema file as a library
     */
    private static class JSGraphQLFindUsagesDialog extends CommonFindUsagesDialog {

        public JSGraphQLFindUsagesDialog(@NotNull PsiElement element, @NotNull Project project, @NotNull FindUsagesOptions findUsagesOptions, boolean toShowInNewTab, boolean mustOpenInNewTab, boolean isSingleFile, @NotNull FindUsagesHandler handler) {
            super(element, project, findUsagesOptions, toShowInNewTab, mustOpenInNewTab, isSingleFile, handler);
        }

        @Override
        public void calcFindUsagesOptions(FindUsagesOptions options) {
            super.calcFindUsagesOptions(options);
            addGraphQLSchemaFileToProjectAndLibrariesScope(options);
        }
    }
}
