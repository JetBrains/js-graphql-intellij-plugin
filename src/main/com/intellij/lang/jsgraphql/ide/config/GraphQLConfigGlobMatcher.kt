/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.config

import com.google.common.collect.Maps
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import minimatch.Minimatch
import minimatch.Options

/**
 * Matcher which uses nashorn-minimatch to achieve same glob semantics as graphql-config.
 */
@Service
class GraphQLConfigGlobMatcher {
    companion object {
        @JvmStatic
        fun getInstance(project: Project) = project.service<GraphQLConfigGlobMatcher>()
    }

    private val matches: MutableMap<Pair<String, String>, Boolean> = Maps.newConcurrentMap()
    private val options = Options().setMatchBase(true)

    fun matches(path: String, glob: String): Boolean {
        return matches.computeIfAbsent(path to glob) { (path, glob) ->
            Minimatch.minimatch(path, glob, options)
        }
    }
}
