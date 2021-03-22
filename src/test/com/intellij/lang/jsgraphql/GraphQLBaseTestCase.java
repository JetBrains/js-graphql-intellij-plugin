package com.intellij.lang.jsgraphql;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public abstract class GraphQLBaseTestCase extends BasePlatformTestCase {
    @Override
    protected String getTestDataPath() {
        return GraphQLTestUtils.getTestDataPath(getBasePath());
    }
}
