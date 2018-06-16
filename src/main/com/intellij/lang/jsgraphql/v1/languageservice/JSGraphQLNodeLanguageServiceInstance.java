/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.languageservice;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.lang.jsgraphql.v1.JSGraphQLDebugUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;

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
    private String schemaProjectDir;

    public JSGraphQLNodeLanguageServiceInstance(@NotNull Project project) {

        this.project = project;

        final ProjectManager projectManager = ProjectManager.getInstance();
        if(projectManager != null) {
            projectManager.addProjectManagerListener(project, this);
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

    // ---- project listener ----

    @Override
    public void projectClosing(Project project) {
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
