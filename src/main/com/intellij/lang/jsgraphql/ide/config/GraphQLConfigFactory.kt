/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.config

import com.intellij.lang.jsgraphql.ide.notifications.GRAPHQL_NOTIFICATION_GROUP_ID
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import org.apache.commons.io.IOUtils
import java.io.IOException
import java.io.OutputStream
import java.util.function.Consumer

@Service(Service.Level.PROJECT)
class GraphQLConfigFactory(private val project: Project) {

    companion object {
        const val PREFERRED_CONFIG = GRAPHQL_CONFIG_YML

        @JvmStatic
        fun getInstance(project: Project) = project.service<GraphQLConfigFactory>()
    }

    @JvmOverloads
    fun createAndOpenConfigFile(
        configBaseDir: VirtualFile,
        openEditor: Boolean,
        outputStreamConsumer: Consumer<OutputStream> = Consumer { outputStream: OutputStream ->
            try {
                javaClass.classLoader.getResourceAsStream("META-INF/$PREFERRED_CONFIG")?.use { inputStream ->
                    IOUtils.copy(inputStream, outputStream)
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    ) {
        invokeLater {
            WriteCommandAction.runWriteCommandAction(project) {
                try {
                    val configFile = configBaseDir.createChildData(this, PREFERRED_CONFIG)
                    configFile.getOutputStream(this).use { stream -> outputStreamConsumer.accept(stream) }
                    val psiFile = PsiManager.getInstance(project).findFile(configFile)
                    if (psiFile != null) {
                        CodeStyleManager.getInstance(project).reformat(psiFile)
                    }
                    if (openEditor) {
                        FileEditorManager.getInstance(project).openFile(configFile, true, true)
                    }
                } catch (e: IOException) {
                    Notifications.Bus.notify(
                        Notification(
                            GRAPHQL_NOTIFICATION_GROUP_ID,
                            "Unable to create $PREFERRED_CONFIG",
                            "Unable to create file '$PREFERRED_CONFIG' in directory '${configBaseDir.path}': ${e.message}",
                            NotificationType.ERROR
                        )
                    )
                }
            }
        }
    }

    // TODO: migrate matching logic for the corner cases, e.g. generated schemas, scratches
    // @Nullable
    // public GraphQLNamedScope getSchemaScope(@Nullable VirtualFile virtualFile) {
    //     VirtualFile virtualFileWithPath = GraphQLPsiUtil.getPhysicalVirtualFile(virtualFile);
    //     if (virtualFileWithPath == null) return null;
    //
    //     try {
    //         readLock.lock();
    //         GraphQLNamedScope namedScope = virtualFilePathToScopes.computeIfAbsent(virtualFileWithPath.getPath(), path -> {
    //             VirtualFile configBaseDir = !ScratchUtil.isScratch(virtualFileWithPath)
    //                 ? getConfigBaseDir(virtualFileWithPath)
    //                 : getConfigBaseDirForScratch(virtualFileWithPath);
    //             if (configBaseDir == null) return NONE;
    //
    //             // locate the nearest config file, see https://github.com/kamilkisiela/graphql-config/tree/legacy/src/findGraphQLConfigFile.ts
    //             Ref<GraphQLNamedScope> scopeRef = Ref.create(NONE);
    //             GraphQLResolveUtil.processDirectoriesUpToContentRoot(myProject, configBaseDir, dir -> {
    //                 GraphQLConfigData configData = configFilesToConfigurations.get(dir);
    //                 if (configData == null) {
    //                     return true;
    //                 }
    //
    //                 // check projects first
    //                 if (configData.projects != null) {
    //                     final String projectKey = virtualFileWithPath.getUserData(GRAPHQL_SCRATCH_PROJECT_KEY);
    //                     for (Map.Entry<String, GraphQLResolvedConfigData> entry : configData.projects.entrySet()) {
    //                         if (projectKey != null && !projectKey.trim().isEmpty() && !projectKey.equals(entry.getKey())) {
    //                             // associated with another project so skip ahead
    //                             continue;
    //                         }
    //                         final GraphQLResolvedConfigData projectConfigData = entry.getValue();
    //                         final GraphQLConfigPackageSet packageSet = configDataToPackageSet.computeIfAbsent(projectConfigData,
    //                             dataKey -> {
    //                                 final GraphQLFile configEntryFile = getConfigurationEntryFile(dataKey);
    //                                 return new GraphQLConfigPackageSet(dir, configEntryFile, dataKey, configGlobMatcher);
    //                             });
    //                         if (packageSet.includesVirtualFile(virtualFileWithPath)) {
    //                             scopeRef.set(new GraphQLNamedScope("graphql-config:" + dir.getPath() + ":" + entry.getKey(), packageSet));
    //                             return false;
    //                         }
    //                     }
    //                 }
    //
    //                 // then top level config
    //                 final GraphQLConfigPackageSet packageSet = configDataToPackageSet.computeIfAbsent(configData, dataKey -> {
    //                     final GraphQLFile configEntryFile = getConfigurationEntryFile(dataKey);
    //                     return new GraphQLConfigPackageSet(dir, configEntryFile, dataKey, configGlobMatcher);
    //                 });
    //                 if (packageSet.includesVirtualFile(virtualFileWithPath)) {
    //                     scopeRef.set(new GraphQLNamedScope("graphql-config:" + dir.getPath(), packageSet));
    //                     return false;
    //                 }
    //                 return false;
    //             });
    //
    //             return scopeRef.get();
    //         });
    //
    //         return namedScope != NONE ? namedScope : null;
    //     } finally {
    //         readLock.unlock();
    //     }
    // }


    // TODO: move to GraphQLConfigProvider
    // /**
    //  * Resolves the logical configuration base dir for a scratch file that is placed outside the project by IntelliJ
    //  *
    //  * @param scratchVirtualFile the scratch file to resolve a configuration dir for
    //  * @return the resolved configuration base dir or null if none was found
    //  */
    // @Nullable
    // private VirtualFile getConfigBaseDirForScratch(@NotNull VirtualFile scratchVirtualFile) {
    //     final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(scratchVirtualFile);
    //
    //     // but look for a GRAPHQLCONFIG_COMMENT to override it
    //     if (psiFile != null) {
    //         scratchVirtualFile.putUserData(GRAPHQL_SCRATCH_PROJECT_KEY, null);
    //         PsiElement child = psiFile.getFirstChild();
    //         PsiElement element = child instanceof PsiComment ? child : PsiTreeUtil.skipWhitespacesForward(child);
    //         if (element instanceof PsiComment) {
    //             final String commentText = element.getText().trim();
    //             if (commentText.contains(GRAPHQLCONFIG_COMMENT)) {
    //                 String configFileName = StringUtil.substringAfter(commentText, GRAPHQLCONFIG_COMMENT);
    //                 if (configFileName != null) {
    //                     if (configFileName.contains("!")) {
    //                         final String projectKey = StringUtils.substringAfterLast(configFileName, "!");
    //                         scratchVirtualFile.putUserData(GRAPHQL_SCRATCH_PROJECT_KEY, projectKey);
    //                         configFileName = StringUtils.substringBeforeLast(configFileName, "!");
    //                     }
    //                     final VirtualFile configVirtualFile = scratchVirtualFile.getFileSystem().findFileByPath(configFileName.trim());
    //                     if (configVirtualFile != null) {
    //                         return configVirtualFile.isDirectory() ? configVirtualFile : configVirtualFile.getParent();
    //                     }
    //                 }
    //             }
    //         }
    //     }
    //
    //     try {
    //         readLock.lock();
    //         if (configFilesToConfigurations.size() == 1) {
    //             return configFilesToConfigurations.keySet().iterator().next();
    //         }
    //     } finally {
    //         readLock.unlock();
    //     }
    //
    //     return ProjectUtil.guessProjectDir(myProject);
    // }
}
