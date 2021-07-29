/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.graphqlconfig;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.ProjectTopics;
import com.intellij.ide.scratch.ScratchUtil;
import com.intellij.json.JsonFileType;
import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointFileType;
import com.intellij.lang.jsgraphql.ide.editor.GraphQLIntrospectionService;
import com.intellij.lang.jsgraphql.ide.notifications.GraphQLNotificationUtil;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigData;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLConfigEndpoint;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLResolvedConfigData;
import com.intellij.lang.jsgraphql.ide.references.GraphQLFindUsagesUtil;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.psi.GraphQLPsiUtil;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaKeys;
import com.intellij.lang.jsgraphql.v1.ide.configuration.JSGraphQLConfigurationListener;
import com.intellij.lang.jsgraphql.v1.ide.configuration.JSGraphQLSchemaEndpointConfiguration;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.EditorNotifications;
import com.intellij.util.Processor;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.schema.GraphQLSchemaKeys.GRAPHQL_SCRATCH_PROJECT_KEY;

/**
 * Manages integration with graphql-config files. See https://github.com/kamilkisiela/graphql-config/tree/legacy
 */
public class GraphQLConfigManager implements Disposable {

    private final static Logger LOG = Logger.getInstance(GraphQLConfigManager.class);

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

    private volatile boolean initialized = false;

    private volatile Map<VirtualFile, GraphQLConfigData> configFilesToConfigurations = Maps.newConcurrentMap();
    private final Map<GraphQLResolvedConfigData, GraphQLFile> configDataToEntryFiles = Maps.newConcurrentMap();
    private final Map<GraphQLResolvedConfigData, GraphQLConfigPackageSet> configDataToPackageSet = Maps.newConcurrentMap();
    private final Map<String, GraphQLNamedScope> virtualFilePathToScopes = Maps.newConcurrentMap();
    private final Map<GraphQLNamedScope, JSGraphQLSchemaEndpointConfiguration> scopeToSchemaEndpointLanguageConfiguration = Maps.newConcurrentMap();

    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock(true);
    private final Lock writeLock = cacheLock.writeLock();
    private final Lock readLock = cacheLock.readLock();

    private final Ref<Runnable> buildConfigurationModelCallable = Ref.create();

    public GraphQLConfigManager(Project myProject) {
        this.myProject = myProject;
        this.projectScope = GlobalSearchScope.projectScope(myProject);
        this.graphQLConfigGlobMatcher = ServiceManager.getService(myProject, GraphQLConfigGlobMatcher.class);
    }

