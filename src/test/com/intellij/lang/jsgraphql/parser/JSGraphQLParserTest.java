/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.parser;

import com.intellij.lang.jsgraphql.JSGraphQLParserDefinition;
import com.intellij.lang.jsgraphql.languageservice.CodeMirrorNodeLanguageServiceClientTest;
import com.intellij.lang.jsgraphql.languageservice.JSGraphQLNodeLanguageServiceInstance;
import com.intellij.openapi.progress.impl.ProgressManagerImpl;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.impl.ProjectManagerImpl;
import com.intellij.testFramework.ParsingTestCase;
import org.junit.Test;

import java.net.URL;

public class JSGraphQLParserTest extends ParsingTestCase {

    public JSGraphQLParserTest() {
        super("", "graphql", new JSGraphQLParserDefinition());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // TODO: This test depends on a running JS GraphQL Language service at http://localhost:3000
        CodeMirrorNodeLanguageServiceClientTest.setLanguageServiceUrl(new URL("http", "localhost", 3000, JSGraphQLNodeLanguageServiceInstance.JSGRAPHQL_LANGUAGE_SERVICE_MAPPING));
        getApplication().addComponent(ProjectManager.class, new ProjectManagerImpl(new ProgressManagerImpl()));
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        CodeMirrorNodeLanguageServiceClientTest.setLanguageServiceUrl(null);
    }

    @Test
    public void testParsingTestData() {
        doTest(true);
    }

    @Override
    protected String getTestDataPath() {
        return "test-resources/testData";
    }

    @Override
    protected boolean skipSpaces() {
        return false;
    }

    @Override
    protected boolean includeRanges() {
        return true;
    }
}