package com.intellij.lang.jsgraphql.frameworks.federation;

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase;
import com.intellij.lang.jsgraphql.GraphQLTestUtils;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaInfo;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryTypes;
import org.jetbrains.annotations.NotNull;

public class GraphQLFederationValidationTest extends GraphQLTestCaseBase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        enableAllInspections();
    }

    @Override
    protected @NotNull String getBasePath() {
        return "/frameworks/federation/validation";
    }

    public void testQueryValidation() {
        GraphQLTestUtils.withLibrary(getProject(), GraphQLLibraryTypes.FEDERATION, () -> {
            doHighlightingTest();

            GraphQLSchemaInfo schemaInfo = GraphQLSchemaProvider.getInstance(getProject()).getSchemaInfo(myFixture.getFile());
            assertNotNull(schemaInfo);
            assertEmpty(schemaInfo.getErrors(getProject()));
        }, getTestRootDisposable());
    }

}
