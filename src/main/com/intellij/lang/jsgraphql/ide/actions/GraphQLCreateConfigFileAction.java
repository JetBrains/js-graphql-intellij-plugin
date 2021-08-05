package com.intellij.lang.jsgraphql.ide.actions;

import com.intellij.ide.IdeView;
import com.intellij.lang.jsgraphql.icons.GraphQLIcons;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;

public class GraphQLCreateConfigFileAction extends AnAction {

    public GraphQLCreateConfigFileAction() {
        super("GraphQL Configuration File", "Creates a new GraphQL Configuration file (.graphqlconfig)", GraphQLIcons.Logos.GraphQL);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        if (e.getProject() != null) {
            final VirtualFile virtualFile = getActionDirectory(e);
            if (virtualFile != null) {
                GraphQLConfigManager.getService(e.getProject()).createAndOpenConfigFile(virtualFile, true);
                ApplicationManager.getApplication().saveAll();
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {

        boolean isAvailable = false;

        if (e.getProject() != null) {
            final DataContext dataContext = e.getDataContext();
            final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
            if (view != null && view.getDirectories().length > 0) {
                final Module module = LangDataKeys.MODULE.getData(dataContext);
                if (module != null) {
                    final VirtualFile actionDirectory = getActionDirectory(e);
                    if (actionDirectory != null) {
                        isAvailable = actionDirectory.findChild(GraphQLConfigManager.GRAPHQLCONFIG) == null;
                    }
                }
            }
        }

        final Presentation presentation = e.getPresentation();
        presentation.setVisible(isAvailable);
        presentation.setEnabled(isAvailable);
    }

    private VirtualFile getActionDirectory(AnActionEvent e) {
        VirtualFile virtualFile = e.getDataContext().getData(LangDataKeys.VIRTUAL_FILE);
        if (virtualFile != null) {
            if (!virtualFile.isDirectory()) {
                virtualFile = virtualFile.getParent();
            }
        }
        return virtualFile;
    }
}
