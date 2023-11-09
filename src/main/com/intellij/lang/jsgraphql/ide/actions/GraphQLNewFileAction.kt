/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.icons.GraphQLIcons
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory

class GraphQLNewFileAction : CreateFileFromTemplateAction(), DumbAware {

  override fun isAvailable(dataContext: DataContext): Boolean {
    if (!super.isAvailable(dataContext)) {
      return false
    }
    val module = LangDataKeys.MODULE.getData(dataContext)
    return module != null
  }

  override fun getActionName(directory: PsiDirectory, newName: String, templateName: String): String {
    return GraphQLBundle.message("graphql.action.create.file.from.template.name", newName)
  }

  override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
    builder
      .setTitle(GraphQLBundle.message("graphql.action.create.file.from.template.dialog.title"))
      .addKind(
        GraphQLBundle.message("graphql.action.create.file.from.template.dialog.kind"),
        GraphQLIcons.Files.GraphQL,
        "GraphQL File"
      )
  }
}
