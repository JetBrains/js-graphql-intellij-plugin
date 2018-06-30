/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.graphqlconfig;

import com.google.common.collect.Maps;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.model.GraphQLResolvedConfigData;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.scope.packageSet.NamedScopesHolder;
import com.intellij.psi.search.scope.packageSet.PackageSet;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PackageSet implementation which uses graphql-config include/exclude globs to apply scoping for schema types etc.
 */
class GraphQLConfigPackageSet implements PackageSet {

    private final VirtualFile configBaseDir;
    private final GraphQLResolvedConfigData configData;
    private GraphQLConfigGlobMatcher globMatcher;
    private final String configBaseDirPath;
    private final boolean hasIncludes;

    private String schemaFilePath;

    private final Map<String, Boolean> includesFilePath = Maps.newConcurrentMap();

    GraphQLConfigPackageSet(VirtualFile configBaseDir, GraphQLResolvedConfigData configData, GraphQLConfigGlobMatcher globMatcher) {

        this.configBaseDir = configBaseDir;
        this.configBaseDirPath = configBaseDir.getPath() + "/";
        this.configData = configData;
        this.globMatcher = globMatcher;

        if (StringUtils.isNotEmpty(configData.schemaPath)) {
            VirtualFile schemaFile = configBaseDir.findFileByRelativePath(configData.schemaPath);
            if (schemaFile != null) {
                schemaFilePath = schemaFile.getPath();
            }
        }

        configData.excludes = normalizeGlobs(configData.excludes);
        configData.includes = normalizeGlobs(configData.includes);

        hasIncludes = configData.includes != null && !configData.includes.isEmpty();
    }

    /**
     * Removes any unnecessary leading dots and slashes since the globs match relative to the config directory
     * Based on graphql-config: https://github.com/prismagraphql/graphql-config/blob/b6785a7f0c1b84010cd6e9b94797796254d527b9/src/utils.ts#L45
     */
    private List<String> normalizeGlobs(List<String> globs) {
        if (globs != null) {
            globs = globs.stream().map(g -> StringUtils.removeStart(g, "./")).collect(Collectors.toList());
        }
        return globs;
    }

    @Override
    public boolean contains(@NotNull PsiFile file, NamedScopesHolder holder) {
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
     * Based on graphl-config: https://github.com/prismagraphql/graphql-config/blob/b6785a7f0c1b84010cd6e9b94797796254d527b9/src/GraphQLProjectConfig.ts#L56
     */
    boolean includesVirtualFile(@NotNull VirtualFile file) {
        return includesFilePath.computeIfAbsent(file.getPath(), filePath -> {
            if (filePath.equals(schemaFilePath)) {
                // fast-path for always including the schema file if present
                return true;
            }
            final String relativePath;
            if (filePath.startsWith(configBaseDirPath)) {
                relativePath = StringUtils.removeStart(filePath, configBaseDirPath);
            } else {
                return false;
            }
            return (!hasIncludes || matchesGlobs(relativePath, this.configData.includes)) && !matchesGlobs(relativePath, this.configData.excludes);
        });
    }

    /**
     * Based on graphl-config: https://github.com/prismagraphql/graphql-config/blob/b6785a7f0c1b84010cd6e9b94797796254d527b9/src/utils.ts#L52
     */
    private boolean matchesGlobs(String filePath, List<String> globs) {
        return Optional.ofNullable(globs).orElse(Collections.emptyList()).stream().anyMatch(glob -> {
            VirtualFile relativePath = configBaseDir.findFileByRelativePath(glob);
            if (relativePath != null) {
                // glob is a directory, so include the files in it
                glob = glob + "/**";
            }
            return globMatcher.matches(filePath, glob);
        });
    }

    @NotNull
    @Override
    public PackageSet createCopy() {
        return new GraphQLConfigPackageSet(configBaseDir, configData, globMatcher);
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
}
