package com.intellij.lang.jsgraphql.resolve;

import com.intellij.lang.jsgraphql.GraphQLResolveTestCaseBase;
import com.intellij.lang.jsgraphql.psi.*;

public class GraphQLSchemaResolveTest extends GraphQLResolveTestCaseBase {
    @Override
    protected String getBasePath() {
        return "/resolve/schema";
    }

    public void testGithubSchema() {
        doHighlightingTest();
    }

    public void testAniListSchema() {
        doHighlightingTest();
    }

    public void testBitQuerySchema() {
        doHighlightingTest();
    }

    public void testUniverseSchema() {
        doHighlightingTest();
    }

    public void testDirectiveArgumentObjectFieldValue() {
        doResolveWithOffsetTest(GraphQLEnumValueDefinition.class, "owner");
    }

    public void testDirectiveArgumentObjectField() {
        doResolveWithOffsetTest(GraphQLInputValueDefinition.class, "allow");
    }

    public void testDefaultArgumentObjectField() {
        doResolveWithOffsetTest(GraphQLInputValueDefinition.class, "field");
    }

    public void testDirectiveArgumentEnumValue() {
        doResolveWithOffsetTest(GraphQLEnumValueDefinition.class, "FOREVER");
    }

    public void testDefaultArgumentObjectFieldInsideArray() {
        doResolveWithOffsetTest(GraphQLInputValueDefinition.class, "field");
    }

    public void testDefaultArgumentObjectFieldNestedCircular() {
        doResolveWithOffsetTest(GraphQLInputValueDefinition.class, "order");
    }

    public void testObjectFieldDefinitionObjectType() {
        doResolveWithOffsetTest(GraphQLObjectTypeDefinition.class, "Location");
    }

    public void testObjectFieldDefinitionEnumType() {
        doResolveWithOffsetTest(GraphQLEnumTypeDefinition.class, "CheckStatusState");
    }

    public void testObjectFieldDefinitionScalarType() {
        doResolveWithOffsetTest(GraphQLScalarTypeDefinition.class, "Upload");
    }

    public void testObjectFieldDefinitionUnionType() {
        doResolveWithOffsetTest(GraphQLUnionTypeDefinition.class, "Node");
    }

    public void testObjectFieldDefinitionInterfaceType() {
        doResolveWithOffsetTest(GraphQLInterfaceTypeDefinition.class, "Node");
    }

    public void testArgumentDefinitionObjectType() {
        doResolveWithOffsetTest(GraphQLObjectTypeDefinition.class, "Argument");
    }

    public void testArgumentDefinitionInputType() {
        doResolveWithOffsetTest(GraphQLInputObjectTypeDefinition.class, "Argument");
    }

    public void testDirectiveArgument() {
        doResolveWithOffsetTest(GraphQLInputValueDefinition.class, "fieldType");
    }

    public void testSchemaOperationQueryType() {
        doResolveWithOffsetTest(GraphQLObjectTypeDefinition.class, "CustomQuery");
    }

    public void testSchemaOperationMutationType() {
        doResolveWithOffsetTest(GraphQLObjectTypeDefinition.class, "CustomMutation");
    }

    public void testSchemaOperationSubscriptionType() {
        doResolveWithOffsetTest(GraphQLObjectTypeDefinition.class, "CustomSubscription");
    }

    public void testSchemaExtensionOperationQueryType() {
        doResolveWithOffsetTest(GraphQLObjectTypeDefinition.class, "CustomQuery");
    }

    public void testSchemaExtensionOperationMutationType() {
        doResolveWithOffsetTest(GraphQLObjectTypeDefinition.class, "CustomMutation");
    }

    public void testSchemaExtensionOperationSubscriptionType() {
        doResolveWithOffsetTest(GraphQLObjectTypeDefinition.class, "CustomSubscription");
    }

    public void testInterfaceFieldDefinitionObjectType() {
        doResolveWithOffsetTest(GraphQLObjectTypeDefinition.class, "Location");
    }

    public void testInputFieldDefinitionInputType() {
        doResolveWithOffsetTest(GraphQLInputObjectTypeDefinition.class, "LocationInput");
    }

    public void testObjectTypeImplementsInterface() {
        doResolveWithOffsetTest(GraphQLInterfaceTypeDefinition.class, "Entity");
    }

    public void testObjectTypeDirective() {
        doResolveWithOffsetTest(GraphQLDirectiveDefinition.class, "table");
    }

    public void testObjectFieldDefinitionDirective() {
        doResolveWithOffsetTest(GraphQLDirectiveDefinition.class, "field");
    }

    public void testInterfaceTypeImplementsInterface() {
        doResolveWithOffsetTest(GraphQLInterfaceTypeDefinition.class, "Node");
    }

    public void testInterfaceTypeImplementsCircular() {
        doResolveWithOffsetTest(GraphQLInterfaceTypeDefinition.class, "Named");
    }

    public void testInputObjectTypeCircular() {
        doResolveWithOffsetTest(GraphQLInputObjectTypeDefinition.class, "Example");
    }

    public void testInputObjectTypeCircularList() {
        doResolveWithOffsetTest(GraphQLInputObjectTypeDefinition.class, "Example");
    }

    public void testUnionTypeMember() {
        doResolveWithOffsetTest(GraphQLObjectTypeDefinition.class, "B");
    }

    public void testUnionTypeMemberCircular() {
        doResolveWithOffsetTest(GraphQLUnionTypeDefinition.class, "U");
    }
}
