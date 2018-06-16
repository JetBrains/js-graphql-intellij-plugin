 /**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql;

 import com.intellij.lang.jsgraphql.languageservice.CodeMirrorNodeLanguageServiceClientTest;
 import com.intellij.lang.jsgraphql.v1.languageservice.JSGraphQLNodeLanguageServiceInstance;
 import com.intellij.openapi.command.WriteCommandAction;
 import com.intellij.psi.codeStyle.CodeStyleManager;
 import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
 import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
 import org.junit.Ignore;
 import org.junit.Test;

 import java.net.URL;


 @Ignore
 public class JSGraphQLCodeInsightTest extends LightCodeInsightFixtureTestCase {
    @Override
    protected void setUp() throws Exception {

        // TODO: This test depends on a running JS GraphQL Language service at http://localhost:3000
        CodeMirrorNodeLanguageServiceClientTest.setLanguageServiceUrl(new URL("http", "localhost", 3000, JSGraphQLNodeLanguageServiceInstance.JSGRAPHQL_LANGUAGE_SERVICE_MAPPING));

        super.setUp();
    }

     @Override
     public void tearDown() throws Exception {
         super.tearDown();
         CodeMirrorNodeLanguageServiceClientTest.setLanguageServiceUrl(null);
     }

     @Override
    protected String getTestDataPath() {
        return "test-resources/testData";
    }

    @Test
    public void testFormatter() {
        myFixture.configureByFiles("FormatterTestData.graphql");
        CodeStyleSettingsManager.getSettings(getProject()).KEEP_BLANK_LINES_IN_CODE = 2;
        new WriteCommandAction.Simple(getProject()) {
            @Override
            protected void run() throws Throwable {
                CodeStyleManager.getInstance(getProject()).reformat(myFixture.getFile());
            }
        }.execute();
        myFixture.checkResultByFile("FormatterExpectedResult.graphql");
    }

}
