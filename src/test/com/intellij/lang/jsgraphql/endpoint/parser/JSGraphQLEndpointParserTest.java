/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.parser;

import org.junit.Test;

import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointParserDefinition;
import com.intellij.testFramework.ParsingTestCase;

public class JSGraphQLEndpointParserTest extends ParsingTestCase {

    public JSGraphQLEndpointParserTest() {
        super("", "graphqle", new JSGraphQLEndpointParserDefinition());
    }

    @Test
    public void testParsingTestData() {
        doTest(true);
    }

    @Test
    public void testErrorRecoveryTestData1() {
        doTest(true);
    }

    @Override
    protected String getTestDataPath() {
        return "test-resources/testData/endpoint";
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
