/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValue;

public class GraphQLSchemaKeys {

    /**
     * Set on the Virtual File that contains the JSON result of an introspection query
     */
    public static final Key<Boolean> IS_GRAPHQL_INTROSPECTION_JSON = Key.create("JSGraphQL.IsIntrospectionJSON");

    /**
     * Set on a JSON introspection file (PSI and Virtual) to get the derived GraphQL SDL file
     */
    public static final Key<CachedValue<GraphQLFile>> GRAPHQL_INTROSPECTION_JSON_TO_SDL = Key.create("JSGraphQL.IntrospectionJSONToSDL");

    /**
     * Set on a SDL introspection file (PSI and Virtual) to get the JSON file that the SDL file is derived from
     */
    public static final Key<PsiFile> GRAPHQL_INTROSPECTION_SDL_TO_JSON = Key.create("JSGraphQL.IntrospectionSDLToJSON");

    /**
     * Set on the PSI File that is the SDL version of a JSON Introspection file
     */
    public static final Key<Boolean> IS_GRAPHQL_INTROSPECTION_SDL = Key.create("JSGraphQL.IsIntrospectionSDL");

    /**
     * Set on a scratch Virtual File to indicate which project it's been associated with
     */
    public static final Key<String> GRAPHQL_SCRATCH_PROJECT_KEY = Key.create("JSGraphQL.ScratchProjectKey");

}
