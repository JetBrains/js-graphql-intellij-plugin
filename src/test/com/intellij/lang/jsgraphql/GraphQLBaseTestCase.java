package com.intellij.lang.jsgraphql;

import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public abstract class GraphQLBaseTestCase extends BasePlatformTestCase {
    @Override
    protected String getTestDataPath() {
        return GraphQLTestUtils.getTestDataPath(getBasePath());
    }

    protected void loadConfiguration() {
        // use the synchronous method of building the configuration for the unit test
        GraphQLConfigManager.getService(getProject()).doBuildConfigurationModel(null);
    }

    protected void doHighlightingTest() {
        myFixture.configureByFile(getTestName(false) + ".graphql");
        myFixture.checkHighlighting();
    }
}
