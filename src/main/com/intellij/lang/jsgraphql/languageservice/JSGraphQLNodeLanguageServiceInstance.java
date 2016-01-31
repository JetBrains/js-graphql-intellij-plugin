/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.languageservice;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.javascript.nodejs.NodeDetectionUtil;
import com.intellij.javascript.nodejs.NodeSettingsUtil;
import com.intellij.lang.jsgraphql.JSGraphQLDebugUtil;
import com.intellij.lang.jsgraphql.ide.project.JSGraphQLLanguageUIProjectService;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.net.NetUtils;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Represents a js-graphql-language-service instance running as a Node.js process handler.
 */
public class JSGraphQLNodeLanguageServiceInstance implements ProjectManagerListener {

    public static final String JSGRAPHQL_INTELLIJ_PLUGIN_ID = "com.intellij.lang.jsgraphql";
    public static final String JSGRAPHQL_LANGUAGE_SERVICE_DIST_JS = "js-graphql-language-service.dist.js";
    public static final String JSGRAPHQL_LANGUAGE_SERVICE_MAPPING = "/js-graphql-language-service";

    private static final Logger log = Logger.getInstance(JSGraphQLNodeLanguageServiceInstance.class);

    private final static Object jsGraphQLNodeLanguageServiceLock = new Object();
    private static File jsGraphQLNodeLanguageServiceFileName = null;

    private Project project;
    private URL url;
    private OSProcessHandler processHandler;
    private String schemaProjectDir;

    public JSGraphQLNodeLanguageServiceInstance(@NotNull Project project) {

        this.project = project;

        ProjectManager.getInstance().addProjectManagerListener(project, this);

        if (JSGraphQLDebugUtil.debug && JSGraphQLDebugUtil.languageServiceUrl != null) {
            try {
                url = new URL(JSGraphQLDebugUtil.languageServiceUrl);
                return; // debug url doesn't require us to create a process
            } catch (MalformedURLException e) {
                log.error("Invalid language service debug url", JSGraphQLDebugUtil.languageServiceUrl);
            }
        }

        try {
            createProcessHandler();
        } catch (Exception e) {
            log.error("Unable to start JS GraphQL Language Service using Node.js", e);
        }

    }

    public static String getNodeInterpreter(Project project) {
        String interpreterPath = NodeSettingsUtil.getInterpreterPath(project);
        if(interpreterPath == null) {
            File interpreterInPath = NodeDetectionUtil.findInterpreterInPath();
            if(interpreterInPath != null) {
                interpreterPath = interpreterInPath.getAbsolutePath();
            }
        }
        return interpreterPath;
    }

    private void createProcessHandler() {

        // Make sure we have a node interpreter
        final String nodeInterpreter = getNodeInterpreter(project);
        if (nodeInterpreter == null) {
            if(log.isDebugEnabled()) {
                log.debug("Can't create process handler: No Node.js interpreter configured.");
            }
            return;
        }

        try {

            if(log.isDebugEnabled()) {
                log.debug("Resolving node.js file...");
            }

            final File jsGraphQLNodeFile = getOrCreateJSGraphQLLanguageServiceFileName();
            if(jsGraphQLNodeFile == null) {
                if(log.isDebugEnabled()) {
                    log.debug("Can't create process handler: Got null from getOrCreateJSGraphQLLanguageServiceFileName.");
                }
                return;
            }

            final int socketPort = NetUtils.findAvailableSocketPort();
            final GeneralCommandLine commandLine = new GeneralCommandLine(nodeInterpreter);

            commandLine.withWorkDirectory(jsGraphQLNodeFile.getParent());
            commandLine.addParameter(jsGraphQLNodeFile.getAbsolutePath());

            commandLine.addParameter("--port=" + socketPort);

            if(log.isDebugEnabled()) {
                log.debug("Creating processHandler using command line " + commandLine.toString());
            }

            processHandler = new OSProcessHandler(commandLine);
            JSGraphQLLanguageUIProjectService languageService = JSGraphQLLanguageUIProjectService.getService(project);
            final Runnable onInitialized = languageService.connectToProcessHandler(processHandler);

            if (waitForListeningNotification(processHandler, project)) {
                url = new URL("http", NetUtils.getLocalHostString(), socketPort, JSGRAPHQL_LANGUAGE_SERVICE_MAPPING);
                onInitialized.run();
            } else {
                log.error("Unable to start JS GraphQL Language Service using Node.js with commandline " + commandLine.toString());
            }



        } catch (IOException | ExecutionException e) {
            log.error("Error running JS GraphQL Language Service using Node.js", e);
        }
    }


    // ---- getters ----

    @NotNull
    public Project getProject() {
        return project;
    }

    public URL getUrl() {
        return url;
    }

    public String getSchemaProjectDir() {
        return schemaProjectDir;
    }

    public void setSchemaProjectDir(String schemaProjectDir) {
        this.schemaProjectDir = schemaProjectDir;
    }

    // ---- actions

    public void restart(Runnable onRestartedInvoke) {
        if(processHandler != null) {
            processHandler.destroyProcess();
            createProcessHandler();
            final Application application = ApplicationManager.getApplication();
            application.executeOnPooledThread(() -> {
                JSGraphQLNodeLanguageServiceClient.onInstanceRestarted(this);
                if(onRestartedInvoke != null) {
                    UIUtil.invokeLaterIfNeeded(onRestartedInvoke);
                }
            });
        } else {
            JSGraphQLNodeLanguageServiceClient.onInstanceRestarted(this);
            if(onRestartedInvoke != null) {
                onRestartedInvoke.run();
            }
        }
    }


