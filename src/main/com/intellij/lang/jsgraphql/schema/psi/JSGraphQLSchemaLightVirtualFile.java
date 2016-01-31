/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema.psi;

import com.intellij.lang.jsgraphql.schema.JSGraphQLSchemaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileAttributes;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.vfs.ex.temp.TempFileSystem;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an in-memory virtual file that backs a GraphQL Schema
 */
public class JSGraphQLSchemaLightVirtualFile extends LightVirtualFile {

    private final Project project;
    private VirtualFileSystem wrappedFileSystem;

    public JSGraphQLSchemaLightVirtualFile(LightVirtualFile delegate, Project project) {
        super(delegate.getName(), JSGraphQLSchemaFileType.INSTANCE, delegate.getContent());
        this.project = project;
    }


    @Override
    protected void setModificationStamp(long stamp) {
        // NO-OP - don't want the 'file was changed notification'
    }

    @NotNull
    @Override
    public String getPath() {
        return getName();
    }

    @NotNull
    @Override
    public VirtualFileSystem getFileSystem() {
        // NOTE!!!: we have to return the project file system in order to be automatically selectable in the project tree view
        // otherwise VfsUtil.isAncestor returns false in ProjectViewProjectNode.contains
        final VirtualFile parent = getParent();
        if(parent != null) {
            return getOrCreateFileSystemWrapper(parent.getFileSystem());
        }
        return getOrCreateFileSystemWrapper(super.getFileSystem());
    }

    @Override
    public VirtualFile getParent() {
        // show us as belonging to the project base directory in the navigation bar
        // this also enables the tree view to automatically select the node in "scroll from source"
        if(project.isDisposed()) {
            return null;
        }
        return project.getBaseDir();
    }

    /**
     * Returns a wrapped file system to provide the FileAttributes that FileChangedNotificationProvider#createNotificationPanel wants
     * to not show the "File was changed on disk" editor notification
     */
    private VirtualFileSystem getOrCreateFileSystemWrapper(final VirtualFileSystem virtualFileSystem) {

        if(wrappedFileSystem == null) {

            if(virtualFileSystem instanceof LocalFileSystem) {

                final JSGraphQLSchemaLightVirtualFile self = this;
                wrappedFileSystem = new TempFileSystem() {

                    @Override
                    @SuppressWarnings("UseVirtualFileEquals")
                    public FileAttributes getAttributes(@NotNull VirtualFile file) {
                        if(file == self) {
                            // prevent the "File was changed on disk" editor notification from prompting a reload
                            return new FileAttributes(false, false, false, false, self.getLength(), self.getTimeStamp(), true);
                        }
                        return super.getAttributes(file);
                    }

                    @Override
                    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
                    public boolean equals(Object o) {
                        // needed to be selected in the project view, specifically AbstractProjectNode.contains()
                        return virtualFileSystem.equals(o);
                    }

                };
            } else {
                wrappedFileSystem = virtualFileSystem;
            }
        }
        return this.wrappedFileSystem;
    }
}
