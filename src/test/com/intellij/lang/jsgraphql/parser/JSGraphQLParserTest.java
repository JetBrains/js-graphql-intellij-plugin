/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.parser;

import com.intellij.lang.jsgraphql.GraphQLParserDefinition;
import com.intellij.testFramework.ParsingTestCase;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class JSGraphQLParserTest extends ParsingTestCase {

    public JSGraphQLParserTest() {
        super("", "graphql", new GraphQLParserDefinition());
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