    // ---- js-graphql-language-service.dist.js ----

    private File getOrCreateJSGraphQLLanguageServiceFileName() {

        synchronized (jsGraphQLNodeLanguageServiceLock) {

            if(jsGraphQLNodeLanguageServiceFileName == null) {

                if(JSGraphQLDebugUtil.debug && JSGraphQLDebugUtil.languageServiceDistFile != null) {
                    jsGraphQLNodeLanguageServiceFileName = new File(JSGraphQLDebugUtil.languageServiceDistFile);
                    return jsGraphQLNodeLanguageServiceFileName;
                }

                final IdeaPluginDescriptor pluginDescriptor = PluginManager.getPlugin(PluginId.getId(JSGRAPHQL_INTELLIJ_PLUGIN_ID));
                if (pluginDescriptor != null) {
                    final String workingDir;
                    boolean isJar = !pluginDescriptor.getPath().isDirectory();
                    if(isJar) {
                        workingDir = pluginDescriptor.getPath().getParentFile().getAbsolutePath();
                    } else {
                        workingDir = pluginDescriptor.getPath().getAbsolutePath();
                    }
                    final File distJS = new File(workingDir, JSGRAPHQL_LANGUAGE_SERVICE_DIST_JS);
                    if(log.isDebugEnabled()) {
                        log.debug("Found it at " + distJS.toString());
                    }
                    if(distJS.exists()) {
                        // check if we need to write an updated version
                        if(!isJar || (pluginDescriptor.getPath().lastModified() > distJS.lastModified())) {
                            // update
                            jsGraphQLNodeLanguageServiceFileName = createDistJSFile(pluginDescriptor, distJS);
                        } else {
                            // reuse existing
                            jsGraphQLNodeLanguageServiceFileName = distJS;
                        }
                    } else {
                        // doesn't exist -- create it
                        jsGraphQLNodeLanguageServiceFileName = createDistJSFile(pluginDescriptor, distJS);
                    }
                } else {
                    if(log.isDebugEnabled()) {
                        log.debug("No plugin descriptor for " + JSGRAPHQL_INTELLIJ_PLUGIN_ID);
                    }
                }

            }

            return jsGraphQLNodeLanguageServiceFileName;
        }
    }

    private File createDistJSFile(PluginDescriptor pluginDescriptor, File distJS) {
        try {
            if(distJS.exists()) {
                if(log.isDebugEnabled()) {
                    log.debug("Deleting " + distJS + " to extract new version");
                }
                distJS.delete();
            }
            try(OutputStream stream = new FileOutputStream(distJS)) {
                try(InputStream inputStream = pluginDescriptor.getPluginClassLoader().getResourceAsStream("/META-INF/dist/"+JSGRAPHQL_LANGUAGE_SERVICE_DIST_JS)) {
                    if(inputStream != null) {
                        IOUtils.copy(inputStream, stream);
                    } else {
                        log.error("Couldn't load " + JSGRAPHQL_LANGUAGE_SERVICE_DIST_JS + " from " + pluginDescriptor.getPluginClassLoader());
                    }
                }
            }
            return distJS;
        } catch (IOException e) {
            log.error("JS GraphQL: Unable to create file '" + distJS.getAbsolutePath() + "': " + e.getMessage());
        }
        return null;
    }


    // ---- Node.JS process handler ----

    private static boolean waitForListeningNotification(OSProcessHandler processHandler, Project project) {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final Ref<Boolean> result = new Ref<>(false);
        ProcessAdapter listener = new ProcessAdapter() {
            public void onTextAvailable(ProcessEvent event, Key outputType) {
                if(!StringUtil.isEmpty(event.getText())) {
                    if(outputType == ProcessOutputTypes.STDOUT || outputType == ProcessOutputTypes.SYSTEM) {
                        if(log.isDebugEnabled()) {
                            log.debug("Language Service response: " + event.getText());
                        }
                        final String text = event.getText().trim();
                        if(text.startsWith("JS GraphQL listening on")) {
                            result.set(true);
                            countDownLatch.countDown();
                        }
                    } else if(outputType == ProcessOutputTypes.STDERR) {
                        result.set(false);
                        countDownLatch.countDown();
                        JSGraphQLLanguageUIProjectService.showConsole(project);
                    }
                }
            }
            public void processTerminated(ProcessEvent event) {
                countDownLatch.countDown();
            }
        };
        processHandler.addProcessListener(listener);
        processHandler.startNotify();
        try {
            log.debug("Start waiting for ready start");
            countDownLatch.await(30L, TimeUnit.SECONDS);
            log.debug("End waiting for process starting. Result " + result.get());
        } catch (InterruptedException e) {
            log.debug("Process interrupted while waiting ready state", e);
        }

        processHandler.removeProcessListener(listener);
        return result.get();
    }


    // ---- project listener ----

    @Override
    public void projectClosing(Project project) {
        if(processHandler != null) {
            processHandler.destroyProcess();
        }
        JSGraphQLNodeLanguageServiceClient.onProjectClosing(this);
        this.project = null;
    }

    @Override
    public void projectClosed(Project project) {}

    @Override
    public void projectOpened(Project project) {}

    @Override
    public boolean canCloseProject(Project project) { return true; }

}
