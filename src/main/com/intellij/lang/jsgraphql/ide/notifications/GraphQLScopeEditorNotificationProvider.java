/*
 *  Copyright (c) 2018-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.notifications;

import com.intellij.ide.impl.DataManagerImpl;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.ide.actions.GraphQLEditConfigAction;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.ide.references.GraphQLFindUsagesUtil;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import com.intellij.ui.EditorNotifications.Provider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Notifies the user that a GraphQL file (or file with injected GraphQL) is not included in the configured scopes
 */
public class GraphQLScopeEditorNotificationProvider extends Provider {

    private static final Key<EditorNotificationPanel> KEY = Key.create("GraphQLScopeEditorNotificationProvider");

    protected final Project myProject;
    private final EditorNotifications myNotifications;

    @NotNull
    public Key getKey() {
        return KEY;
    }

    public GraphQLScopeEditorNotificationProvider(Project project, EditorNotifications notifications) {
        myProject = project;
        myNotifications = notifications;
    }

    @Nullable
    public JComponent createNotificationPanel(@NotNull VirtualFile file, @NotNull FileEditor fileEditor) {
        return showNotification(file) ? new SchemaConfigEditorNotificationPanel() : null;
    }

    private boolean showNotification(@NotNull VirtualFile file) {
        if (!isGraphQLRelatedFile(file)) {
            return false;
        }
        if (file.getFileType() != GraphQLFileType.INSTANCE) {
            // for injected files, must contain graphql before the warning is shown
            final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);
            final Ref<Boolean> hasGraphQL = new Ref<>(false);
            if (psiFile != null) {
                final InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(myProject);
                psiFile.accept(new PsiRecursiveElementVisitor() {
                    @Override
                    public void visitElement(PsiElement element) {
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
        if (GlobalSearchScope.projectScope(myProject).accept(file)) {
            final GraphQLConfigManager graphQLConfigManager = GraphQLConfigManager.getService(myProject);
            if (graphQLConfigManager.getClosestConfigFile(file) != null) {
                if (graphQLConfigManager.getClosestIncludingConfigFile(file) == null) {
                    // has config, but is not included
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isGraphQLRelatedFile(VirtualFile file) {
        return GraphQLFindUsagesUtil.INCLUDED_FILE_TYPES.contains(file.getFileType());
    }

    protected class SchemaConfigEditorNotificationPanel extends EditorNotificationPanel {

        SchemaConfigEditorNotificationPanel() {
            final String scopeMessage = "Schema discovery and language tooling will use entire project.";
            setText("The .graphqlconfig associated with this file does not include it. " + scopeMessage);
            createActionLabel("Edit .graphqlconfig globs", () -> {
                final GraphQLEditConfigAction action = new GraphQLEditConfigAction();
                final AnActionEvent actionEvent = AnActionEvent.createFromDataContext(
                        ActionPlaces.UNKNOWN,
                        null,
                        new DataManagerImpl.MyDataContext(this)
                );
                action.actionPerformed(actionEvent);
            });
        }

    }

}
