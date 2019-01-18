/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.graphqlconfig;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.scratch.ScratchFileType;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.GraphQLScopeResolution;
import com.intellij.lang.jsgraphql.GraphQLSettings;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigData;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigEndpoint;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLResolvedConfigData;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.v1.ide.configuration.JSGraphQLConfigurationListener;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileCopyEvent;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.EditorNotifications;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages integration with graphql-config files. See https://github.com/prismagraphql/graphql-config
 */
public class GraphQLConfigManager {

    public static final String GRAPHQLCONFIG = ".graphqlconfig";
    public static final String GRAPHQLCONFIG_COMMENT = ".graphqlconfig=";

    private static final String GRAPHQLCONFIG_YML = ".graphqlconfig.yml";
    private static final String GRAPHQLCONFIG_YAML = ".graphqlconfig.yaml";

    private static final String[] GRAPHQLCONFIG_FILE_NAMES = {
            GRAPHQLCONFIG,
            GRAPHQLCONFIG_YML,
            GRAPHQLCONFIG_YAML
    };

    private static final GraphQLNamedScope NONE = new GraphQLNamedScope("", null);

    private final Project myProject;
    private final GlobalSearchScope projectScope;
    private final GraphQLConfigGlobMatcher graphQLConfigGlobMatcher;
    public final IdeaPluginDescriptor pluginDescriptor;

    private volatile Map<String, GraphQLConfigData> configPathToConfigurations = Maps.newConcurrentMap();
    private final Map<String, GraphQLNamedScope> virtualFilePathToScopes = Maps.newConcurrentMap();
    private VirtualFileListener virtualFileListener;

    public GraphQLConfigManager(Project myProject) {
        this.myProject = myProject;
        this.projectScope = GlobalSearchScope.projectScope(myProject);
        this.graphQLConfigGlobMatcher = ServiceManager.getService(myProject, GraphQLConfigGlobMatcher.class);
        this.pluginDescriptor = PluginManager.getPlugin(PluginId.getId("com.intellij.lang.jsgraphql"));
    }

