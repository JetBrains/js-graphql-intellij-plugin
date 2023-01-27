/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.actions;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.ide.notifications.GraphQLNotificationUtil;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLResolveUtil;
import com.intellij.lang.jsgraphql.psi.GraphQLPsiUtil;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaKeys;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.CommonProcessors;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
        final Project project = e.getData(CommonDataKeys.PROJECT);
        final VirtualFile virtualFile = GraphQLPsiUtil.getPhysicalVirtualFile(e.getData(CommonDataKeys.VIRTUAL_FILE));
        if (project == null || virtualFile == null) {
            return;
        }

        final GraphQLConfigManager configManager = GraphQLConfigManager.getService(project);
        final VirtualFile configFile = configManager.getClosestConfigFile(virtualFile);
        if (configFile != null) {
            final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            fileEditorManager.openFile(configFile, true, true);
        } else {
            // no config associated, ask to create one
            String message = "Searched current and parent directories.<br><a href=\"create\">Create .graphqlconfig file</a>";
            Notifications.Bus.notify(new Notification(
                GraphQLNotificationUtil.NOTIFICATION_GROUP_ID,
                "No .graphqlconfig file found",
                message,
                NotificationType.INFORMATION,
                (notification, event) -> createConfig(project, virtualFile, configManager, notification)
            ), project);
        }
    }

    private void createConfig(Project project, VirtualFile virtualFile, GraphQLConfigManager configManager, Notification notification) {
        Collection<VirtualFile> configDirectoryCandidates = getParentDirsUpToContentRoots(project, virtualFile);

        if (configDirectoryCandidates.size() == 1) {
            configManager.createAndOpenConfigFile(ContainerUtil.getFirstItem(configDirectoryCandidates), true);
            notification.expire();
        } else {
            final GraphQLConfigDirectoryDialog dialog = new GraphQLConfigDirectoryDialog(project, configDirectoryCandidates);
            if (dialog.showAndGet() && dialog.getSelectedDirectory() != null) {
                configManager.createAndOpenConfigFile(dialog.getSelectedDirectory(), true);
                notification.expire();
            }
        }
    }

    @NotNull
    private static Collection<VirtualFile> getParentDirsUpToContentRoots(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        Collection<VirtualFile> configDirectoryCandidates;
        if (GraphQLFileType.isGraphQLScratchFile(project, virtualFile)) {
            configDirectoryCandidates = Collections.singletonList(ProjectUtil.guessProjectDir(project));
        } else {
            CommonProcessors.CollectProcessor<VirtualFile> directoriesProcessor = new CommonProcessors.CollectProcessor<>();
            GraphQLResolveUtil.processDirectoriesUpToContentRoot(project, virtualFile, directoriesProcessor);
            configDirectoryCandidates = directoriesProcessor.getResults();
        }
        return configDirectoryCandidates;
    }

    static class GraphQLConfigDirectoryDialog extends DialogWrapper {

        private final List<PsiDirectory> configDirectoryCandidates;
        private ComboBox<PsiDirectory> comboBox;

        GraphQLConfigDirectoryDialog(@NotNull Project project, Collection<VirtualFile> configDirectoryCandidates) {
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
