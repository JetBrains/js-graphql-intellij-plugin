package com.intellij.lang.jsgraphql;

import com.google.common.collect.Lists;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.ide.references.GraphQLResolveUtil;
import com.intellij.lang.jsgraphql.ide.validation.inspections.*;
import com.intellij.lang.jsgraphql.psi.GraphQLDirectiveDefinition;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLIdentifierImpl;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLTypeNameDefinitionOwner;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public abstract class GraphQLTestCaseBase extends BasePlatformTestCase {

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
        GraphQLInterfaceImplementationInspection.class,
        GraphQLDuplicateDirectiveInspection.class,
        GraphQLMissingTypeInspection.class,
        GraphQLIllegalDirectiveArgumentInspection.class,
        GraphQLInvalidDirectiveLocationInspection.class
    );

    @Override
    protected final String getTestDataPath() {
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

    @NotNull
    protected String readFileAsString(@NotNull VirtualFile file) {
        try {
            return StringUtil.convertLineSeparators(VfsUtil.loadText(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void assertNamedElement(@Nullable PsiElement element,
                                      @NotNull Class<? extends PsiElement> expectedClass,
                                      @NotNull String expectedName) {
        assertInstanceOf(element, expectedClass);

        PsiNamedElement namedElement;
        if (element instanceof GraphQLTypeNameDefinitionOwner) {
            namedElement = ((GraphQLTypeNameDefinitionOwner) element).getTypeNameDefinition();
        } else if (element instanceof GraphQLDirectiveDefinition) {
            // for some reason elements which are supposed to implement PsiNamedElement don't implement it
            // GraphQLIdentifier interface also doesn't, so we need this explicit downcast to the GraphQLIdentifierImpl
            namedElement = ((GraphQLIdentifierImpl) ((GraphQLDirectiveDefinition) element).getNameIdentifier());
        } else {
            assertInstanceOf(element, PsiNamedElement.class);
            namedElement = (PsiNamedElement) element;
        }
        assertNotNull(namedElement);
        assertEquals(expectedName, namedElement.getName());
    }

    protected void assertContainingDefinition(@Nullable PsiElement element,
                                              @NotNull Class<? extends PsiElement> expectedClass,
                                              @NotNull String expectedName) {
        assertNamedElement(GraphQLResolveUtil.findContainingDefinition(element), expectedClass, expectedName);
    }
}
