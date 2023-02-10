package com.intellij.lang.jsgraphql.resolve;

import com.intellij.lang.jsgraphql.GraphQLResolveTestCaseBase;
import com.intellij.lang.jsgraphql.psi.*;

public class GraphQLOperationsResolveTest extends GraphQLResolveTestCaseBase {

    private static final String GITHUB_SCHEMA = "GithubSchema.graphql";

    @Override
    protected String getBasePath() {
        return "/resolve/operations";
    }

    public void testQueryFieldRoot() {
        doResolveWithOffsetTest(GraphQLFieldDefinition.class, "name");
    }

    public void testCustomQueryFieldRoot() {
        doResolveWithOffsetTest(GraphQLFieldDefinition.class, "name");
    }

    public void testSelectionSetQueryFieldRoot() {
        doResolveWithOffsetTest(GraphQLFieldDefinition.class, "name");
    }

    public void testMutationFieldRoot() {
        doResolveWithOffsetTest(GraphQLFieldDefinition.class, "createUser");
    }

    public void testCustomMutationFieldRoot() {
        doResolveWithOffsetTest(GraphQLFieldDefinition.class, "createUser");
    }

    public void testSubscriptionFieldRoot() {
        doResolveWithOffsetTest(GraphQLFieldDefinition.class, "users");
    }

    public void testCustomSubscriptionFieldRoot() {
        doResolveWithOffsetTest(GraphQLFieldDefinition.class, "users");
    }

    public void testFragmentName() {
        doResolveWithOffsetTest(GraphQLFragmentDefinition.class, "fragment1");
    }

    public void testFragmentObjectField() {
        doResolveWithOffsetTest(GraphQLFieldDefinition.class, "name");
    }

    public void testFragmentObjectFieldExtension() {
        doResolveWithOffsetTest(GraphQLFieldDefinition.class, "email");
    }

    public void testFragmentInterfaceField() {
        doResolveWithOffsetTest(GraphQLFieldDefinition.class, "id");
    }

    public void testFragmentInterfaceFieldExtension() {
        doResolveWithOffsetTest(GraphQLFieldDefinition.class, "createdAt");
    }

    public void testFragmentUnionField() {
        doResolveWithOffsetTest(GraphQLFieldDefinition.class, "language");
    }

    public void testFragmentUnionFieldExtension() {
        doResolveWithOffsetTest(GraphQLFieldDefinition.class, "someOtherField");
    }

    public void testFragmentInlineAnonymous() {
        doResolveWithOffsetTest(GraphQLFieldDefinition.class, "id");
    }

    public void testFieldArgument() {
        doResolveWithOffsetTest(GraphQLInputValueDefinition.class, "after");
    }

    public void testDirectiveArgument() {
        doResolveWithOffsetTest(GraphQLInputValueDefinition.class, "second");
    }

    public void testDirective() {
        doResolveWithOffsetTest(GraphQLDirectiveDefinition.class, "someDir");
    }

    public void testInputValue() {
        doResolveWithOffsetTest(GraphQLInputValueDefinition.class, "address");
    }

    public void testInputValueNested() {
        doResolveWithOffsetTest(GraphQLInputValueDefinition.class, "zip");
    }

    public void testInputValueDefinitionDefault() {
        doResolveWithOffsetTest(GraphQLInputValueDefinition.class, "street");
    }

    public void testGithubQueries() {
        myFixture.copyFileToProject(GITHUB_SCHEMA);
        doHighlightingTest();
    }

    public void testUnresolvedReferences() {
        myFixture.copyFileToProject(GITHUB_SCHEMA);
        doHighlightingTest();
    }
}
