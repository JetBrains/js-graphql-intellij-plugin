/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.graphqlconfig;

import com.google.common.collect.Maps;
import com.intellij.openapi.util.Pair;
import minimatch.Minimatch;
import minimatch.Options;

import java.util.Map;

/**
 * Matcher which uses nashorn-minimatch to achieve same glob semantics as graphql-config.
 */
public class GraphQLConfigGlobMatcherImpl implements GraphQLConfigGlobMatcher {

    private final static Map<Pair<String, String>, Boolean> matches = Maps.newConcurrentMap();
    private final static Options OPTIONS = new Options().setMatchBase(true);

    @Override
    public boolean matches(String filePath, String glob) {
        return matches.computeIfAbsent(Pair.create(filePath, glob), args -> Minimatch.minimatch(args.first, args.second, OPTIONS));
    }

}
