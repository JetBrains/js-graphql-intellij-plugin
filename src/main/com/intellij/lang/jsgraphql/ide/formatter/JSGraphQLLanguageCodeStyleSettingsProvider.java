/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.formatter;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.lang.jsgraphql.JSGraphQLLanguage;
import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class JSGraphQLLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {

    @NotNull
    @Override
    public Language getLanguage() {
        return JSGraphQLLanguage.INSTANCE;
    }

    @Nullable
    @Override
    public CommonCodeStyleSettings getDefaultCommonSettings() {
        final CommonCodeStyleSettings defaultSettings = new CommonCodeStyleSettings(getLanguage());
        CommonCodeStyleSettings.IndentOptions indentOptions = defaultSettings.initIndentOptions();
        indentOptions.INDENT_SIZE = 4;
        indentOptions.USE_TAB_CHARACTER = false;
        return defaultSettings;
    }

    @Nullable
    @Override
    public IndentOptionsEditor getIndentOptionsEditor() {
        return new IndentOptionsEditor() {
            protected void addComponents() {
                addTabOptions();
                myTabSizeField = new JTextField();
                myTabSizeLabel = new JLabel();
                myIndentField = createIndentTextField();
                myIndentLabel = new JLabel(ApplicationBundle.message("editbox.indent.indent"));
                add(myIndentLabel, myIndentField);
            }

            protected void addTabOptions() {
                myCbUseTab = new JCheckBox("");
            }
        };
    }

    @Override
    public String getCodeSample(@NotNull SettingsType settingsType) {
        return "query MyQuery {\n" +
                "    __schema {\n" +
                "        types {\n" +
                "            ...FullType\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "fragment FullType on __Type {\n" +
                "    # Note: __Type has a lot more fields than this\n" +
                "    name\n" +
                "}\n" +
                "\n" +
                "mutation MyMutation($input: MyInput!) {\n" +
                "    # Payload\n" +
                "}\n";
    }
}
