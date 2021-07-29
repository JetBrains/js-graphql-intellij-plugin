/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.graphqlconfig;

import com.google.common.collect.Maps;
import com.intellij.ide.scratch.ScratchUtil;
import com.intellij.json.JsonFileType;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLResolvedConfigData;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.scope.packageSet.NamedScopesHolder;
import com.intellij.psi.search.scope.packageSet.PackageSet;
import com.intellij.testFramework.LightVirtualFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PackageSet implementation which uses graphql-config include/exclude globs to apply scoping for schema types etc.
 */
public class GraphQLConfigPackageSet implements PackageSet {

    private final VirtualFile configBaseDir;
    private GraphQLFile configEntryFile;
    private final GraphQLResolvedConfigData configData;
    private GraphQLConfigGlobMatcher globMatcher;
    private final String configBaseDirPath;
    private final boolean hasIncludes;

    private String schemaFilePath;

    private final Map<String, Boolean> includesFilePath = Maps.newConcurrentMap();

    GraphQLConfigPackageSet(VirtualFile configBaseDir, GraphQLFile configEntryFile, GraphQLResolvedConfigData configData, GraphQLConfigGlobMatcher globMatcher) {

        this.configBaseDir = configBaseDir;
        this.configBaseDirPath = configBaseDir.getPath() + "/";
        this.configEntryFile = configEntryFile;
        this.configData = configData;
        this.globMatcher = globMatcher;

        updateSchemaFilePath();

        configData.excludes = normalizeGlobs(configData.excludes);
        configData.includes = normalizeGlobs(configData.includes);

        hasIncludes = configData.includes != null && !configData.includes.isEmpty();
    }

    private void updateSchemaFilePath() {
        if (StringUtils.isNotEmpty(configData.schemaPath)) {
            VirtualFile schemaFile = configBaseDir.findFileByRelativePath(StringUtils.replaceChars(configData.schemaPath, '\\', '/'));
            if (schemaFile != null) {
                schemaFilePath = schemaFile.getPath();
            }
        }
    }

    /**
     * Removes any unnecessary leading dots and slashes since the globs match relative to the config directory
     * Based on graphql-config: https://github.com/kamilkisiela/graphql-config/blob/b6785a7f0c1b84010cd6e9b94797796254d527b9/src/utils.ts#L45
     */
    private List<String> normalizeGlobs(List<String> globs) {
        if (globs != null) {
            globs = globs.stream().map(g -> StringUtils.removeStart(g, "./")).collect(Collectors.toList());
        }
        return globs;
    }

    @Override
    public boolean contains(@NotNull PsiFile file, NamedScopesHolder holder) {
        if (file.equals(configEntryFile)) {
            return true;
        }
        VirtualFile virtualFile = file.getVirtualFile();
        if (virtualFile == null) {
            virtualFile = file.getOriginalFile().getVirtualFile();
        }
        if (virtualFile != null) {
            return includesVirtualFile(virtualFile);
        }
        return false;
    }

    /**
     * Gets whether a file is included.
     * Based on graphl-config: https://github.com/kamilkisiela/graphql-config/blob/b6785a7f0c1b84010cd6e9b94797796254d527b9/src/GraphQLProjectConfig.ts#L56
     * Note: Scratch files are always considered to be included since they are associated with a configuration package set but have a path that lies the project sources
     */
    public boolean includesVirtualFile(@NotNull VirtualFile file) {
        if (ScratchUtil.isScratch(file)) {
            // if a scratch file has been associated with a configuration it is considered to be included
            return true;
        }
        if (file.equals(configEntryFile.getVirtualFile())) {
            // the "entry" file is always considered included
            return true;
        }
        if (JsonFileType.INSTANCE.equals(file.getFileType())) {
            // the only JSON file that can be considered included is an introspection JSON file referenced as schemaPath
            if (schemaFilePath == null && Boolean.TRUE.equals(file.getUserData(GraphQLSchemaKeys.IS_GRAPHQL_INTROSPECTION_JSON))) {
                // new file created from introspection, so update schemaFilePath accordingly
                updateSchemaFilePath();
            }
            return file.getPath().equals(schemaFilePath);
        }
        final PsiFile jsonIntrospectionFile = file.getUserData(GraphQLSchemaKeys.GRAPHQL_INTROSPECTION_SDL_TO_JSON);
        if (jsonIntrospectionFile != null && jsonIntrospectionFile.isValid() && jsonIntrospectionFile.getVirtualFile() != null) {
            // the file is the in-memory SDL derived from a JSON introspection file, so it's included if the JSON file is set as the schemaPath
            return jsonIntrospectionFile.getVirtualFile().getPath().equals(schemaFilePath);
        }

        String inclusionPath = file.getPath();
        if (file instanceof LightVirtualFile) {
            // the light file is potentially derived from a file on disk, so we should use the physical path to check for inclusion
            final VirtualFile originalFile = ((LightVirtualFile) file).getOriginalFile();
            if (originalFile != null) {
                inclusionPath = originalFile.getPath();
            }
        }

        return includesFilePath.computeIfAbsent(inclusionPath, filePath -> {
            if (filePath.equals(schemaFilePath)) {
                // fast-path for always including the schema file if present
                return true;
            }
            final String relativePath;
            if (filePath.startsWith(configBaseDirPath)) {
                relativePath = StringUtils.removeStart(filePath, configBaseDirPath);
            } else {
                // the file is outside the config base dir, so it's not included
                return false;
            }
            return (!hasIncludes || matchesGlobs(relativePath, this.configData.includes)) && !matchesGlobs(relativePath, this.configData.excludes);
        });
    }

    /**
     * Based on graphl-config: https://github.com/kamilkisiela/graphql-config/blob/b6785a7f0c1b84010cd6e9b94797796254d527b9/src/utils.ts#L52
     */
    private boolean matchesGlobs(String filePath, List<String> globs) {
        return Optional.ofNullable(globs).orElse(Collections.emptyList()).stream().anyMatch(glob -> {
            VirtualFile relativePath = configBaseDir.findFileByRelativePath(glob);
            if (relativePath != null && relativePath.isDirectory()) {
                // glob is a directory, so include the files in it
                glob = glob + "/**";
            }
            return globMatcher.matches(filePath, glob);
        });
    }

    @NotNull
    @Override
    public PackageSet createCopy() {
        return new GraphQLConfigPackageSet(configBaseDir, configEntryFile, configData, globMatcher);
    }

    @NotNull
    @Override
    public String getText() {
        return "graphl-config:" + configBaseDir.getPath();
    }

    @Override
    public int getNodePriority() {
        return 0;
    }

    public GraphQLResolvedConfigData getConfigData() {
        return configData;
    }

    public VirtualFile getConfigBaseDir() {
        return configBaseDir;
    }

    public String getSchemaFilePath() {
        return schemaFilePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphQLConfigPackageSet that = (GraphQLConfigPackageSet) o;
        return hasIncludes == that.hasIncludes &&
            Objects.equals(configData, that.configData) &&
            Objects.equals(configBaseDirPath, that.configBaseDirPath) &&
            Objects.equals(schemaFilePath, that.schemaFilePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configBaseDirPath, hasIncludes, schemaFilePath);
    }
}
