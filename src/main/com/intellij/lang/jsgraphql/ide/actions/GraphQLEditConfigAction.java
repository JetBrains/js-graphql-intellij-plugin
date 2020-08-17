/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.actions;

import com.google.common.collect.Lists;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaKeys;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.components.panels.NonOpaquePanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class GraphQLEditConfigAction extends AnAction {

    private static final String SETTINGS_TOOLTIP = "Edit .graphqlconfig file (GraphQL project structure and endpoints)";

    public GraphQLEditConfigAction() {
        super(SETTINGS_TOOLTIP, SETTINGS_TOOLTIP, AllIcons.General.Settings);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        final VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean isEnabled = virtualFile == null
                || !Boolean.TRUE.equals(virtualFile.getUserData(GraphQLSchemaKeys.IS_GRAPHQL_INTROSPECTION_SDL));
        e.getPresentation().setEnabled(isEnabled);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project myProject = e.getData(CommonDataKeys.PROJECT);
        final VirtualFile virtualFile = getVirtualFileOnDisk(e.getData(CommonDataKeys.VIRTUAL_FILE));
        if (myProject != null && virtualFile != null) {
            final GraphQLConfigManager configManager = GraphQLConfigManager.getService(myProject);
            final VirtualFile configFile = configManager.getClosestConfigFile(virtualFile);
            if (configFile != null) {
                final FileEditorManager fileEditorManager = FileEditorManager.getInstance(myProject);
                fileEditorManager.openFile(configFile, true, true);
            } else {
                // no config associated, ask to create one
                String message = "Searched current and parent directories.<br><a href=\"create\">Create .graphqlconfig file</a>";
                Notifications.Bus.notify(new Notification("GraphQL", "No .graphqlconfig file found", message, NotificationType.INFORMATION, (notification, event) -> {
                    final Set<VirtualFile> contentRoots = Optional.ofNullable(configManager.getContentRoots(virtualFile)).orElse(Collections.emptySet());
                    VirtualFile directory = virtualFile.getParent();
                    assert directory != null;
                    final List<VirtualFile> configDirectoryCandidates = Lists.newArrayList(directory);
                    while (!contentRoots.contains(directory)) {
                        directory = directory.getParent();
                        if (directory != null) {
                            configDirectoryCandidates.add(directory);
                        } else {
                            break;
                        }
                    }
                    if (configDirectoryCandidates.size() == 1) {
                        configManager.createAndOpenConfigFile(configDirectoryCandidates.get(0), true);
                        notification.expire();
                    } else {
                        final GraphQLConfigDirectoryDialog dialog = new GraphQLConfigDirectoryDialog(myProject, configDirectoryCandidates);
                        if (dialog.showAndGet()) {
                            if (dialog.getSelectedDirectory() != null) {
                                configManager.createAndOpenConfigFile(dialog.getSelectedDirectory(), true);
                                notification.expire();
                            }
                        }
                    }
                }), myProject);
            }
        }
    }

    @Nullable
    private VirtualFile getVirtualFileOnDisk(@Nullable VirtualFile virtualFile) {
        if (virtualFile instanceof LightVirtualFile) {
            virtualFile = ((LightVirtualFile) virtualFile).getOriginalFile();
        }
        if (virtualFile instanceof VirtualFileWindow) {
            virtualFile = ((VirtualFileWindow) virtualFile).getDelegate();
        }
        return virtualFile;
    }

    static class GraphQLConfigDirectoryDialog extends DialogWrapper {

        private final List<PsiDirectory> configDirectoryCandidates;
        private ComboBox<PsiDirectory> comboBox;

        GraphQLConfigDirectoryDialog(@NotNull Project project, List<VirtualFile> configDirectoryCandidates) {
            super(project);
            final PsiDirectoryFactory factory = PsiDirectoryFactory.getInstance(project);
            this.configDirectoryCandidates = configDirectoryCandidates.stream().map(factory::createDirectory).collect(Collectors.toList());
            setTitle("Select GraphQL Configuration Folder");
            init();
            comboBox.requestFocus();
        }

        @Nullable
        @Override
        public JComponent getPreferredFocusedComponent() {
            return comboBox;
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            comboBox = new ComboBox<>(new CollectionComboBoxModel<>(configDirectoryCandidates));
            comboBox.setRenderer(new DefaultPsiElementCellRenderer());
            comboBox.setMinimumAndPreferredWidth(450);
            if (comboBox.getItemCount() > 0) {
                comboBox.setSelectedIndex(0);
            }
            final NonOpaquePanel panel = new NonOpaquePanel();
            panel.add(comboBox, BorderLayout.NORTH);
            return panel;
        }

        VirtualFile getSelectedDirectory() {
            final PsiDirectory selectedItem = (PsiDirectory) comboBox.getSelectedItem();
            return selectedItem != null ? selectedItem.getVirtualFile() : null;
        }
    }

}
