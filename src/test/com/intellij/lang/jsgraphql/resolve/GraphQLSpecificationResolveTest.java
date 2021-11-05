package com.intellij.lang.jsgraphql.resolve;

import com.intellij.lang.jsgraphql.GraphQLResolveTestCaseBase;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.psi.PsiElement;

public class GraphQLSpecificationResolveTest extends GraphQLResolveTestCaseBase {

    @Override
    protected String getBasePath() {
        return "/resolve/specification";
    }

    public void testDirectiveLocation() {
        PsiElement target = doResolveAsTextTest(GraphQLEnumValueDefinition.class, "FIELD_DEFINITION");
        assertContainingDefinition(target, GraphQLEnumTypeDefinition.class, "__DirectiveLocation");
    }

    public void testScalar() {
        doResolveAsTextTest(GraphQLScalarTypeDefinition.class, "String");
    }

    public void testDirective() {
        doResolveAsTextTest(GraphQLDirectiveDefinition.class, "deprecated");
    }

    public void testTypeName() {
        PsiElement target = doResolveAsTextTest(GraphQLFieldDefinition.class, "__typename");
        assertContainingDefinition(target, GraphQLObjectTypeDefinition.class, "__TypeNameMeta");
    }

    public void testTypeNameNested() {
        PsiElement target = doResolveAsTextTest(GraphQLFieldDefinition.class, "__typename");
        assertContainingDefinition(target, GraphQLObjectTypeDefinition.class, "__TypeNameMeta");
    }

    public void testTypeNameUnion() {
        PsiElement target = doResolveAsTextTest(GraphQLFieldDefinition.class, "__typename");
        assertContainingDefinition(target, GraphQLObjectTypeDefinition.class, "__TypeNameMeta");
    }

    public void testTypeNameInterface() {
        PsiElement target = doResolveAsTextTest(GraphQLFieldDefinition.class, "__typename");
        assertContainingDefinition(target, GraphQLObjectTypeDefinition.class, "__TypeNameMeta");
    }

    public void testTypeNameInlineFragment() {
        PsiElement target = doResolveAsTextTest(GraphQLFieldDefinition.class, "__typename");
        assertContainingDefinition(target, GraphQLObjectTypeDefinition.class, "__TypeNameMeta");
    }

    public void testIntrospectionType() {
        PsiElement target = doResolveAsTextTest(GraphQLFieldDefinition.class, "__type");
        assertContainingDefinition(target, GraphQLObjectTypeDefinition.class, "__QueryIntrospectionMeta");
    }

    public void testIntrospectionTypeFieldNames() {
        PsiElement target = doResolveAsTextTest(GraphQLFieldDefinition.class, "name");
        assertContainingDefinition(target, GraphQLObjectTypeDefinition.class, "__Field");
    }

    // TODO: [resolve] fix
    public void _testIntrospectionTypesOnRootLevelOnly() {
        doHighlightingTest();
    }
}
