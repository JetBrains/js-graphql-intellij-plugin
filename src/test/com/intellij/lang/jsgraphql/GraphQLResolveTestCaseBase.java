package com.intellij.lang.jsgraphql;

import com.intellij.lang.jsgraphql.ide.resolve.GraphQLResolveUtil;
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLUnresolvedReferenceInspection;
import com.intellij.lang.jsgraphql.psi.GraphQLIdentifier;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.VfsTestUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class GraphQLResolveTestCaseBase extends GraphQLTestCaseBase {
    public static final String REF_MARK = "<ref>";
    public static final String CARET_MARK = "<caret>";

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // all resolve tests should also check highlighting to ensure that the rest references are also resolved
        myFixture.enableInspections(List.of(GraphQLUnresolvedReferenceInspection.class));
    }

    @NotNull
    protected PsiElement doResolveAsTextTest(@NotNull Class<? extends PsiElement> expectedClass, @NotNull String expectedName) {
        PsiReference reference = myFixture.getReferenceAtCaretPosition(getTestName(false) + ".graphql");
        assertNotNull(reference);
        PsiElement element = reference.resolve();
        assertInstanceOf(element, PsiNamedElement.class);
        assertEquals(expectedName, ((PsiNamedElement) element).getName());
        PsiElement definition = GraphQLResolveUtil.findResolvedDefinition(element);
        assertInstanceOf(definition, expectedClass);

        myFixture.checkHighlighting();

        return definition;
    }

    @NotNull
    protected PsiElement doResolveWithOffsetTest(@NotNull Class<? extends PsiElement> expectedClass,
                                                 @NotNull String expectedName) {
        String fileName = getTestName(false) + ".graphql";
        String path = FileUtil.join(getTestDataPath(), fileName);
        VirtualFile file = VfsTestUtil.findFileByCaseSensitivePath(path);
        assertNotNull(file);

        String text = readFileAsString(file);
        String textWithoutCarets = text.replace(CARET_MARK, "");
        int refOffset = textWithoutCarets.indexOf(REF_MARK);
        assertTrue(refOffset >= 0);

        PsiFile psiFile = prepareFile(fileName, text);
        reloadConfiguration();
        PsiElement target = findElementAndResolve(psiFile);
        assertEquals(target.getTextOffset(), refOffset);
        assertInstanceOf(target, PsiNamedElement.class);
        assertEquals(expectedName, ((PsiNamedElement) target).getName());

        PsiElement definition = GraphQLResolveUtil.findResolvedDefinition(target);
        assertInstanceOf(definition, expectedClass);

        myFixture.checkHighlighting();

        return definition;
    }

    @NotNull
    private PsiFile prepareFile(@NotNull String fileName, @NotNull String text) {
        text = text.replace(REF_MARK, "");
        return myFixture.configureByText(fileName, text);
    }

    @NotNull
    private PsiElement findElementAndResolve(@NotNull PsiFile psiFile) {
        PsiElement element = PsiTreeUtil.getParentOfType(psiFile.findElementAt(myFixture.getCaretOffset()), GraphQLIdentifier.class);
        assertNotNull(element);
        PsiReference reference = element.getReference();
        assertNotNull("Reference is null", reference);
        PsiElement target = reference.resolve();
        assertNotNull("Resolved reference is null", target);

        return target;
    }
}
