/*
 *  Copyright (c) 2018-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.notifications;

import com.intellij.ide.DataManager;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.ide.actions.GraphQLEditConfigAction;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.ide.references.GraphQLFindUsagesUtil;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications.Provider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Notifies the user that a GraphQL file (or file with injected GraphQL) is not included in the configured scopes
 */
public class GraphQLScopeEditorNotificationProvider extends Provider<EditorNotificationPanel> {

    private static final Key<EditorNotificationPanel> KEY = Key.create("GraphQLScopeEditorNotificationProvider");

    @NotNull
    public Key<EditorNotificationPanel> getKey() {
        return KEY;
    }

    @Override
    public @Nullable EditorNotificationPanel createNotificationPanel(@NotNull VirtualFile file,
                                                                     @NotNull FileEditor fileEditor,
                                                                     @NotNull Project project) {
        return showNotification(file, project) ? createPanel() : null;
    }

    @NotNull
    private EditorNotificationPanel createPanel() {
        EditorNotificationPanel panel = new EditorNotificationPanel();
        panel.setText(GraphQLBundle.message("graphql.notification.file.out.of.scope"));
        panel.createActionLabel(GraphQLBundle.message("graphql.notification.file.out.of.scope.edit"), () -> {
            final GraphQLEditConfigAction action = new GraphQLEditConfigAction();
            final AnActionEvent actionEvent = AnActionEvent.createFromDataContext(
                "GraphQLConfigEditorNotification",
                null,
                DataManager.getInstance().getDataContext(panel)
            );
            action.actionPerformed(actionEvent);
        });
        return panel;
    }

    private boolean showNotification(@NotNull VirtualFile file, @NotNull Project project) {
        if (ApplicationManager.getApplication().isUnitTestMode()) {
            // injection enumerate below causes missing JSON schemas in 2019.2 for eslint
            return false;
        }
        if (!isGraphQLRelatedFile(file) || DumbService.getInstance(project).isDumb()) {
            return false;
        }
        final GraphQLConfigManager graphQLConfigManager = GraphQLConfigManager.getService(project);
        if (!graphQLConfigManager.isInitialized()) {
            return false;
        }
        if (file.getFileType() != GraphQLFileType.INSTANCE) {
            // for injected files, must contain graphql before the warning is shown
            final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            final Ref<Boolean> hasGraphQL = new Ref<>(false);
            if (psiFile != null) {
                final InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(project);
                psiFile.accept(new PsiRecursiveElementVisitor() {
                    @Override
                    public void visitElement(@NotNull PsiElement element) {
                        if (hasGraphQL.get()) {
                            return;
                        }
                        if (element instanceof PsiLanguageInjectionHost) {
                            injectedLanguageManager.enumerate(element, (injectedPsi, places) -> {
                                if (injectedPsi instanceof GraphQLFile) {
                                    hasGraphQL.set(true);
                                }
                            });
                        } else {
                            super.visitElement(element);
                        }
                    }
                });
            }
            if (!hasGraphQL.get()) {
                return false;
            }
        }
        if (GlobalSearchScope.projectScope(project).accept(file)) {
            if (graphQLConfigManager.getClosestConfigFile(file) != null) {
                // has config, but is not included
                return graphQLConfigManager.getClosestIncludingConfigFile(file) == null;
            }
        }
        return false;
    }

    private boolean isGraphQLRelatedFile(VirtualFile file) {
        return GraphQLFindUsagesUtil.getService().getIncludedFileTypes().contains(file.getFileType());
    }
}
