package com.intellij.lang.jsgraphql;

import com.google.common.collect.Lists;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.ide.validation.inspections.*;
import com.intellij.lang.jsgraphql.psi.GraphQLIdentifier;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.VfsTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public abstract class GraphQLTestCaseBase extends BasePlatformTestCase {
    public static final String REF_MARK = "<ref>";
    public static final String CARET_MARK = "<caret>";

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
    protected PsiElement doResolveAsTextTest(@NotNull String expectedText) {
        PsiReference reference = myFixture.getReferenceAtCaretPosition(getTestName(false) + ".graphql");
        assertNotNull(reference);
        PsiElement element = reference.resolve();
        assertNotNull(element);
        assertEquals(expectedText, element.getText());
        return element;
    }

    protected @NotNull PsiElement doResolveWithOffsetTest(@NotNull Class<? extends PsiElement> expectedClass, @NotNull String expectedText) {
        String fileName = getTestName(false) + ".graphql";
        String path = FileUtil.join(getTestDataPath(), fileName);
        VirtualFile file = VfsTestUtil.findFileByCaseSensitivePath(path);
        assertNotNull(file);

        String text = readFileAsString(file);
        String textWithoutCarets = text.replace(CARET_MARK, "");
        int refOffset = textWithoutCarets.indexOf(REF_MARK);
        assertTrue(refOffset >= 0);

        PsiFile psiFile = prepareFile(fileName, text);
        loadConfiguration();
        PsiElement target = findElementAndResolve(psiFile);
        assertEquals(target.getTextOffset(), refOffset);
        assertInstanceOf(target, expectedClass);
        assertEquals(expectedText, target.getText());
        return target;
    }

    private @NotNull PsiFile prepareFile(@NotNull String fileName, @NotNull String text) {
        text = text.replace(REF_MARK, "");
        return myFixture.configureByText(fileName, text);
    }

    private @NotNull PsiElement findElementAndResolve(@NotNull PsiFile psiFile) {
        PsiElement element = PsiTreeUtil.getParentOfType(psiFile.findElementAt(myFixture.getCaretOffset()), GraphQLIdentifier.class);
        assertNotNull(element);
        PsiReference reference = element.getReference();
        assertNotNull("Reference is null", reference);
        PsiElement target = reference.resolve();
        assertNotNull("Resolved reference is null", target);

        return target;
    }

    private @NotNull String readFileAsString(@NotNull VirtualFile file) {
        try {
            return StringUtil.convertLineSeparators(VfsUtil.loadText(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
