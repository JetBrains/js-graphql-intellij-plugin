 /**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint;

 import org.junit.Test;

 import com.intellij.openapi.command.WriteCommandAction;
 import com.intellij.psi.codeStyle.CodeStyleManager;
 import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
 import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;


 public class JSGraphQLEndpointCodeInsightTest extends LightCodeInsightFixtureTestCase {

     @Override
    protected String getTestDataPath() {
        return "test-resources/testData/endpoint";
    }

    @Test
    public void testFormatter() {
        myFixture.configureByFiles("FormatterTestData.graphqle");
        CodeStyleSettingsManager.getSettings(getProject()).KEEP_BLANK_LINES_IN_CODE = 2;
        new WriteCommandAction.Simple(getProject()) {
            @Override
            protected void run() throws Throwable {
                CodeStyleManager.getInstance(getProject()).reformat(myFixture.getFile());
            }
        }.execute();
        myFixture.checkResultByFile("FormatterExpectedResult.graphqle");
    }

}
