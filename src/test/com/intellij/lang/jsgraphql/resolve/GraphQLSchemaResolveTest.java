package com.intellij.lang.jsgraphql.resolve;

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase;

public class GraphQLSchemaResolveTest extends GraphQLTestCaseBase {
    @Override
    protected String getBasePath() {
        return "/resolve/schema";
    }

    public void testDirectiveObjectArgumentValue() {
        doResolveTest("owner");
    }

    public void testDirectiveObjectArgumentField() {
        doResolveTest("allow");
    }

    public void testDefaultArgumentObjectValue() {
        doResolveTest("field");
    }

    public void testDefaultArgumentObjectArrayValue() {
        doResolveTest("field");
    }

    public void testDefaultArgumentObjectRecursiveArrayValue() {
        doResolveTest("order");
    }
}
