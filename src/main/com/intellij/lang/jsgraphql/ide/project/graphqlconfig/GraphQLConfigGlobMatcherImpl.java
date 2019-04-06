/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.graphqlconfig;

import com.google.common.collect.Maps;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.Pair;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.io.IOUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Matcher which uses nashorn-minimatch to achieve same glob semantics as graphql-config.
 *
 * See /META-INF/minimatch-nashorn.js and https://github.com/prismagraphql/graphql-config/blob/b6785a7f0c1b84010cd6e9b94797796254d527b9/src/utils.ts#L52
 */
public class GraphQLConfigGlobMatcherImpl implements GraphQLConfigGlobMatcher {

    private static final String NASHORN_SCRIPT_OBJECT = "__nashorn__";

    private final static Map<String, ScriptEngine> scriptEngines = Maps.newConcurrentMap();
    private final static Map<Pair<String, String>, Boolean> matches = Maps.newConcurrentMap();

    private final static PluginDescriptor pluginDescriptor = PluginManager.getPlugin(PluginId.getId("com.intellij.lang.jsgraphql"));

    @Override
    public boolean matches(String filePath, String glob) {
        if (pluginDescriptor == null) {
            throw new IllegalStateException("Plugin description is null. Can not load minimatch-nashorn.js");
        }
        return matches.computeIfAbsent(Pair.create(filePath, glob), args -> {
            final ScriptEngine scriptEngine = scriptEngines.computeIfAbsent("default", s -> {
                final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
                try {
                    engine.put(NASHORN_SCRIPT_OBJECT, engine.eval("new Object()"));
                    final String script;
                    if(ApplicationManager.getApplication().isUnitTestMode()) {
                        // plugin class loader appears to be unable to locate certain resources during unit-test?!
                        script = IOUtils.toString(getClass().getResource("/META-INF/minimatch-nashorn.js"), Charset.forName("UTF-8"));
                    } else {
                        script = IOUtils.resourceToString("/META-INF/minimatch-nashorn.js", Charset.forName("UTF-8"), pluginDescriptor.getPluginClassLoader());
                    }
                    engine.eval(script);
                } catch (IOException | ScriptException e) {
                    throw new RuntimeException("Unable to load minimatch-nashorn.js", e);
                }
                return engine;
            });
            final ScriptObjectMirror nashornScriptObject = (ScriptObjectMirror) scriptEngine.get(NASHORN_SCRIPT_OBJECT);
            return (Boolean) nashornScriptObject.callMember("minimatch", args.first, args.second);
        });
    }

}
