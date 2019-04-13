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
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.junit.Test;

import java.util.List;


public class GraphQLInjectionCodeInsightTest extends LightCodeInsightFixtureTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.configureByFiles(".graphqlconfig", "schema.graphql");
        // use the synchronous method of building the configuration for the unit test
        GraphQLConfigManager.getService(getProject()).doBuildConfigurationModel(null);
    }

    @Override
    protected String getTestDataPath() {
        return "test-resources/testData/graphql/injection";
    }

    // ---- highlighting -----

    @Test
    public void testErrorAnnotator() {
        myFixture.configureByFiles("injection-comment.js");
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEquals("Expected just one error", 1, highlighting.size());
        assertEquals("Unknown fragment name should be the error", "OnlyTheUnknownFragmentShouldBeHighlightedAsError", highlighting.get(0).getText());
    }

}
