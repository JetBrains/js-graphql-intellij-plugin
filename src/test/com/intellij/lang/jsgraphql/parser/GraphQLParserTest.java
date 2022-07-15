/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.parser;

import com.intellij.lang.jsgraphql.GraphQLParserDefinition;
import com.intellij.testFramework.ParsingTestCase;
import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.runner.RunWith;

@RunWith(JUnit38ClassRunner.class) // TODO: drop the annotation when issue with Gradle test scanning go away
public class GraphQLParserTest extends ParsingTestCase {

    public GraphQLParserTest() {
        super("", "graphql", new GraphQLParserDefinition());
    }

    public void testParsingTestData() {
        doTest(true);
    }

    public void testDirectives() {
        doTest(true, true);
    }

    public void testSchemaDescriptions() {
        doTest(true, true);
    }

    public void testSingleLineDescriptions() {
        doTest(true, true);
    }

    public void testMultilineDescriptions() {
        doTest(true, true);
    }

    public void testExtendSchema() {
        doTest(true, true);
    }

    public void testEmoji() {
        doTest(true, true);
    }

    public void testKeywordsAsIdentifiers() {
        doTest(true, true);
    }

    @Override
    protected String getTestDataPath() {
        return "test-resources/testData/graphql/parser";
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
