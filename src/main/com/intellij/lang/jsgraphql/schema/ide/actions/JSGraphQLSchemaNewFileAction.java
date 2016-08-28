/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema.ide.actions;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;

public class JSGraphQLSchemaNewFileAction extends CreateFileFromTemplateAction implements DumbAware {

    public JSGraphQLSchemaNewFileAction() {
        super("GraphQL Schema File", "Creates a new GraphQL Schema file", JSGraphQLIcons.Files.GraphQLSchema);
    }

    @Override
    protected boolean isAvailable(DataContext dataContext) {
        if (!super.isAvailable(dataContext)) {
            return false;
        }
        final Module module = LangDataKeys.MODULE.getData(dataContext);
        return module != null;
    }

    @Override
    protected String getActionName(PsiDirectory directory, String newName, String templateName) {
        return "Create GraphQL Schema file " + newName;
    }

    @Override
    protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
        builder
                .setTitle("New GraphQL Schema File")
                .addKind("GraphQL Schema File", JSGraphQLIcons.Files.GraphQLSchema, "GraphQL Schema File");
    }
}