package com.intellij.lang.jsgraphql.schema;

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.schema.idl.SchemaPrinter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;


public class GraphQLSchemaBuilderTest extends GraphQLTestCaseBase {

    @Override
    protected @NotNull String getBasePath() {
        return "/schema/builder";
    }

    public void testObjects() {
        doTest();
    }

    public void testInterfaces() {
        doTest();
    }

    public void testUnions() {
        doTest();
    }

    public void testInputObjects() {
        doTest();
    }

    public void testScalars() {
        doTest();
    }

    public void testEnums() {
        doTest();
    }

    public void testDirectives() {
        doTest(true, builder -> builder.includeDirectiveDefinitions(true));
    }

    public void testSchemas() {
        doTest();
    }

    public void testSpecifiedByAndDeprecatedDirectives() {
        doTest(false);
    }

    private void doTest() {
        doTest(true);
    }

    private void doTest(boolean withAst) {
        doTest(withAst, null);
    }

    private void doTest(boolean withAst, @Nullable UnaryOperator<SchemaPrinter.Options> optionsBuilder) {
        myFixture.configureByFile(getTestName(true) + ".graphql");
        GraphQLSchemaProvider schemaProvider = GraphQLSchemaProvider.getInstance(myFixture.getProject());
        GraphQLSchema schema = schemaProvider.getSchemaInfo(myFixture.getFile()).getSchema();

        myFixture.configureByText("schema.graphql", new SchemaPrinter(getProject(), getOptions(optionsBuilder)).print(schema));
        myFixture.checkResultByFile(getTestName(true) + "_schema.graphql");

        if (withAst) {
            myFixture.configureByText("ast.graphql", new SchemaPrinter(getOptions(optionsBuilder).useAstDefinitions(true)).print(schema));
            myFixture.checkResultByFile(getTestName(true) + "_ast.graphql");
        }
    }

    @NotNull
    private static SchemaPrinter.Options getOptions(@Nullable UnaryOperator<SchemaPrinter.Options> optionsBuilder) {
        SchemaPrinter.Options options = SchemaPrinter.Options.defaultOptions().includeDirectiveDefinitions(false);
        if (optionsBuilder != null) {
            return optionsBuilder.apply(options);
        }
        return options;
    }
}