    public static GraphQLConfigManager getService(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLConfigManager.class);
    }

    /**
     * Gets the closest .graphqlconfig{.yml,.yaml} file that includes the specified file
     *
     * @param virtualFile the file to get the config file for
     *
     * @return the closest config file that includes the specified virtualFile, or null if none found or not included
     */
    public VirtualFile getClosestIncludingConfigFile(VirtualFile virtualFile) {
        GraphQLNamedScope schemaScope = getSchemaScope(virtualFile);
        if (schemaScope == null) {
            return null;
        }
        final GraphQLConfigPackageSet packageSet = (GraphQLConfigPackageSet) schemaScope.getValue();
        if (packageSet != null) {
            for (String fileName : GRAPHQLCONFIG_FILE_NAMES) {
                VirtualFile configFile = packageSet.getConfigBaseDir().findChild(fileName);
                if (configFile != null) {
                    return configFile;
                }
            }
        }
        return null;
    }

    /**
     * Gets the closest .graphqlconfig{.yml,.yaml} file even though it doesn't include the specified file.
     */
    public VirtualFile getClosestConfigFile(VirtualFile virtualFile) {
        final Set<VirtualFile> contentRoots = getContentRoots(virtualFile);
        VirtualFile directory;
        if(virtualFile.getFileType() == ScratchFileType.INSTANCE) {
            directory = getConfigBaseDirForScratch(virtualFile);
        } else {
            directory = virtualFile.getParent();
        }
        while (directory != null) {
            for (String fileName : GRAPHQLCONFIG_FILE_NAMES) {
                VirtualFile configFile = directory.findChild(fileName);
                if (configFile != null) {
                    return configFile;
                }
            }
            if (contentRoots != null && contentRoots.contains(directory)) {
                // don't step outside the module content roots
                break;
            }
            directory = directory.getParent();
        }
        return null;
    }

    public void createAndOpenConfigFile(VirtualFile configBaseDir, Boolean openEditor) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                final VirtualFile configFile = configBaseDir.createChildData(this, GRAPHQLCONFIG);
                try (OutputStream stream = configFile.getOutputStream(this)) {
                    try (InputStream inputStream = pluginDescriptor.getPluginClassLoader().getResourceAsStream("/META-INF/" + GRAPHQLCONFIG)) {
                        if (inputStream != null) {
                            IOUtils.copy(inputStream, stream);
                        }
                    }
                }
                if (openEditor) {
                    UIUtil.invokeLaterIfNeeded(() -> {
                        FileEditorManager.getInstance(myProject).openFile(configFile, true, true);
                    });
                }
            } catch (IOException e) {
                Notifications.Bus.notify(new Notification("GraphQL", "Unable to create " + GRAPHQLCONFIG, "Unable to create file '" + GRAPHQLCONFIG + "' in directory '" + configBaseDir.getPath() + "': " + e.getMessage(), NotificationType.ERROR));
            }
        });
    }

    /**
     * Gets the endpoints that are within scope for the specified GraphQL virtual file.
     *
     * If the file is not a GraphQL file, null is returned
     */
    @SuppressWarnings("unchecked")
    public List<GraphQLConfigEndpoint> getEndpoints(VirtualFile virtualFile) {

        if (virtualFile.getFileType() != GraphQLFileType.INSTANCE && !GraphQLFileType.isGraphQLScratchFile(myProject, virtualFile)) {
            return null;
        }

        final GraphQLNamedScope schemaScope = getSchemaScope(virtualFile);
        if (schemaScope != null) {
            GraphQLConfigPackageSet packageSet = (GraphQLConfigPackageSet) schemaScope.getValue();
            if (packageSet != null && packageSet.getConfigData() != null) {
                Map<String, Object> extensions = packageSet.getConfigData().extensions;
                if (extensions != null) {
                    final Object endpointsValue = extensions.get("endpoints");
                    if (endpointsValue instanceof Map) {
                        final String configPath = packageSet.getConfigBaseDir().getPath();
                        final List<GraphQLConfigEndpoint> result = Lists.newArrayList();
                        ((Map<String, Object>) endpointsValue).forEach((key, value) -> {
                            if (value instanceof String) {
                                result.add(new GraphQLConfigEndpoint(configPath, key, (String) value));
                            } else if (value instanceof Map) {
                                Object url = ((Map) value).get("url");
                                Object headers = ((Map) value).get("headers");
                                if (url instanceof String) {
                                    GraphQLConfigEndpoint endpoint = new GraphQLConfigEndpoint(configPath, key, (String) url);
                                    if (headers instanceof Map) {
                                        endpoint.headers = (Map<String, Object>) headers;
                                    }
                                    result.add(endpoint);
                                }
                            }
                        });
                        return result;
                    }
                }
            }
        }

        // NOTE: modifiable list since it powers the endpoint UI and must support item operations
        return Lists.newArrayListWithExpectedSize(1);
    }

    void initialize() {
        virtualFileListener = new VirtualFileListener() {

            @Override
            public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
                onVirtualFileChange(event);
            }

            @Override
            public void contentsChanged(@NotNull VirtualFileEvent event) {
                onVirtualFileChange(event);
            }

            @Override
            public void fileDeleted(@NotNull VirtualFileEvent event) {
                onVirtualFileChange(event);
            }

            @Override
            public void fileCreated(@NotNull VirtualFileEvent event) {
                onVirtualFileChange(event);
            }

            @Override
            public void fileCopied(@NotNull VirtualFileCopyEvent event) {
                onVirtualFileChange(event);
            }

            @Override
            public void fileMoved(@NotNull VirtualFileMoveEvent event) {
                onVirtualFileChange(event);
            }


        };
        VirtualFileManager.getInstance().addVirtualFileListener(virtualFileListener);

        PsiManager.getInstance(myProject).addPsiTreeChangeListener(new PsiTreeChangeAdapter() {

            @Override
            public void childReplaced(@NotNull PsiTreeChangeEvent event) {
                if (event.getFile() instanceof GraphQLFile && event.getFile().getVirtualFile().getFileType() == ScratchFileType.INSTANCE) {
                    if (hasGraphQLConfigComment(event.getNewChild()) || hasGraphQLConfigComment(event.getOldChild())) {
                        // updated the .graphqlconfig comment in a scratch comment which associates the scratch with a scope
                        // so clear the cached path to scope entry in virtualFilePathToScopes
                        virtualFilePathToScopes.keySet().remove(event.getFile().getVirtualFile().getPath());
                    }
                }
            }
        });

        buildConfigurationModel();

    }

    private boolean hasGraphQLConfigComment(PsiElement psiElement) {
        if (psiElement instanceof PsiComment) {
            return psiElement.getText().contains(GRAPHQLCONFIG_COMMENT);
        }
        return false;
    }

    private void onVirtualFileChange(VirtualFileEvent event) {
        if (myProject.isDisposed()) {
            VirtualFileManager.getInstance().removeVirtualFileListener(virtualFileListener);
            return;
        }
        String oldName = null;
        if (event instanceof VirtualFilePropertyEvent) {
            final VirtualFilePropertyEvent propertyEvent = (VirtualFilePropertyEvent) event;
            if (VirtualFile.PROP_NAME.equals(propertyEvent.getPropertyName())) {
                oldName = String.valueOf(propertyEvent.getOldValue());
            }
        }
        final String[] names = new String[]{event.getFile().getName(), oldName};
        for (String name : names) {
            if (name == null) {
                continue;
            }
            if (name.startsWith(GRAPHQLCONFIG)) {
                if (name.length() == GRAPHQLCONFIG.length() || name.equals(GRAPHQLCONFIG_YML) || name.equals(GRAPHQLCONFIG_YAML)) {
                    buildConfigurationModel();
                    break;
                }
            }

        }
    }

    private void buildConfigurationModel() {

        final Map<String, GraphQLConfigData> newConfigPathToConfigurations = Maps.newConcurrentMap();

        // JSON format
        final Collection<VirtualFile> jsonFiles = FilenameIndex.getVirtualFilesByName(myProject, GRAPHQLCONFIG, projectScope);
        final Gson gson = new Gson();
        for (VirtualFile jsonFile : jsonFiles) {
            try {
                final String jsonText = new String(jsonFile.contentsToByteArray(), jsonFile.getCharset());
                final GraphQLConfigData graphQLConfigData = gson.fromJson(jsonText, GraphQLConfigData.class);
                if(graphQLConfigData != null) {
                    newConfigPathToConfigurations.put(jsonFile.getParent().getPath(), graphQLConfigData);
                }
            } catch (IOException | JsonSyntaxException e) {
                createParseErrorNotification(jsonFile, e);
            }
        }

        // YAML format
        final Collection<VirtualFile> ymlFiles = FilenameIndex.getVirtualFilesByName(myProject, GRAPHQLCONFIG_YML, projectScope);
        final Collection<VirtualFile> yamlFiles = FilenameIndex.getVirtualFilesByName(myProject, GRAPHQLCONFIG_YAML, projectScope);
        if (!ymlFiles.isEmpty() || !yamlFiles.isEmpty()) {
            final Representer representer = new Representer();
            representer.getPropertyUtils().setSkipMissingProperties(true);
            final Yaml yaml = new Yaml(new Constructor(GraphQLConfigData.class), representer);
            Streams.concat(ymlFiles.stream(), yamlFiles.stream()).forEach(yamlFile -> {
                try {
                    final String yamlText = new String(yamlFile.contentsToByteArray(), yamlFile.getCharset());
                    final GraphQLConfigData graphQLConfigData = yaml.load(yamlText);
                    newConfigPathToConfigurations.putIfAbsent(yamlFile.getParent().getPath(), graphQLConfigData);
                } catch (IOException | YAMLException e) {
                    createParseErrorNotification(yamlFile, e);
                }
            });
        }

        // suggest graphql-config should be used to resolve the schema if applicable
        if (!newConfigPathToConfigurations.isEmpty()) {
            boolean suggestAsScopes = false;
            for (GraphQLConfigData graphQLConfigData : newConfigPathToConfigurations.values()) {
                if (hasScopeSettings(graphQLConfigData)) {
                    suggestAsScopes = true;
                    break;
                }
            }
            if (suggestAsScopes) {
                GraphQLSettings settings = GraphQLSettings.getSettings(myProject);
                if (settings.getScopeResolution() != GraphQLScopeResolution.GRAPHQL_CONFIG_GLOBS) {
                    Notification enableGraphQLConfig = new Notification("GraphQL", "graphl-config file globs detected", "<a href=\"enable\">Use graphql-config globs</a> to discover schemas", NotificationType.INFORMATION, (notification, event) -> {
                        settings.setScopeResolution(GraphQLScopeResolution.GRAPHQL_CONFIG_GLOBS);
                        ApplicationManager.getApplication().saveSettings();
                        notification.expire();
                        EditorNotifications.getInstance(myProject).updateAllNotifications();
                    });
                    enableGraphQLConfig.setImportant(true);
                    Notifications.Bus.notify(enableGraphQLConfig);
                }
            }
        }

        // apply defaults to projects as spec'ed in https://github.com/prismagraphql/graphql-config/blob/master/specification.md#default-configuration-properties
        for (GraphQLConfigData baseConfig : newConfigPathToConfigurations.values()) {
            if (baseConfig.projects != null) {
                baseConfig.projects.forEach((projectName, projectConfig) -> {
                    if (projectConfig.name == null) {
                        projectConfig.name = projectName;
                    }
                    if (projectConfig.schemaPath == null) {
                        projectConfig.schemaPath = baseConfig.schemaPath;
                    }
                    if (projectConfig.includes == null) {
                        projectConfig.includes = baseConfig.includes;
                    }
                    if (projectConfig.excludes == null) {
                        projectConfig.excludes = baseConfig.excludes;
                    }
                    if (projectConfig.extensions == null) {
                        projectConfig.extensions = baseConfig.extensions;
                    } else if (baseConfig.extensions != null) {
                        for (Map.Entry<String, Object> extension : baseConfig.extensions.entrySet()) {
                            projectConfig.extensions.put(extension.getKey(), extension.getValue());
                        }
                    }
                });
            }
        }

        this.configPathToConfigurations = newConfigPathToConfigurations;
        this.virtualFilePathToScopes.clear();

        myProject.getMessageBus().syncPublisher(JSGraphQLConfigurationListener.TOPIC).onEndpointsChanged();

        EditorNotifications.getInstance(myProject).updateAllNotifications();
    }

    @Nullable
    public Set<VirtualFile> getContentRoots(VirtualFile virtualFile) {
        final Module module = ModuleUtil.findModuleForFile(virtualFile, myProject);
        if (module != null) {
            return Sets.newHashSet(ModuleRootManager.getInstance(module).getContentRoots());
        } else {
            return Sets.newHashSet(myProject.getBaseDir());
        }
    }

    @Nullable
    public GraphQLNamedScope getSchemaScope(VirtualFile virtualFile) {
        final Ref<VirtualFile> virtualFileWithPath = new Ref<>(virtualFile);
        if (virtualFile instanceof VirtualFileWindow) {
            // injected virtual files
            virtualFileWithPath.set(((VirtualFileWindow) virtualFile).getDelegate());
        }
        GraphQLNamedScope namedScope = virtualFilePathToScopes.computeIfAbsent(virtualFileWithPath.get().getPath(), path -> {
            VirtualFile configBaseDir;
            if (virtualFileWithPath.get().getFileType() != ScratchFileType.INSTANCE) {
                configBaseDir = virtualFileWithPath.get().getParent();
            } else {
                configBaseDir = getConfigBaseDirForScratch(virtualFileWithPath.get());
            }
            // locate the nearest config file, see https://github.com/prismagraphql/graphql-config/blob/master/src/findGraphQLConfigFile.ts
            final Set<VirtualFile> contentRoots = getContentRoots(virtualFileWithPath.get());
            while (configBaseDir != null) {
                final String configBaseDirPath = configBaseDir.getPath();
                GraphQLConfigData configData = configPathToConfigurations.get(configBaseDirPath);
                if (configData != null) {
                    // check projects first
                    if (configData.projects != null) {
                        for (Map.Entry<String, GraphQLResolvedConfigData> entry : configData.projects.entrySet()) {
                            final GraphQLConfigPackageSet packageSet = new GraphQLConfigPackageSet(configBaseDir, entry.getValue(), graphQLConfigGlobMatcher);
                            if (packageSet.includesVirtualFile(virtualFileWithPath.get())) {
                                return new GraphQLNamedScope("graphql-config:" + entry.getKey(), packageSet);
                            }
                        }
                    }
                    // then top level config
                    final GraphQLConfigPackageSet packageSet = new GraphQLConfigPackageSet(configBaseDir, configData, graphQLConfigGlobMatcher);
                    if (packageSet.includesVirtualFile(virtualFileWithPath.get())) {
                        return new GraphQLNamedScope("graphql-config:" + configBaseDirPath, packageSet);
                    }
                    return NONE;
                } else {
                    if (contentRoots != null && contentRoots.contains(configBaseDir)) {
                        // don't step outside the module content roots
                        break;
                    }
                    configBaseDir = configBaseDir.getParent();
                }
            }
            // can't return null here because computeIfAbsent doesn't consider that as a
            return NONE;
        });
        if (namedScope != NONE) {
            return namedScope;
        }
        return null;
    }

    /**
     * Resolves the logical configuration base dir for a scratch file that is placed outside the project by IntelliJ
     * @param scratchVirtualFile the scratch file to resolve a configuration dir for
     * @return the resolved configuration base dir or null if none was found
     */
    private VirtualFile getConfigBaseDirForScratch(VirtualFile scratchVirtualFile) {

        final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(scratchVirtualFile);

        // by default we'll use the directory of the scratch file
        final Ref<VirtualFile> baseDir = new Ref<>();

        // but look for a GRAPHQLCONFIG_COMMENT to override it
        if (psiFile != null) {
            PsiElement element = psiFile.getFirstChild();
            while (element != null) {
                if (element instanceof PsiComment) {
                    final String commentText = element.getText();
                    if (commentText.contains(GRAPHQLCONFIG_COMMENT)) {
                        final String configFileName = StringUtil.substringAfter(commentText, GRAPHQLCONFIG_COMMENT);
                        if (configFileName != null) {
                            final VirtualFile configVirtualFile = scratchVirtualFile.getFileSystem().findFileByPath(configFileName.trim());
                            if (configVirtualFile != null) {
                                if (configVirtualFile.isDirectory()) {
                                    baseDir.set(configVirtualFile);
                                } else {
                                    baseDir.set(configVirtualFile.getParent());
                                }
                                break;
                            }
                        }
                    }
                }
                element = element.getNextSibling();
            }
        }

        if (baseDir.get() == null) {
            // fallback to either:
            // - a single config
            // - the project base dir
            if (configPathToConfigurations.size() == 1) {
                final String singleConfigPath = configPathToConfigurations.keySet().iterator().next();
                baseDir.set(scratchVirtualFile.getFileSystem().findFileByPath(singleConfigPath));
            }
            if (baseDir.get() == null) {
                baseDir.set(myProject.getBaseDir());
            }
        }

        return baseDir.get();
    }

    private void createParseErrorNotification(VirtualFile file, Exception e) {
        Notifications.Bus.notify(new Notification("GraphQL", "Unable to parse " + file.getName(), "<a href=\"" + file.getUrl() + "\">" + file.getPresentableUrl() + "</a>: " + e.getMessage(), NotificationType.WARNING, (notification, event) -> {
            VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(event.getURL().toString());
            if (virtualFile != null) {
                FileEditorManager.getInstance(myProject).openFile(virtualFile, true, true);
            } else {
                notification.expire();
            }
        }));
    }

    private boolean hasScopeSettings(GraphQLResolvedConfigData configData) {
        if (configData.includes != null && !configData.includes.isEmpty()) {
            return true;
        }
        return configData.excludes != null && !configData.excludes.isEmpty();
    }

}
