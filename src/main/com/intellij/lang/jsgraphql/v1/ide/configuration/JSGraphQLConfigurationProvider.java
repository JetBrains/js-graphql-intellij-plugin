/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.configuration;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.lang.jsgraphql.endpoint.psi.*;
import com.intellij.lang.jsgraphql.v1.ide.endpoints.JSGraphQLEndpoint;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

public class JSGraphQLConfigurationProvider extends VirtualFileAdapter {

    private final static Logger log = Logger.getInstance(JSGraphQLConfigurationProvider.class);
    private final static Key<String> GRAPHQL_CONFIG_BASE_DIR_URL = Key.create("jsgraphql.config.baseDir");
    private final static Key<String> GRAPHQL_CONFIG_ENDPOINT_ENTRY_FILE_URL = Key.create("jsgraphql.config.entryDir");

    public static final String GRAPHQL_CONFIG_JSON = "graphql.config.json";
    public static final String GRAPHQL_DEFAULT_SCHEMA = "schema.graphql";

    private Project myProject;
    private List<JSGraphQLEndpoint> endpoints;
    private final Object endpointsLock = new Object();
    private PluginDescriptor pluginDescriptor;

    public JSGraphQLConfigurationProvider(@NotNull Project myProject) {
        this.myProject = myProject;
        this.pluginDescriptor = PluginManager.getPlugin(PluginId.getId("com.intellij.lang.jsgraphql"));
        VirtualFileManager.getInstance().addVirtualFileListener(this);
        detectConfigurationBaseDir();
    }

    public static JSGraphQLConfigurationProvider getService(@NotNull Project project) {
        return ServiceManager.getService(project, JSGraphQLConfigurationProvider.class);
    }

    @Nullable
    public VirtualFile getConfigurationBaseDir() {
        final String baseDirUrl = myProject.getUserData(GRAPHQL_CONFIG_BASE_DIR_URL);
        if(baseDirUrl != null) {
            final VirtualFile baseDir = VirtualFileManager.getInstance().findFileByUrl(baseDirUrl);
            if(baseDir != null) {
                return baseDir;
            }
        }
        return null;
    }

    public void setConfigurationBasDirFromModule(Module module) {
        for (VirtualFile virtualFile : ModuleRootManager.getInstance(module).getContentRoots()) {
            if(virtualFile.isValid() && virtualFile.isDirectory()) {
                myProject.putUserData(GRAPHQL_CONFIG_BASE_DIR_URL, virtualFile.getUrl());
                break;
            }
        }
    }

    @Nullable
    public String getConfigurationBasePath() {
        final VirtualFile configurationBaseDir = getConfigurationBaseDir();
        return configurationBaseDir != null ? configurationBaseDir.getPath() : null;
    }

    @Nullable
    public VirtualFile getGraphQLConfigFile() {
        final VirtualFile configurationBaseDir = getConfigurationBaseDir();
        if(configurationBaseDir != null) {
            return configurationBaseDir.findFileByRelativePath(JSGraphQLConfigurationProvider.GRAPHQL_CONFIG_JSON);
        }
        return null;
    }

    public boolean hasGraphQLConfig() {
        return getGraphQLConfigFile() != null;
    }

    public VirtualFile getEndpointEntryFile() {
        final String entryFileUrl = myProject.getUserData(GRAPHQL_CONFIG_ENDPOINT_ENTRY_FILE_URL);
        if(entryFileUrl != null) {
            return VirtualFileManager.getInstance().findFileByUrl(entryFileUrl);
        }
        final VirtualFile configFile = getGraphQLConfigFile();
        if(configFile != null) {
            final JSGraphQLConfiguration configuration = getConfiguration(configFile);
            if (configuration != null && configuration.schema != null && configuration.schema.endpoint != null) {
                final String entryRelativeFileName = configuration.schema.endpoint.entry;
                if(StringUtil.isNotEmpty(entryRelativeFileName) && configFile.getParent() != null) {
                    final VirtualFile entryFile = configFile.getParent().findFileByRelativePath(entryRelativeFileName);
                    if(entryFile != null) {
                        myProject.putUserData(GRAPHQL_CONFIG_ENDPOINT_ENTRY_FILE_URL, entryFile.getUrl());
                        return entryFile;
                    }
                }
            }
        }
        return null;
    }

