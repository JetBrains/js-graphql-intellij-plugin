/*
 * Copyright (c) 2019-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.injection;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.GraphQLTestCaseBase;
import com.intellij.lang.jsgraphql.ide.search.GraphQLPsiSearchHelper;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.types.language.AstPrinter;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.CommonProcessors;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GraphQLInjectionHighlightingTest extends GraphQLTestCaseBase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        enableAllInspections();
    }

    private void initPredefinedSchema() {
        myFixture.configureByFiles(
            "schema.graphql",
            ".graphqlconfig",
            "lines-1/.graphqlconfig",
            "lines-2/.graphqlconfig"
        );

        reloadConfiguration();
    }

    @Override
    protected @NotNull String getBasePath() {
        return "/injection";
    }

    public void testErrorAnnotatorOnFragments() {
        initPredefinedSchema();
        myFixture.configureByFiles("injection-comment.js");
        myFixture.checkHighlighting();
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEquals("Expected just one error", 1, highlighting.size());
        assertEquals("Unknown fragment name should be the error", "OnlyTheUnknownFragmentShouldBeHighlightedAsError",
            highlighting.get(0).getText());
    }

    public void testErrorAnnotatorSourceLines1() {
        initPredefinedSchema();
        myFixture.configureByFiles("lines-1/injection-source-lines-1.js");
        myFixture.checkHighlighting();
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEquals("Expected just one error", 1, highlighting.size());
        assertEquals("Should mark ServerType with an error", "ServerType", highlighting.get(0).getText());
        assertEquals("Should mark ServerType in the right injected position", 193, highlighting.get(0).getStartOffset());
    }

    public void testErrorAnnotatorSourceLines2() {
        initPredefinedSchema();
        myFixture.configureByFiles("lines-2/injection-source-lines-2.js");
        myFixture.checkHighlighting();
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEquals("Expected just one error", 1, highlighting.size());
        assertEquals("Should mark OutputType with an error", "OutputType", highlighting.get(0).getText());
        assertEquals("Should mark OutputType in the right injected position", 201, highlighting.get(0).getStartOffset());
    }

    public void testInjectedTemplatesDontFail() {
        PsiFile injectedFile = doTestInjectedFile("injectedTemplates/injectedTemplates.js");
        myFixture.configureByText(GraphQLFileType.INSTANCE, AstPrinter.printAst(((GraphQLFile) injectedFile).getDocument()));
        myFixture.checkResultByFile("injectedTemplates/injectedTemplates.graphql");
    }

    public void testInjectedWithEOLComment() {
        doTestInjectedFile("eolComment.js");
    }

    public void testInjectedWithEOLComment1() {
        doTestInjectedFile("eolComment1.js");
    }

    public void testInjectedWithEOLCommentInvalid() {
        doTestNoInjections("eolCommentInvalid.js");
    }

    public void testInjectedWithEOLCommentInvalid1() {
        doTestNoInjections("eolCommentInvalid1.js");
    }

    public void testInjectedWithCStyleComment() {
        doTestInjectedFile("cStyleComment.js");
    }

    public void testInjectedWithCStyleCommentTagged() {
        doTestInjectedFile("cStyleCommentTagged.js");
    }

    public void testInjectedWithCStyleCommentMultipleVars() {
        doTestInjectedFile("cStyleCommentMultipleVars.js");
    }

    private @NotNull PsiFile doTestInjectedFile(@NotNull String sourcePath) {
        myFixture.configureByFile(sourcePath);

        List<PsiFile> psiFiles = new ArrayList<>();
        GraphQLPsiSearchHelper.getInstance(getProject()).processInjectedGraphQLPsiFiles(
            GlobalSearchScope.allScope(getProject()), new CommonProcessors.CollectProcessor<>(psiFiles));
        assertSize(1, psiFiles);

        PsiFile injectedFile = psiFiles.get(0);
        assertInstanceOf(injectedFile, GraphQLFile.class);

        return injectedFile;
    }

    private void doTestNoInjections(@NotNull String sourcePath) {
        myFixture.configureByFile(sourcePath);

        List<PsiFile> psiFiles = new ArrayList<>();
        GraphQLPsiSearchHelper.getInstance(getProject()).processInjectedGraphQLPsiFiles(
            GlobalSearchScope.allScope(getProject()), new CommonProcessors.CollectProcessor<>(psiFiles));
        assertEmpty(psiFiles);
    }

}
