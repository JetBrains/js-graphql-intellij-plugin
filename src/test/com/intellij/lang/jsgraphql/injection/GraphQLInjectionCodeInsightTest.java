/*
 * Copyright (c) 2019-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.injection;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.Test;

import java.util.List;


public class GraphQLInjectionCodeInsightTest extends LightPlatformCodeInsightFixtureTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.configureByFiles(
                "schema.graphql",
                ".graphqlconfig",
                "lines-1/.graphqlconfig",
                "lines-2/.graphqlconfig"
        );
        // use the synchronous method of building the configuration for the unit test
        GraphQLConfigManager.getService(getProject()).doBuildConfigurationModel(null);
    }

    @Override
    protected String getTestDataPath() {
        return "test-resources/testData/graphql/injection";
    }

    // ---- highlighting -----

    @Test
    public void testErrorAnnotatorOnFragments() {
        myFixture.configureByFiles("injection-comment.js");
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEquals("Expected just one error", 1, highlighting.size());
        assertEquals("Unknown fragment name should be the error", "OnlyTheUnknownFragmentShouldBeHighlightedAsError", highlighting.get(0).getText());
    }

    @Test
    public void testErrorAnnotatorSourceLines1() {
        myFixture.configureByFiles("lines-1/injection-source-lines-1.js");
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEquals("Expected just one error", 1, highlighting.size());
        assertEquals("Should mark ServerType with an error", "ServerType", highlighting.get(0).getText());
        assertEquals("Should mark ServerType in the right injected position", 201, highlighting.get(0).getStartOffset());
    }

    @Test
    public void testErrorAnnotatorSourceLines2() {
        myFixture.configureByFiles("lines-2/injection-source-lines-2.js");
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEquals("Expected just one error", 1, highlighting.size());
        assertEquals("Should mark OutputType with an error", "OutputType", highlighting.get(0).getText());
        assertEquals("Should mark OutputType in the right injected position", 209, highlighting.get(0).getStartOffset());
    }

}
