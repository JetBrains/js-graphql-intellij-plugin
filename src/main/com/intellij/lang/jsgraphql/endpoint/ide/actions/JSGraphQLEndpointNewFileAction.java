/*
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.actions;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.lang.jsgraphql.icons.GraphQLIcons;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigData;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;

public class JSGraphQLEndpointNewFileAction extends CreateFileFromTemplateAction implements DumbAware {

    public JSGraphQLEndpointNewFileAction() {
        super("GraphQL Endpoint File", "Creates a new GraphQL Endpoint file", GraphQLIcons.Files.GraphQLSchema);
    }

    @Override
    protected boolean isAvailable(DataContext dataContext) {
        if (!super.isAvailable(dataContext)) {
            return false;
        }

        final Project myProject = CommonDataKeys.PROJECT.getData(dataContext);
        if (myProject != null) {
            final VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
            if (virtualFile != null) {
                final GraphQLConfigManager configManager = GraphQLConfigManager.getService(myProject);
                final VirtualFile configFile = configManager.getClosestConfigFile(virtualFile);
                if (configFile != null) {
                    final GraphQLConfigData configData = configManager.getConfigurationsByPath().get(configFile.getParent());
                    if (configData != null && configData.extensions != null) {
                        return configData.extensions.get(GraphQLConfigManager.ENDPOINT_LANGUAGE_EXTENSION) != null;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected String getActionName(PsiDirectory directory, String newName, String templateName) {
        return "Create GraphQL Endpoint file " + newName;
    }

    @Override
    protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
        builder
                .setTitle("New GraphQL Endpoint File")
                .addKind("GraphQL Endpoint File", GraphQLIcons.Files.GraphQLSchema, "GraphQL Endpoint File");
    }
}
