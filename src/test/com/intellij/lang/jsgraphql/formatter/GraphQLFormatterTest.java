/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.formatter;

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.codeStyle.CodeStyleManager;

public class GraphQLFormatterTest extends GraphQLTestCaseBase {

    @Override
    protected String getBasePath() {
        return "/formatter";
    }

    public void testFormatter() {
        doTest();
    }

    private void doTest() {
        myFixture.configureByFiles(getTestName(false) + ".graphql");
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            CodeStyleManager.getInstance(getProject()).reformat(myFixture.getFile());
        });
        myFixture.checkResultByFile(getTestName(false) + "_after.graphql");
    }

}
