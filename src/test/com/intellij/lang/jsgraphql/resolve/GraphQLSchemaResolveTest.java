package com.intellij.lang.jsgraphql.resolve;

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase;
import com.intellij.lang.jsgraphql.psi.GraphQLEnumValue;
import com.intellij.lang.jsgraphql.psi.GraphQLInputValueDefinition;
import com.intellij.psi.PsiElement;

public class GraphQLSchemaResolveTest extends GraphQLTestCaseBase {
    @Override
    protected String getBasePath() {
        return "/resolve/schema";
    }

    public void testDirectiveObjectArgumentValue() {
        PsiElement target = doResolveAsTextTest("owner");
        assertInstanceOf(target.getParent(), GraphQLEnumValue.class);
    }

    public void testDirectiveObjectArgumentField() {
        PsiElement target = doResolveAsTextTest("allow");
        assertInstanceOf(target.getParent(), GraphQLInputValueDefinition.class);
    }

    public void testDefaultArgumentObjectValue() {
        PsiElement target = doResolveAsTextTest("field");
        assertInstanceOf(target.getParent(), GraphQLInputValueDefinition.class);
    }

    public void testDefaultArgumentObjectArrayValue() {
        PsiElement target = doResolveAsTextTest("field");
        assertInstanceOf(target.getParent(), GraphQLInputValueDefinition.class);
    }

    public void testDefaultArgumentObjectRecursiveArrayValue() {
        PsiElement target = doResolveAsTextTest("order");
        assertInstanceOf(target.getParent(), GraphQLInputValueDefinition.class);
    }
}