    public VirtualFile getOrCreateFile(String name) {
        VirtualFile baseDir = getConfigurationBaseDir();
        if(baseDir == null) {
            log.warn("Attempted to create '" + name + "' before base dir was detected");
            return null;
        }
        VirtualFile file = baseDir.findFileByRelativePath(name);
        if(file == null) {
            Ref<VirtualFile> fileRef = new Ref<>();
            ApplicationManager.getApplication().runWriteAction(() -> {
                try {
                    fileRef.set(baseDir.createChildData(this, name));
                    try(OutputStream stream = fileRef.get().getOutputStream(this)) {
                        try(InputStream inputStream = pluginDescriptor.getPluginClassLoader().getResourceAsStream("/META-INF/"+name)) {
                            if(inputStream != null) {
                                IOUtils.copy(inputStream, stream);
                            }
                        }
                    }
                } catch (IOException e) {
                    UIUtil.invokeLaterIfNeeded(() -> {
                        Notifications.Bus.notify(new Notification("GraphQL", "JS GraphQL", "Unable to create file '" + name + "' in directory '" + baseDir.getPath() + "': " + e.getMessage(), NotificationType.ERROR));
                    });
                }
            });
            return fileRef.get();
        }
        return file;
    }

    public List<JSGraphQLEndpoint> getEndpoints() {
        final VirtualFile configFile = getGraphQLConfigFile();
        if(configFile != null) {
            final JSGraphQLConfiguration configuration = getConfiguration(configFile);
            if(configuration != null) {
                synchronized (endpointsLock) {
                    if(endpoints == null) {
                        endpoints = configuration.endpoints;
                    }
                }
                return endpoints;
            }
        }
        return Lists.newArrayList(); // no config yet
    }

    public List<JSGraphQLSchemaEndpointAnnotation> getEndpointAnnotations(PsiFile psiFile) {
        final List<JSGraphQLSchemaEndpointAnnotation> annotations = Lists.newArrayList();
        final VirtualFile configFile = getGraphQLConfigFile();
        if(configFile != null) {
            final JSGraphQLConfiguration configuration = getConfiguration(configFile);
            if (configuration != null) {
                if (configuration.schema != null && configuration.schema.endpoint != null) {
                    if(configuration.schema.endpoint.annotations != null) {
	                    annotations.addAll(configuration.schema.endpoint.annotations);
                    }
                }
            }
        }

        // also include in-language annotations (could eventually remove the need for the config file annotations)
        final Collection<JSGraphQLEndpointTypeResult<JSGraphQLEndpointAnnotationDefinition>> languageAnnotations = JSGraphQLEndpointPsiUtil.getKnownDefinitionNames(
                psiFile,
                JSGraphQLEndpointAnnotationDefinition.class,
                false
        );
        for (JSGraphQLEndpointTypeResult<JSGraphQLEndpointAnnotationDefinition> languageAnnotation : languageAnnotations) {
            final JSGraphQLSchemaEndpointAnnotation annotationConfig = new JSGraphQLSchemaEndpointAnnotation();
            annotationConfig.name = languageAnnotation.name;
            final JSGraphQLEndpointArgumentsDefinition argumentsDefinition = languageAnnotation.element.getArgumentsDefinition();
            if(argumentsDefinition != null && argumentsDefinition.getInputValueDefinitions() != null) {
                for (JSGraphQLEndpointInputValueDefinition argument : argumentsDefinition.getInputValueDefinitions().getInputValueDefinitionList()) {
                    final JSGraphQLSchemaEndpointAnnotationArgument argumentConfig = new JSGraphQLSchemaEndpointAnnotationArgument();
                    argumentConfig.name = argument.getInputValueDefinitionIdentifier().getText();
                    if(argument.getCompositeType() != null) {
                        argumentConfig.type = argument.getCompositeType().getText();
                    }
                    annotationConfig.arguments.add(argumentConfig);
                }
            }
            annotations.add(annotationConfig);
        }

        return annotations;
    }