    public static GraphQLConfigManager getService(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLConfigManager.class);
    }

    public boolean isInitialized() {
        try {
            readLock.lock();
            return initialized;
        } finally {
            readLock.unlock();
        }
    }

    @TestOnly
    public Lock getReadLock() {
        return readLock;
    }

    /**
     * Gets the closest .graphqlconfig{.yml,.yaml} file that includes the specified file
     *
     * @param virtualFile the file to get the config file for
     * @return the closest config file that includes the specified virtualFile, or null if none found or not included
     */
    @Nullable
    public VirtualFile getClosestIncludingConfigFile(@Nullable VirtualFile virtualFile) {
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
    @Nullable
    public VirtualFile getClosestConfigFile(@Nullable VirtualFile virtualFile) {
        if (virtualFile == null) return null;

        if (virtualFile instanceof LightVirtualFile) {
            VirtualFile originalFile = ((LightVirtualFile) virtualFile).getOriginalFile();
            if (originalFile != null) {
                // need a path on disk to find the closest config, so use the original physical file if one has been set
                virtualFile = originalFile;
            }
        }
        VirtualFile initialDirectory;
        if (ScratchUtil.isScratch(virtualFile)) {
            initialDirectory = getConfigBaseDirForScratch(virtualFile);
        } else {
            initialDirectory = virtualFile.isDirectory() ? virtualFile : virtualFile.getParent();
        }

        if (initialDirectory == null) return null;

        Ref<VirtualFile> result = Ref.create();
        processDirectoriesUpToContentRoot(myProject, initialDirectory, dir -> {
            for (String fileName : GRAPHQLCONFIG_FILE_NAMES) {
                VirtualFile configFile = dir.findChild(fileName);
                if (configFile != null) {
                    result.set(configFile);
                    return false;
                }
            }
            return true;
        });
        return result.get();
    }

    public static void processDirectoriesUpToContentRoot(@NotNull Project project,
                                                         @NotNull VirtualFile fileOrDir,
                                                         @NotNull Processor<? super VirtualFile> directoryProcessor) {
        if (project.isDisposed()) {
            return;
        }
        ProjectFileIndex fileIndex = ProjectFileIndex.getInstance(project);
        VirtualFile dir = fileOrDir.isDirectory() ? fileOrDir : fileOrDir.getParent();
        if (dir == null) {
            return;
        }
        VirtualFile contentRoot = fileIndex.getContentRootForFile(dir, false);
        while (dir != null && contentRoot != null) {
            if (!directoryProcessor.process(dir)) {
                return;
            }
            if (dir.equals(contentRoot)) {
                dir = dir.getParent();
                if (dir == null) {
                    return;
                }
                contentRoot = fileIndex.getContentRootForFile(dir, false);
            } else {
                dir = dir.getParent();
            }
        }
    }

    public void createAndOpenConfigFile(@NotNull VirtualFile configBaseDir, Boolean openEditor) {
        createAndOpenConfigFile(configBaseDir, openEditor, outputStream -> {
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("META-INF/" + GRAPHQLCONFIG)) {
                if (inputStream != null) {
                    IOUtils.copy(inputStream, outputStream);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    public void createAndOpenConfigFile(@NotNull VirtualFile configBaseDir,
                                        Boolean openEditor,
                                        @NotNull Consumer<OutputStream> outputStreamConsumer) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                final VirtualFile configFile = configBaseDir.createChildData(this, GRAPHQLCONFIG);
                try (OutputStream stream = configFile.getOutputStream(this)) {
                    outputStreamConsumer.accept(stream);
                }
                if (openEditor) {
                    UIUtil.invokeLaterIfNeeded(() -> FileEditorManager.getInstance(myProject).openFile(configFile, true, true));
                }
            } catch (IOException e) {
                Notifications.Bus.notify(new Notification(
                    GraphQLNotificationUtil.NOTIFICATION_GROUP_ID,
                    "Unable to create " + GRAPHQLCONFIG,
                    String.format("Unable to create file '%s' in directory '%s': %s", GRAPHQLCONFIG, configBaseDir.getPath(), e.getMessage()),
                    NotificationType.ERROR)
                );
            }
        });
    }

    /**
     * Gets the currently discovered configurations
     *
     * @see #buildConfigurationModel(List, Runnable)
     */
    @NotNull
    public Map<VirtualFile, GraphQLConfigData> getConfigurationsByPath() {
        try {
            readLock.lock();
            return configFilesToConfigurations;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Gets a GraphQL PSI file that will be considered included in the specified configuration.
     * This file can be used as "the entry file" to get the corresponding schema scope.
     */
    @NotNull
    public GraphQLFile getConfigurationEntryFile(@NotNull GraphQLResolvedConfigData configData) {
        try {
            readLock.lock();
            return configDataToEntryFiles.computeIfAbsent(configData, cd -> {
                final PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(myProject);
                return ApplicationManager.getApplication().runReadAction(
                    (Computable<GraphQLFile>) () -> (GraphQLFile) psiFileFactory
                        .createFileFromText("graphql-config:" + UUID.randomUUID().toString(), GraphQLLanguage.INSTANCE, ""));
            });
        } finally {
            readLock.unlock();
        }
    }

    @Nullable
    public JSGraphQLSchemaEndpointConfiguration getEndpointLanguageConfiguration(@NotNull VirtualFile virtualFile,
                                                                                 @Nullable Ref<VirtualFile> configBasedir) {
        if (virtualFile.getFileType() != GraphQLFileType.INSTANCE && virtualFile.getFileType() != JSGraphQLEndpointFileType.INSTANCE && !GraphQLFileType.isGraphQLScratchFile(myProject, virtualFile)) {
            if (!GraphQLFindUsagesUtil.getService().getIncludedFileTypes().contains(virtualFile.getFileType())) {
                return null;
            }
        }
        try {
            readLock.lock();
            GraphQLNamedScope schemaScope = getSchemaScope(virtualFile);
            if (schemaScope != null) {
                JSGraphQLSchemaEndpointConfiguration configuration = scopeToSchemaEndpointLanguageConfiguration.computeIfAbsent(schemaScope, scope -> {
                    if (schemaScope.getConfigData() != null) {
                        final Map<String, Object> extensions = schemaScope.getConfigData().extensions;
                        if (extensions != null && extensions.containsKey(ENDPOINT_LANGUAGE_EXTENSION)) {
                            try {
                                final Gson gson = new Gson();
                                return gson.fromJson(gson.toJsonTree(extensions.get(ENDPOINT_LANGUAGE_EXTENSION)), JSGraphQLSchemaEndpointConfiguration.class);
                            } catch (JsonSyntaxException je) {
                                LOG.warn("Invalid JSON in config file", je);
                            }
                        }
                    }
                    // using sentinel value to avoid re-computing values which happens on nulls
                    return JSGraphQLSchemaEndpointConfiguration.NONE;
                });
                if (configuration != JSGraphQLSchemaEndpointConfiguration.NONE) {
                    if (configBasedir != null) {
                        configBasedir.set(schemaScope.getConfigBaseDir());
                    }
                    return configuration;
                }
            }
            return null;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Gets the endpoints that are within scope for the specified GraphQL virtual file.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public List<GraphQLConfigEndpoint> getEndpoints(@NotNull VirtualFile virtualFile) {
        // NOTE: modifiable list since it powers the endpoint UI and must support item operations
        ArrayList<GraphQLConfigEndpoint> emptyList = Lists.newArrayListWithExpectedSize(1);

        if (!GraphQLFileType.isGraphQLFile(myProject, virtualFile)) {
            return emptyList;
        }

        try {
            readLock.lock();

            final GraphQLNamedScope schemaScope = initialized ? getSchemaScope(virtualFile) : null;
            if (schemaScope == null) {
                return emptyList;
            }
            GraphQLConfigPackageSet packageSet = (GraphQLConfigPackageSet) schemaScope.getValue();
            if (packageSet == null || packageSet.getConfigData() == null) {
                return emptyList;
            }

            Map<String, Object> extensions = packageSet.getConfigData().extensions;
            if (extensions == null) {
                return emptyList;
            }
            final Object endpointsValue = extensions.get(ENDPOINTS_EXTENSION);

            if (endpointsValue instanceof Map) {
                final List<GraphQLConfigEndpoint> result = Lists.newArrayList();
                ((Map<String, Object>) endpointsValue).forEach((key, value) -> {
                    if (value instanceof String) {
                        result.add(new GraphQLConfigEndpoint(packageSet, key, (String) value));
                    } else if (value instanceof Map) {
                        final Map<String, Object> endpointAsMap = (Map<String, Object>) value;
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

            return emptyList;
        } finally {
            readLock.unlock();
        }
    }

    void initialize() {
        final MessageBusConnection connection = myProject.getMessageBus().connect(this);
        connection.subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
            @Override
            public void rootsChanged(@NotNull ModuleRootEvent event) {
                // rebuild configuration when the project structure is changed, e.g. excludes
                ApplicationManager.getApplication().invokeLater(() -> {
                    // let queued updates complete
                    buildConfigurationModel(null, null);
                });
            }
        });
        connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                final List<VirtualFile> changedConfigFiles = Lists.newArrayList();
                boolean configurationsChanged = false;
                for (VFileEvent event : events) {
                    final VirtualFile file = event.getFile();
                    if (file != null) {
                        if (file.isDirectory()) {
                            // directory was changed, check whether a config file is affected
                            if (!configurationsChanged) {
                                try {
                                    readLock.lock();
                                    for (VirtualFile configPathDir : configFilesToConfigurations.keySet()) {
                                        if (!configPathDir.isValid() || file.getPath().startsWith(configPathDir.getPath())) {
                                            configurationsChanged = true;
                                            break;
                                        }
                                    }
                                } finally {
                                    readLock.unlock();
                                }
                            }
                            if (!configurationsChanged && event instanceof VFileCreateEvent) {
                                // new directory created, e.g. by switching branch
                                for (String configFileName : GRAPHQLCONFIG_FILE_NAMES_SET) {
                                    if (file.findFileByRelativePath(configFileName) != null) {
                                        configurationsChanged = true;
                                        break;
                                    }
                                }
                            }
                        } else {
                            if (event instanceof VFilePropertyChangeEvent) {
                                // renames
                                final VFilePropertyChangeEvent propertyChangeEvent = (VFilePropertyChangeEvent) event;
                                if (VirtualFile.PROP_NAME.equals(propertyChangeEvent.getPropertyName())) {
                                    if (propertyChangeEvent.getNewValue() instanceof String && GRAPHQLCONFIG_FILE_NAMES_SET.contains(propertyChangeEvent.getNewValue())) {
                                        configurationsChanged = true;
                                    } else if (propertyChangeEvent.getOldValue() instanceof String && GRAPHQLCONFIG_FILE_NAMES_SET.contains(propertyChangeEvent.getOldValue())) {
                                        configurationsChanged = true;
                                    }
                                }
                            } else {
                                // other changes (create/deletions)
                                final String name = file.getName();
                                if (GRAPHQLCONFIG_FILE_NAMES_SET.contains(name)) {
                                    changedConfigFiles.add(file);
                                }
                            }
                        }
                    }
                }
                if (!changedConfigFiles.isEmpty() || configurationsChanged) {
                    buildConfigurationModel(changedConfigFiles, null);
                }
            }
        });

        PsiManager.getInstance(myProject).addPsiTreeChangeListener(new PsiTreeChangeAdapter() {

            @Override
            public void childReplaced(@NotNull PsiTreeChangeEvent event) {
                if (event.getFile() instanceof GraphQLFile && ScratchUtil.isScratch(event.getFile().getVirtualFile())) {
                    if (hasGraphQLConfigComment(event.getNewChild()) || hasGraphQLConfigComment(event.getOldChild())) {
                        // updated the .graphqlconfig comment in a scratch comment which associates the scratch with a scope
                        // so clear the cached path to scope entry in virtualFilePathToScopes
                        virtualFilePathToScopes.keySet().remove(event.getFile().getVirtualFile().getPath());
                    }
                }
            }
        }, this);

        buildConfigurationModel(null, this::introspectEndpoints);

    }

    private static boolean hasGraphQLConfigComment(@Nullable PsiElement psiElement) {
        if (psiElement instanceof PsiComment) {
            return psiElement.getText().contains(GRAPHQLCONFIG_COMMENT);
        }
        return false;
    }

    /**
     * Builds a model of the .graphqlconfig files in the project using an asynchronous background task.
     * <p>
     * Can safely be invoked from the AWT UI thread.
     *
     * @param changedConfigurationFiles config files that were changed in the Virtual File System and should be explicitly processed given that they haven't been indexed yet
     * @param onCompleted               optional runnable to execute when the config model has been built
     */
    public void buildConfigurationModel(@Nullable List<VirtualFile> changedConfigurationFiles, @Nullable Runnable onCompleted) {
        DumbService.getInstance(myProject).smartInvokeLater(() -> {

            // runs on the UI thread so task scheduling can be considered atomic
            final boolean hasExistingTask = buildConfigurationModelCallable.get() != null;

            // set the runnable that the task uses to the latest refresh info
            buildConfigurationModelCallable.set(() -> {
                doBuildConfigurationModel(changedConfigurationFiles);
                if (onCompleted != null) {
                    onCompleted.run();
                }
            });

            if (hasExistingTask) {
                // already queued up the task
                return;
            }

            new Task.Backgroundable(
                myProject,
                GraphQLBundle.message("graphql.progress.configuration.scan"),
                false
            ) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    indicator.setIndeterminate(true);
                    DumbService.getInstance(myProject).runReadActionInSmartMode(() -> {
                        final Runnable runnable = buildConfigurationModelCallable.get();
                        if (runnable != null) {
                            runnable.run();
                        }
                    });
                }

                @Override
                public void onFinished() {
                    // also called on UI thread
                    buildConfigurationModelCallable.set(null);
                }

            }.queue();
        }, ModalityState.NON_MODAL);
    }

    /**
     * Builds a model of the .graphqlconfig files in the project.
     * <p>
     * NOTE!: This is a potentially long-running process that is executed synchronously, so it should NOT be invoked outside unit tests.
     * Use the asynchronous {@link GraphQLConfigManager#buildConfigurationModel(List, Runnable)} for all other use cases.
     *
     * @param changedConfigurationFiles config files that were changed in the Virtual File System and should be explicitly processed given that they haven't been indexed yet
     */
    @VisibleForTesting
    public void doBuildConfigurationModel(@Nullable List<VirtualFile> changedConfigurationFiles) {

        final Map<VirtualFile, GraphQLConfigData> newConfigPathToConfigurations = Maps.newConcurrentMap();

        // JSON format
        final Collection<VirtualFile> jsonFiles = ApplicationManager.getApplication().runReadAction(
            (Computable<Collection<VirtualFile>>) () -> Sets.newLinkedHashSet(FilenameIndex.getVirtualFilesByName(myProject, GRAPHQLCONFIG, projectScope))
        );
        if (changedConfigurationFiles != null) {
            for (VirtualFile configurationFile : changedConfigurationFiles) {
                if (!projectScope.contains(configurationFile)) {
                    // skip excluded files
                    continue;
                }
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
        final Collection<VirtualFile> yamlFiles = ApplicationManager.getApplication().runReadAction(
            (Computable<Collection<VirtualFile>>) () -> {
                final LinkedHashSet<VirtualFile> files = Sets.newLinkedHashSet(FilenameIndex.getVirtualFilesByName(myProject, GRAPHQLCONFIG_YML, projectScope));
                files.addAll(FilenameIndex.getVirtualFilesByName(myProject, GRAPHQLCONFIG_YAML, projectScope));
                return files;
            }
        );
        if (changedConfigurationFiles != null) {
            for (VirtualFile configurationFile : changedConfigurationFiles) {
                if (!projectScope.contains(configurationFile)) {
                    // skip excluded files
                    continue;
                }
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

        // apply defaults to projects as spec'ed in https://github.com/kamilkisiela/graphql-config/tree/legacyspecification.md#default-configuration-properties
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

        try {
            writeLock.lock();
            this.configFilesToConfigurations = newConfigPathToConfigurations;
            this.virtualFilePathToScopes.clear();
            this.configDataToEntryFiles.clear();
            this.configDataToPackageSet.clear();
            this.scopeToSchemaEndpointLanguageConfiguration.clear();
            // finally mark as initialized
            initialized = true;
        } finally {
            writeLock.unlock();
        }

        myProject.getMessageBus().syncPublisher(TOPIC).onGraphQLConfigurationFileChanged();
        myProject.getMessageBus().syncPublisher(JSGraphQLConfigurationListener.TOPIC).onEndpointsChanged();

        EditorNotifications.getInstance(myProject).updateAllNotifications();
    }

    private void introspectEndpoints() {
        final List<GraphQLResolvedConfigData> configDataList = Lists.newArrayList();
        try {
            readLock.lock();
            for (GraphQLConfigData configData : configFilesToConfigurations.values()) {
                configDataList.add(configData);
                if (configData.projects != null) {
                    configDataList.addAll(configData.projects.values());
                }
            }
        } finally {
            readLock.unlock();
        }
        configDataList.forEach(configData -> {
            final GraphQLFile entryFile = getConfigurationEntryFile(configData);
            final List<GraphQLConfigEndpoint> endpoints = getEndpoints(entryFile.getVirtualFile());
            for (GraphQLConfigEndpoint endpoint : endpoints) {
                if (Boolean.TRUE.equals(endpoint.introspect)) {
                    // endpoint should be automatically introspected
                    final String schemaPath = endpoint.configPackageSet.getConfigData().schemaPath;
                    if (schemaPath != null && !schemaPath.trim().isEmpty()) {
                        final Notification introspect = new Notification(
                            GraphQLNotificationUtil.NOTIFICATION_GROUP_ID,
                            GraphQLBundle.message("graphql.notification.load.schema.from.endpoint.title"),
                            GraphQLBundle.message("graphql.notification.load.schema.from.endpoint.body", endpoint.name),
                            NotificationType.INFORMATION
                        ).setImportant(true);

                        introspect.addAction(new NotificationAction(GraphQLBundle.message("graphql.notification.load.schema.from.endpoint.action", endpoint.url)) {
                            @Override
                            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                                GraphQLIntrospectionService.getInstance(myProject).performIntrospectionQueryAndUpdateSchemaPathFile(myProject, endpoint);
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
        });
    }

    @Nullable
    public GraphQLNamedScope getSchemaScope(@Nullable VirtualFile virtualFile) {
        VirtualFile virtualFileWithPath = GraphQLPsiUtil.getPhysicalVirtualFile(virtualFile);
        if (virtualFileWithPath == null) return null;

        try {
            readLock.lock();
            GraphQLNamedScope namedScope = virtualFilePathToScopes.computeIfAbsent(virtualFileWithPath.getPath(), path -> {
                VirtualFile configBaseDir = !ScratchUtil.isScratch(virtualFileWithPath)
                    ? getConfigBaseDir(virtualFileWithPath)
                    : getConfigBaseDirForScratch(virtualFileWithPath);
                if (configBaseDir == null) return NONE;

                // locate the nearest config file, see https://github.com/kamilkisiela/graphql-config/tree/legacy/src/findGraphQLConfigFile.ts
                Ref<GraphQLNamedScope> scopeRef = Ref.create(NONE);
                processDirectoriesUpToContentRoot(myProject, configBaseDir, dir -> {
                    GraphQLConfigData configData = configFilesToConfigurations.get(dir);
                    if (configData == null) {
                        return true;
                    }

                    // check projects first
                    if (configData.projects != null) {
                        final String projectKey = virtualFileWithPath.getUserData(GRAPHQL_SCRATCH_PROJECT_KEY);
                        for (Map.Entry<String, GraphQLResolvedConfigData> entry : configData.projects.entrySet()) {
                            if (projectKey != null && !projectKey.trim().isEmpty() && !projectKey.equals(entry.getKey())) {
                                // associated with another project so skip ahead
                                continue;
                            }
                            final GraphQLResolvedConfigData projectConfigData = entry.getValue();
                            final GraphQLConfigPackageSet packageSet = configDataToPackageSet.computeIfAbsent(projectConfigData, dataKey -> {
                                final GraphQLFile configEntryFile = getConfigurationEntryFile(dataKey);
                                return new GraphQLConfigPackageSet(dir, configEntryFile, dataKey, graphQLConfigGlobMatcher);
                            });
                            if (packageSet.includesVirtualFile(virtualFileWithPath)) {
                                scopeRef.set(new GraphQLNamedScope("graphql-config:" + dir.getPath() + ":" + entry.getKey(), packageSet));
                                return false;
                            }
                        }
                    }

                    // then top level config
                    final GraphQLConfigPackageSet packageSet = configDataToPackageSet.computeIfAbsent(configData, dataKey -> {
                        final GraphQLFile configEntryFile = getConfigurationEntryFile(dataKey);
                        return new GraphQLConfigPackageSet(dir, configEntryFile, dataKey, graphQLConfigGlobMatcher);
                    });
                    if (packageSet.includesVirtualFile(virtualFileWithPath)) {
                        scopeRef.set(new GraphQLNamedScope("graphql-config:" + dir.getPath(), packageSet));
                        return false;
                    }
                    return false;
                });

                return scopeRef.get();
            });

            return namedScope != NONE ? namedScope : null;
        } finally {
            readLock.unlock();
        }
    }

    @Nullable
    private VirtualFile getConfigBaseDir(@NotNull VirtualFile virtualFile) {
        VirtualFile configBaseDir = null;

        if (virtualFile instanceof LightVirtualFile) {
            // handle entry files
            final LightVirtualFile inMemoryVirtualFile = (LightVirtualFile) virtualFile;
            for (Map.Entry<VirtualFile, GraphQLConfigData> entry : configFilesToConfigurations.entrySet()) {
                final GraphQLConfigData configData = entry.getValue();
                GraphQLFile entryFile = getConfigurationEntryFile(configData);
                boolean found = false;
                if (entryFile.getVirtualFile().equals(inMemoryVirtualFile)) {
                    // the virtual file is an entry file for the specific config base (either the root schema or one of the nested graphql-config project schemas)
                    configBaseDir = entry.getKey();
                    found = true;
                } else if (configData.projects != null) {
                    for (Map.Entry<String, GraphQLResolvedConfigData> projectEntry : configData.projects.entrySet()) {
                        entryFile = getConfigurationEntryFile(projectEntry.getValue());
                        if (entryFile.getVirtualFile().equals(inMemoryVirtualFile)) {
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

            if (configBaseDir == null) {
                final PsiFile jsonIntrospectionFile = inMemoryVirtualFile.getUserData(GraphQLSchemaKeys.GRAPHQL_INTROSPECTION_SDL_TO_JSON);
                if (jsonIntrospectionFile != null && jsonIntrospectionFile.isValid() && jsonIntrospectionFile.getVirtualFile() != null) {
                    // the file is the SDL derived from a JSON introspection file, so use the JSON file directory to find the associated config
                    configBaseDir = jsonIntrospectionFile.getVirtualFile().getParent();
                } else if (inMemoryVirtualFile.getOriginalFile() != null) {
                    // editing of injected fragments produce in-memory mapped version of the original file, e.g. editing GraphQL inside a JS as it's own editing window
                    configBaseDir = inMemoryVirtualFile.getOriginalFile().getParent();
                }
            }
        } else {
            // on-disk file, so use the containing directory to look for config files
            configBaseDir = virtualFile.getParent();
        }
        return configBaseDir;
    }

    /**
     * Resolves the logical configuration base dir for a scratch file that is placed outside the project by IntelliJ
     *
     * @param scratchVirtualFile the scratch file to resolve a configuration dir for
     * @return the resolved configuration base dir or null if none was found
     */
    @Nullable
    private VirtualFile getConfigBaseDirForScratch(@NotNull VirtualFile scratchVirtualFile) {
        final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(scratchVirtualFile);

        // but look for a GRAPHQLCONFIG_COMMENT to override it
        if (psiFile != null) {
            scratchVirtualFile.putUserData(GRAPHQL_SCRATCH_PROJECT_KEY, null);
            PsiElement child = psiFile.getFirstChild();
            PsiElement element = child instanceof PsiComment ? child : PsiTreeUtil.skipWhitespacesForward(child);
            if (element instanceof PsiComment) {
                final String commentText = element.getText().trim();
                if (commentText.contains(GRAPHQLCONFIG_COMMENT)) {
                    String configFileName = StringUtil.substringAfter(commentText, GRAPHQLCONFIG_COMMENT);
                    if (configFileName != null) {
                        if (configFileName.contains("!")) {
                            final String projectKey = StringUtils.substringAfterLast(configFileName, "!");
                            scratchVirtualFile.putUserData(GRAPHQL_SCRATCH_PROJECT_KEY, projectKey);
                            configFileName = StringUtils.substringBeforeLast(configFileName, "!");
                        }
                        final VirtualFile configVirtualFile = scratchVirtualFile.getFileSystem().findFileByPath(configFileName.trim());
                        if (configVirtualFile != null) {
                            return configVirtualFile.isDirectory() ? configVirtualFile : configVirtualFile.getParent();
                        }
                    }
                }
            }
        }

        try {
            readLock.lock();
            if (configFilesToConfigurations.size() == 1) {
                return configFilesToConfigurations.keySet().iterator().next();
            }
        } finally {
            readLock.unlock();
        }

        return ProjectUtil.guessProjectDir(myProject);
    }

    private void createParseErrorNotification(@NotNull VirtualFile file, @NotNull Exception e) {
        Notification notification = new Notification(
            GraphQLNotificationUtil.NOTIFICATION_GROUP_ID,
            GraphQLBundle.message("graphql.notification.unable.to.parse.file", file.getName()),
            "<a href=\"" + file.getUrl() + "\">" + file.getPresentableUrl() + "</a>: " + GraphQLNotificationUtil.formatExceptionMessage(e),
            NotificationType.WARNING,
            (n, event) -> {
                VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(event.getURL().toString());
                if (virtualFile != null) {
                    FileEditorManager.getInstance(myProject).openFile(virtualFile, true, true);
                } else {
                    n.expire();
                }
            });

        Notifications.Bus.notify(notification);
    }

    @Override
    public void dispose() {
    }
}
