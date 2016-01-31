/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema.ide.project;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.BasePsiNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.lang.jsgraphql.ide.configuration.JSGraphQLConfigurationProvider;
import com.intellij.lang.jsgraphql.schema.psi.JSGraphQLSchemaFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.messages.MessageBusConnection;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class JSGraphQLSchemaFileNode extends BasePsiNode<PsiFile> implements JSGraphQLSchemaLanguageServiceListener {

    private final VirtualFile virtualFile;
    private final MessageBusConnection connection;

    protected JSGraphQLSchemaFileNode(Project project, PsiFile value, ViewSettings viewSettings) {
        super(project, value, viewSettings);
        virtualFile = value.getVirtualFile();
        connection = project.getMessageBus().connect(project);
        connection.subscribe(JSGraphQLSchemaLanguageServiceListener.TOPIC, this);
    }

    @Nullable
    @Override
    protected Collection<AbstractTreeNode> getChildrenImpl() {
        return null;
    }

    @Nullable
    @Override
    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

    @Override
    public boolean contains(@NotNull VirtualFile file) {
        return Boolean.TRUE.equals(file.getUserData(JSGraphQLSchemaLanguageProjectService.IS_GRAPHQL_SCHEMA_VIRTUAL_FILE));
    }

    @Override
    public boolean canRepresent(final Object element) {
        // let intellij know that this node represents JSGraphQLSchemaFile such that it can be automatically
        // expanded and selected -- see com.intellij.ide.projectView.BaseProjectTreeBuilder#expandPathTo
        return element instanceof JSGraphQLSchemaFile;
    }

    @Override
    protected void updateImpl(PresentationData data) {
        updatePresentationData(data);
    }


    // ---- JSGraphQLSchemaLanguageServiceListener ----

    @Override
    public void onSchemaReloaded() {
        apply(updatePresentationData(new PresentationData()));
    }


    // ---- implementation ---

    private PresentationData updatePresentationData(PresentationData data) {
        PsiFile value = getValue();
        data.setPresentableText(value.getName());
        data.setIcon(value.getIcon(Iconable.ICON_FLAG_READ_STATUS));
        final Project project = getProject();
        if(project != null && !project.isDisposed()) {
            String schemaUrl = JSGraphQLSchemaLanguageProjectService.getService(project).getSchemaUrl();
            if (StringUtils.isNotEmpty(schemaUrl)) {
                final VirtualFile baseDir = JSGraphQLConfigurationProvider.getService(project).getConfigurationBaseDir();
                if(baseDir != null) {
                    final String comparableSchemaUrl = schemaUrl.replace("\\", "/");
                    final String comparableBaseDir = baseDir.getPath().replace("\\", "/");
                    if(comparableSchemaUrl.startsWith(comparableBaseDir)) {
                        schemaUrl = schemaUrl.substring(comparableBaseDir.length());
                        if(schemaUrl.length() > 1 && (schemaUrl.startsWith("/") || schemaUrl.startsWith("\\"))) {
                            schemaUrl = schemaUrl.substring(1);
                        }
                    }
                }
                data.setLocationString(schemaUrl);
            }
        }
        return data;
    }

}