    // ---- VirtualFileListener ----

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent event) {
        final VirtualFile file = event.getFile();
        if(GRAPHQL_CONFIG_JSON.equals(file.getName())) {
            if(myProject.isDisposed()) {
                // the project has been disposed, so this instance isn't needed anymore
                VirtualFileManager.getInstance().removeVirtualFileListener(this);
                return;
            }
            if(file.equals(getGraphQLConfigFile())) {
                final JSGraphQLConfiguration configuration = getConfiguration(file);
                if(configuration != null && configuration.endpoints != null) {
                    synchronized (endpointsLock) {
                        if(endpoints == null) {
                            endpoints = configuration.endpoints;
                        } else {
                            for(int i = 0; i < configuration.endpoints.size(); i++) {
                                if(i < endpoints.size()) {
                                    // update existing instances since they may already be selected in open editors
                                    endpoints.get(i).withPropertiesFrom(configuration.endpoints.get(i));
                                } else {
                                    endpoints.add(configuration.endpoints.get(i));
                                }
                            }
                            if(configuration.endpoints.size() < endpoints.size()) {
                                // one or more endpoints deleted
                                endpoints.removeIf(ep -> !configuration.endpoints.contains(ep));
                            }
                        }
                    }

                    // signal the change
                    myProject.getMessageBus().syncPublisher(JSGraphQLConfigurationListener.TOPIC).onEndpointsChanged(endpoints);

                }
                myProject.putUserData(GRAPHQL_CONFIG_ENDPOINT_ENTRY_FILE_URL, null);
            }
        }
    }


    // ---- implementation ----

    private void detectConfigurationBaseDir() {

        VirtualFile baseDir = myProject.getBaseDir();
        String detectedBaseDirUrl = null;
        if(baseDir != null) {
            VirtualFile configFile = baseDir.findFileByRelativePath(GRAPHQL_CONFIG_JSON);
            if(configFile != null) {
                // found the file in the base dir
                detectedBaseDirUrl = baseDir.getUrl();
            } else {
                // if the project base dir is part of the project files then we use it even though the file doesn't exist yet
                if(!DumbService.getInstance(myProject).isDumb() && ProjectRootManager.getInstance(myProject).getFileIndex().isInContent(baseDir)) {
                    detectedBaseDirUrl = baseDir.getUrl();
                }
            }
        }

        if(detectedBaseDirUrl == null && !DumbService.getInstance(myProject).isDumb()) {
            // try to locate the config file using the file name index first (which is fastest but not available while indexing)
            final PsiFile[] filesByName = FilenameIndex.getFilesByName(myProject, GRAPHQL_CONFIG_JSON, GlobalSearchScope.projectScope(myProject));
            for (PsiFile psiFile : filesByName) {
                final VirtualFile virtualFile = psiFile.getVirtualFile();
                if(virtualFile != null && virtualFile.isValid()) {
                    final VirtualFile parent = virtualFile.getParent();
                    if(parent != null && parent.isDirectory()) {
                        detectedBaseDirUrl = parent.getUrl();
                    }
                    break;
                }
            }
        }

        if(detectedBaseDirUrl == null) {
            // look for the config file in the other content roots (the modules)
            final VirtualFile[] contentRoots = ProjectRootManager.getInstance(myProject).getContentRoots();
            for (VirtualFile contentRoot : contentRoots) {
                VirtualFile configFile = contentRoot.findFileByRelativePath(GRAPHQL_CONFIG_JSON);
                if(configFile != null) {
                    detectedBaseDirUrl = contentRoot.getUrl();
                    break;
                }
            }
            // if we didn't find the config file, and we've only got one content root we'll use it
            if(detectedBaseDirUrl == null && contentRoots.length == 1) {
                detectedBaseDirUrl = contentRoots[0].getUrl();
            }
        }

        if(detectedBaseDirUrl != null) {
            myProject.putUserData(GRAPHQL_CONFIG_BASE_DIR_URL, detectedBaseDirUrl);
        }
    }


    private JSGraphQLConfiguration getConfiguration(VirtualFile file) {
        try {
            try(InputStream inputStream = file.getInputStream()) {
                final String json = IOUtils.toString(inputStream, "UTF-8");
                try {
                    return new Gson().fromJson(json, JSGraphQLConfiguration.class);
                } catch (JsonSyntaxException je) {
                    log.warn("Invalid JSON in config file", je);
                }
            }
        } catch (IOException e) {
            log.warn("Unable to read from config file", e);
        }
        return null;
    }

}
