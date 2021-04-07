package com.intellij.lang.jsgraphql;

import com.google.common.collect.Lists;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.ide.validation.inspections.*;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class GraphQLBaseTestCase extends BasePlatformTestCase {
    protected static final List<Class<? extends LocalInspectionTool>> ourGeneralInspections = Lists.newArrayList(
        GraphQLUnresolvedReferenceInspection.class
    );

    protected static final List<Class<? extends LocalInspectionTool>> ourSchemaInspections = Lists.newArrayList(
        GraphQLSchemaValidationInspection.class,
        GraphQLTypeRedefinitionInspection.class,
        GraphQLUnexpectedTypeInspection.class,
        GraphQLMemberRedefinitionInspection.class,
        GraphQLIllegalNameInspection.class,
        GraphQLDuplicateArgumentInspection.class,
        GraphQLEmptyTypeInspection.class,
        GraphQLInterfaceImplementedInspection.class,
        GraphQLDuplicateDirectiveInspection.class,
        GraphQLMissingTypeInspection.class,
        GraphQLIllegalArgumentInspection.class
    );

    @Override
    protected String getTestDataPath() {
        return GraphQLTestUtils.getTestDataPath(getBasePath());
    }

    protected void loadConfiguration() {
        // use the synchronous method of building the configuration for the unit test
        GraphQLConfigManager.getService(getProject()).doBuildConfigurationModel(null);
    }

    protected void doHighlightingTest() {
        doHighlightingTest("graphql");
    }

    protected void doHighlightingTest(@NotNull String ext) {
        myFixture.configureByFile(getTestName(false) + "." + ext);
        myFixture.checkHighlighting();
    }

    protected final void enableAllInspections() {
        myFixture.enableInspections(ourGeneralInspections);
        myFixture.enableInspections(ourSchemaInspections);
    }
}
