package com.intellij.lang.jsgraphql.resolve;

import com.intellij.lang.jsgraphql.GraphQLResolveTestCaseBase;
import com.intellij.lang.jsgraphql.psi.GraphQLEnumValueDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLInputValueDefinition;

public class GraphQLSchemaResolveTest extends GraphQLResolveTestCaseBase {
    @Override
    protected String getBasePath() {
        return "/resolve/schema";
    }

    public void testDirectiveObjectArgumentValue() {
        doResolveWithOffsetTest(GraphQLEnumValueDefinition.class, "owner");
    }

    public void testDirectiveObjectArgumentField() {
        doResolveWithOffsetTest(GraphQLInputValueDefinition.class, "allow");
    }

    public void testDefaultArgumentObjectValue() {
        doResolveWithOffsetTest(GraphQLInputValueDefinition.class, "field");
    }

    public void testDefaultArgumentObjectArrayValue() {
        doResolveWithOffsetTest(GraphQLInputValueDefinition.class, "field");
    }

    public void testDefaultArgumentObjectRecursiveArrayValue() {
        doResolveWithOffsetTest(GraphQLInputValueDefinition.class, "order");
    }
}
