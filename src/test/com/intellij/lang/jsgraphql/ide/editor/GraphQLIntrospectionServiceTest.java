package com.intellij.lang.jsgraphql.ide.editor;

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase;
import com.intellij.openapi.vfs.VfsUtilCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;

import java.io.IOException;
import java.util.Objects;

public class GraphQLIntrospectionServiceTest extends GraphQLTestCaseBase {

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

    public void testGithubSchema() {
        // test only for being successful, file comparison doesn't give a meaningful result for files of this size
        assertNoThrowable(() -> new GraphQLIntrospectionService(getProject())
            .printIntrospectionAsGraphQL(Objects.requireNonNull(readSchemaJson("githubSchema.json")))
        );
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
