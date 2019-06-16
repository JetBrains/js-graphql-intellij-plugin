/*
 * Copyright (c) 2019-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.formatter;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraphQLLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {

    private static final String SAMPLE = "query Hero($episode: Episode!, $withFriends: Boolean!) {\n" +
            "    hero(episode: $episode) {\n" +
            "        name\n" +
            "        ...HeroDetails\n" +
            "        acquaintances: friends @include(if: $withFriends) {\n" +
            "            name\n" +
            "        }\n" +
            "    }\n" +
            "}";

    @NotNull
    @Override
    public Language getLanguage() {
        return GraphQLLanguage.INSTANCE;
    }

    @Nullable
    @Override
    public IndentOptionsEditor getIndentOptionsEditor() {
        return new IndentOptionsEditor();
    }

    @Override
    public String getCodeSample(@NotNull SettingsType settingsType) {
        return SAMPLE;
    }

}
