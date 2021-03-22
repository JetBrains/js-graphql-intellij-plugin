package com.intellij.lang.jsgraphql.ide.editor;

import com.intellij.lang.jsgraphql.GraphQLBaseTestCase;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;

import java.io.IOException;
import java.util.Objects;

public class GraphQLIntrospectionServiceTest extends GraphQLBaseTestCase {

    @Override
    protected @NotNull String getBasePath() {
        return "/introspection";
    }

    public void testPrintIntrospectionJsonAsGraphQL() {
        doTest("schema.json", "schema.graphql");
    }

    public void testPrintIntrospectionJsonWithEmptyErrorsAsGraphQL() {
        doTest("schemaWithEmptyErrors.json", "schema.graphql");
    }

    public void testPrintIntrospectionJsonWithErrorsAsGraphQL() {
        try {
            doTest("schemaWithErrors.json", "schema.graphql");
        } catch (IllegalArgumentException exception) {
            Assert.assertEquals("Introspection query returned errors: [{\"message\":\"Error\"}]", exception.getMessage());
            return;
        }

        throw new RuntimeException("Expected errors exception, found none.");
    }

    public void testPrintIntrospectionWithNullFields() {
        doTest("schemaWithNullFields.json", "schemaWithNullFields.graphql");
    }

    private void doTest(@NotNull String source, @NotNull String expected) {
        myFixture.configureByText(
            "result.graphql",
            new GraphQLIntrospectionService(getProject()).printIntrospectionAsGraphQL(Objects.requireNonNull(readSchemaJson(source)))
        );
        myFixture.checkResultByFile(expected);
    }

    @Nullable
    private String readSchemaJson(@NotNull String path) {
        try {
            return VfsUtilCore.loadText(myFixture.copyFileToProject(path));
        } catch (IOException e) {
            return null;
        }
    }
}
