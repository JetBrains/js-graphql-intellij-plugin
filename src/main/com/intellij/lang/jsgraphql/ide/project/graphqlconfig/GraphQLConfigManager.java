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
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.scratch.ScratchFileType;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.json.JsonFileType;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointFileType;
import com.intellij.lang.jsgraphql.ide.editor.GraphQLIntrospectionHelper;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigData;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigEndpoint;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLResolvedConfigData;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.v1.ide.configuration.JSGraphQLConfigurationListener;
import com.intellij.lang.jsgraphql.v1.ide.configuration.JSGraphQLSchemaEndpointConfiguration;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.EditorNotifications;
import com.intellij.util.messages.Topic;
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
import java.util.*;
import java.util.function.Consumer;

/**
 * Manages integration with graphql-config files. See https://github.com/prismagraphql/graphql-config
 */
public class GraphQLConfigManager {

    private final static Logger log = Logger.getInstance(GraphQLConfigManager.class);

    public final static Topic<GraphQLConfigFileEventListener> TOPIC = new Topic<>(
            "GraphQL Configuration File Change Events",
            GraphQLConfigFileEventListener.class,
            Topic.BroadcastDirection.TO_PARENT
    );

    public static final String ENDPOINTS_EXTENSION = "endpoints";

    public static final String GRAPHQLCONFIG = ".graphqlconfig";
    public static final String GRAPHQLCONFIG_COMMENT = ".graphqlconfig=";
    public static final String ENDPOINT_LANGUAGE_EXTENSION = "endpoint-language";

    private static final String GRAPHQLCONFIG_YML = ".graphqlconfig.yml";
    private static final String GRAPHQLCONFIG_YAML = ".graphqlconfig.yaml";

    public static final String[] GRAPHQLCONFIG_FILE_NAMES = {
            GRAPHQLCONFIG,
            GRAPHQLCONFIG_YML,
            GRAPHQLCONFIG_YAML
    };

    private static final Set<String> GRAPHQLCONFIG_FILE_NAMES_SET = Sets.newHashSet(GRAPHQLCONFIG_FILE_NAMES);

    private static final GraphQLNamedScope NONE = new GraphQLNamedScope("", null);

    private final Project myProject;
    private final GlobalSearchScope projectScope;
    private final GraphQLConfigGlobMatcher graphQLConfigGlobMatcher;
    public final IdeaPluginDescriptor pluginDescriptor;

    private volatile boolean initialized = false;

    private volatile Map<VirtualFile, GraphQLConfigData> configPathToConfigurations = Maps.newConcurrentMap();
    private volatile Map<GraphQLResolvedConfigData, GraphQLFile> configDataToEntryFiles = Maps.newConcurrentMap();
    private volatile Map<GraphQLResolvedConfigData, GraphQLConfigPackageSet> configDataToPackageset = Maps.newConcurrentMap();
    private final Map<String, GraphQLNamedScope> virtualFilePathToScopes = Maps.newConcurrentMap();
    private final Map<GraphQLNamedScope, JSGraphQLSchemaEndpointConfiguration> scopeToSchemaEndpointLanguageConfiguration = Maps.newConcurrentMap();

    public GraphQLConfigManager(Project myProject) {
        this.myProject = myProject;
        this.projectScope = GlobalSearchScope.projectScope(myProject);
        this.graphQLConfigGlobMatcher = ServiceManager.getService(myProject, GraphQLConfigGlobMatcher.class);
        this.pluginDescriptor = PluginManager.getPlugin(PluginId.getId("com.intellij.lang.jsgraphql"));
    }

    public static GraphQLConfigManager getService(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLConfigManager.class);
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Gets the closest .graphqlconfig{.yml,.yaml} file that includes the specified file
     *
     * @param virtualFile the file to get the config file for
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
        if (virtualFile.getFileType() == ScratchFileType.INSTANCE) {
            directory = getConfigBaseDirForScratch(virtualFile);
        } else {
            directory = virtualFile.isDirectory() ? virtualFile : virtualFile.getParent();
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
        createAndOpenConfigFile(configBaseDir, openEditor, outputStream -> {
            try (InputStream inputStream = pluginDescriptor.getPluginClassLoader().getResourceAsStream("/META-INF/" + GRAPHQLCONFIG)) {
                if (inputStream != null) {
                    IOUtils.copy(inputStream, outputStream);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    public void createAndOpenConfigFile(VirtualFile configBaseDir, Boolean openEditor, Consumer<OutputStream> outputStreamConsumer) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                final VirtualFile configFile = configBaseDir.createChildData(this, GRAPHQLCONFIG);
                try (OutputStream stream = configFile.getOutputStream(this)) {
                    outputStreamConsumer.accept(stream);
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
     * Gets the currently discovered configurations
     *
     * @see #buildConfigurationModel(List)
     */
    public Map<VirtualFile, GraphQLConfigData> getConfigurationsByPath() {
        return configPathToConfigurations;
    }

    /**
     * Gets a GraphQL PSI file that will be considered included in the specified configuration.
     * This file can be used as "the entry file" to get the corresponding schema scope.
     */
    public GraphQLFile getConfigurationEntryFile(GraphQLResolvedConfigData configData) {
        return configDataToEntryFiles.computeIfAbsent(configData, cd -> {
            final PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(myProject);
            return (GraphQLFile) psiFileFactory.createFileFromText("graphql-config:" + UUID.randomUUID().toString(), GraphQLLanguage.INSTANCE, "");
        });
    }

    public JSGraphQLSchemaEndpointConfiguration getEndpointLanguageConfiguration(VirtualFile virtualFile, @Nullable Ref<VirtualFile> configBasedir) {
        if (virtualFile.getFileType() != GraphQLFileType.INSTANCE && virtualFile.getFileType() != JSGraphQLEndpointFileType.INSTANCE && !GraphQLFileType.isGraphQLScratchFile(myProject, virtualFile)) {
            return null;
        }
        GraphQLNamedScope schemaScope = getSchemaScope(virtualFile);
        if(schemaScope != null) {
            JSGraphQLSchemaEndpointConfiguration configuration = scopeToSchemaEndpointLanguageConfiguration.computeIfAbsent(schemaScope, scope -> {
                if (schemaScope.getConfigData() != null) {
                    final Map<String, Object> extensions = schemaScope.getConfigData().extensions;
                    if (extensions != null && extensions.containsKey(ENDPOINT_LANGUAGE_EXTENSION)) {
                        try {
                            final Gson gson = new Gson();
                            final JSGraphQLSchemaEndpointConfiguration config = gson.fromJson(gson.toJsonTree(extensions.get(ENDPOINT_LANGUAGE_EXTENSION)), JSGraphQLSchemaEndpointConfiguration.class);
                            return config;
                        } catch (JsonSyntaxException je) {
                            log.warn("Invalid JSON in config file", je);
                        }

                    }
                }
                // using sentinel value to avoid re-computing values which happens on nulls
                return JSGraphQLSchemaEndpointConfiguration.NONE;
            });
            if(configuration != JSGraphQLSchemaEndpointConfiguration.NONE) {
                if(configBasedir != null) {
                    configBasedir.set(schemaScope.getConfigBaseDir());
                }
                return configuration;
            }
        }
        return null;
    }

    /**
     * Gets the endpoints that are within scope for the specified GraphQL virtual file.
     * <p>
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
                    final Object endpointsValue = extensions.get(ENDPOINTS_EXTENSION);
                    if (endpointsValue instanceof Map) {
                        final List<GraphQLConfigEndpoint> result = Lists.newArrayList();
                        ((Map<String, Object>) endpointsValue).forEach((key, value) -> {
                            if (value instanceof String) {
                                result.add(new GraphQLConfigEndpoint(packageSet, key, (String) value));
                            } else if (value instanceof Map) {
                                final Map endpointAsMap = (Map) value;
                                final Object url = endpointAsMap.get("url");
                                if (url instanceof String) {
                                    GraphQLConfigEndpoint endpoint = new GraphQLConfigEndpoint(packageSet, key, (String) url);
                                    Object headers = endpointAsMap.get("headers");
                                    if (headers instanceof Map) {
                                        endpoint.headers = (Map<String, Object>) headers;
                                    }
                                    Boolean introspect = (Boolean) endpointAsMap.get("introspect");
                                    if (introspect != null) {
                                        endpoint.introspect = introspect;
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
        myProject.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                final List<VirtualFile> changedConfigFiles = Lists.newArrayList();
                boolean configurationsRenamed = false;
                for (VFileEvent event : events) {
                    final VirtualFile file = event.getFile();
                    if (file != null) {
                        if (event instanceof VFilePropertyChangeEvent) {
                            // renames
                            final VFilePropertyChangeEvent propertyChangeEvent = (VFilePropertyChangeEvent) event;
                            if (VirtualFile.PROP_NAME.equals(propertyChangeEvent.getPropertyName())) {
                                if (propertyChangeEvent.getNewValue() instanceof String && GRAPHQLCONFIG_FILE_NAMES_SET.contains(propertyChangeEvent.getNewValue())) {
                                    configurationsRenamed = true;
                                } else if (propertyChangeEvent.getOldValue() instanceof String && GRAPHQLCONFIG_FILE_NAMES_SET.contains(propertyChangeEvent.getOldValue())) {
                                    configurationsRenamed = true;
                                }
                            }
                        } else {
                            // other changes
                            final String name = file.getName();
                            if (GRAPHQLCONFIG_FILE_NAMES_SET.contains(name)) {
                                changedConfigFiles.add(file);
                            }
                        }
                    }
                }
                if (!changedConfigFiles.isEmpty() || configurationsRenamed) {
                    buildConfigurationModel(changedConfigFiles);
                }
            }
        });

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

        buildConfigurationModel(null);

        introspectEndpoints();

    }

    private boolean hasGraphQLConfigComment(PsiElement psiElement) {
        if (psiElement instanceof PsiComment) {
            return psiElement.getText().contains(GRAPHQLCONFIG_COMMENT);
        }
        return false;
    }

    /**
     * Builds a model of the .graphqlconfig files in the project
     *
     * @param changedConfigurationFiles config files that were changed in the Virtual File System and should be explicitly processed given that they haven't been indexed yet
     */
    public void buildConfigurationModel(@Nullable List<VirtualFile> changedConfigurationFiles) {

        final Map<VirtualFile, GraphQLConfigData> newConfigPathToConfigurations = Maps.newConcurrentMap();

        // JSON format
        final Collection<VirtualFile> jsonFiles = Sets.newLinkedHashSet(FilenameIndex.getVirtualFilesByName(myProject, GRAPHQLCONFIG, projectScope));
        if (changedConfigurationFiles != null) {
            for (VirtualFile configurationFile : changedConfigurationFiles) {
                if (configurationFile.getFileType().equals(JsonFileType.INSTANCE)) {
                    if (configurationFile.isValid()) { // don't process deletions
                        jsonFiles.add(configurationFile);
                    }
                }
            }
        }

        final Gson gson = new Gson();
        for (VirtualFile jsonFile : jsonFiles) {
            try {
                final String jsonText = new String(jsonFile.contentsToByteArray(), jsonFile.getCharset());
                final GraphQLConfigData graphQLConfigData = gson.fromJson(jsonText, GraphQLConfigData.class);
                if (graphQLConfigData != null) {
                    newConfigPathToConfigurations.put(jsonFile.getParent(), graphQLConfigData);
                }
            } catch (IOException | JsonSyntaxException e) {
                createParseErrorNotification(jsonFile, e);
            }
        }

        // YAML format
        final Collection<VirtualFile> yamlFiles = Sets.newLinkedHashSet(FilenameIndex.getVirtualFilesByName(myProject, GRAPHQLCONFIG_YML, projectScope));
        yamlFiles.addAll(FilenameIndex.getVirtualFilesByName(myProject, GRAPHQLCONFIG_YAML, projectScope));
        if (changedConfigurationFiles != null) {
            for (VirtualFile configurationFile : changedConfigurationFiles) {
                if (configurationFile.getName().equals(GRAPHQLCONFIG_YML) || configurationFile.getName().equals(GRAPHQLCONFIG_YAML)) {
                    if (configurationFile.isValid()) { // don't process deletions
                        yamlFiles.add(configurationFile);
                    }
                }
            }
        }
        if (!yamlFiles.isEmpty()) {
            final Representer representer = new Representer();
            representer.getPropertyUtils().setSkipMissingProperties(true);
            final Yaml yaml = new Yaml(new Constructor(GraphQLConfigData.class), representer);
            yamlFiles.forEach(yamlFile -> {
                try {
                    final String yamlText = new String(yamlFile.contentsToByteArray(), yamlFile.getCharset());
                    final GraphQLConfigData graphQLConfigData = yaml.load(yamlText);
                    newConfigPathToConfigurations.putIfAbsent(yamlFile.getParent(), graphQLConfigData);
                } catch (IOException | YAMLException e) {
                    createParseErrorNotification(yamlFile, e);
                }
            });
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
                            if (!projectConfig.extensions.containsKey(extension.getKey())) {
                                projectConfig.extensions.put(extension.getKey(), extension.getValue());
                            }
                        }
                    }
                });
            }
        }

        this.configPathToConfigurations = newConfigPathToConfigurations;
        this.virtualFilePathToScopes.clear();
        this.configDataToEntryFiles.clear();
        this.configDataToPackageset.clear();
        this.scopeToSchemaEndpointLanguageConfiguration.clear();

        myProject.getMessageBus().syncPublisher(TOPIC).onGraphQLConfigurationFileChanged();
        myProject.getMessageBus().syncPublisher(JSGraphQLConfigurationListener.TOPIC).onEndpointsChanged();

        initialized = true;

        EditorNotifications.getInstance(myProject).updateAllNotifications();
    }

    private void introspectEndpoints() {
        final List<GraphQLResolvedConfigData> configDataList = Lists.newArrayList();
        for (GraphQLConfigData configData : configPathToConfigurations.values()) {
            configDataList.add(configData);
            if (configData.projects != null) {
                configDataList.addAll(configData.projects.values());
            }
        }
        configDataList.forEach(configData -> {
            final GraphQLFile entryFile = getConfigurationEntryFile(configData);
            final List<GraphQLConfigEndpoint> endpoints = getEndpoints(entryFile.getVirtualFile());
            if (endpoints != null) {
                for (GraphQLConfigEndpoint endpoint : endpoints) {
                    if (Boolean.TRUE.equals(endpoint.introspect)) {
                        // endpoint should be automatically introspected
                        final String schemaPath = endpoint.configPackageSet.getConfigData().schemaPath;
                        if (schemaPath != null && !schemaPath.trim().isEmpty()) {
                            final Notification introspect = new Notification("GraphQL", "Get GraphQL Schema from Endpoint now?", "Introspect '" + endpoint.name + "' to update the local schema file.", NotificationType.INFORMATION).setImportant(true);
                            introspect.addAction(new NotificationAction("Introspect '" + endpoint.url + "'") {
                                @Override
                                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                                    GraphQLIntrospectionHelper.getService(myProject).performIntrospectionQueryAndUpdateSchemaPathFile(myProject, endpoint);
                                }
                            });
                            String schemaFilePath = endpoint.configPackageSet.getSchemaFilePath();
                            if (schemaFilePath != null) {
                                final VirtualFile schemaFile = LocalFileSystem.getInstance().findFileByPath(schemaFilePath);
                                if (schemaFile != null) {
                                    introspect.addAction(new NotificationAction("Open schema file") {
                                        @Override
                                        public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                                            if (schemaFile.isValid()) {
                                                FileEditorManager.getInstance(myProject).openFile(schemaFile, true);
                                            } else {
                                                notification.expire();
                                            }
                                        }
                                    });
                                }
                            }
                            Notifications.Bus.notify(introspect);
                        }
                    }
                }
            }
        });
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
                if (virtualFile instanceof LightVirtualFile) {
                    // handle entry files
                    configBaseDir = null;
                    for (Map.Entry<VirtualFile, GraphQLConfigData> entry : configPathToConfigurations.entrySet()) {
                        final GraphQLConfigData configData = entry.getValue();
                        GraphQLFile entryFile = getConfigurationEntryFile(configData);
                        boolean found = false;
                        if (entryFile.getVirtualFile().equals(virtualFile)) {
                            // the virtual file is an entry file for the specific config base (either the root schema or one of the nested graphql-config project schemas)
                            configBaseDir = entry.getKey();
                            found = true;
                        } else if(configData.projects != null) {
                            for (Map.Entry<String, GraphQLResolvedConfigData> projectEntry : configData.projects.entrySet()) {
                                entryFile = getConfigurationEntryFile(projectEntry.getValue());
                                if (entryFile.getVirtualFile().equals(virtualFile)) {
                                    configBaseDir = entry.getKey();
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (found) {
                            break;
                        }
                    }
                } else {
                    // on-disk file, so use the containing directory to look for config files
                    configBaseDir = virtualFileWithPath.get().getParent();
                }
            } else {
                configBaseDir = getConfigBaseDirForScratch(virtualFileWithPath.get());
            }
            // locate the nearest config file, see https://github.com/prismagraphql/graphql-config/blob/master/src/findGraphQLConfigFile.ts
            final Set<VirtualFile> contentRoots = getContentRoots(virtualFileWithPath.get());
            while (configBaseDir != null) {
                GraphQLConfigData configData = configPathToConfigurations.get(configBaseDir);
                if (configData != null) {
                    final VirtualFile effectiveConfigBaseDir = configBaseDir;
                    // check projects first
                    if (configData.projects != null) {
                        for (Map.Entry<String, GraphQLResolvedConfigData> entry : configData.projects.entrySet()) {
                            final GraphQLResolvedConfigData projectConfigData = entry.getValue();
                            final GraphQLConfigPackageSet packageSet = configDataToPackageset.computeIfAbsent(projectConfigData, dataKey -> {
                                final GraphQLFile configEntryFile = getConfigurationEntryFile(dataKey);
                                return new GraphQLConfigPackageSet(effectiveConfigBaseDir, configEntryFile, dataKey, graphQLConfigGlobMatcher);
                            });
                            if (packageSet.includesVirtualFile(virtualFileWithPath.get())) {
                                return new GraphQLNamedScope("graphql-config:" + configBaseDir.getPath() + ":" + entry.getKey(), packageSet);
                            }
                        }
                    }
                    // then top level config
                    final GraphQLConfigPackageSet packageSet = configDataToPackageset.computeIfAbsent(configData, dataKey -> {
                        final GraphQLFile configEntryFile = getConfigurationEntryFile(dataKey);
                        return new GraphQLConfigPackageSet(effectiveConfigBaseDir, configEntryFile, dataKey, graphQLConfigGlobMatcher);
                    });
                    if (packageSet.includesVirtualFile(virtualFileWithPath.get())) {
                        return new GraphQLNamedScope("graphql-config:" + configBaseDir.getPath(), packageSet);
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
     *
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
                final VirtualFile singleConfigPath = configPathToConfigurations.keySet().iterator().next();
                baseDir.set(singleConfigPath);
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
