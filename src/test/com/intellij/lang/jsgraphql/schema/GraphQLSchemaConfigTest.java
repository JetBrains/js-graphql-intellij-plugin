/**
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.locks.Lock;


/**
 * Verifies that two schemas can be separated using graphql-config
 */
public class GraphQLSchemaConfigTest extends BasePlatformTestCase {

    private PsiFile[] files;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        files = myFixture.configureByFiles(
                "schema-one/.graphqlconfig",
                "schema-one/schema-one.graphql",
                "schema-two/.graphqlconfig",
                "schema-two/schema-two.graphql",
                "schema-two/schema-excluded-two.graphql",
                "schema-one/query-one.graphql",
                "schema-two/query-two.graphql"
        );
        // use the synchronous method of building the configuration for the unit test
        GraphQLConfigManager.getService(getProject()).doBuildConfigurationModel(null);
    }

    @Override
    protected String getTestDataPath() {
        return "test-resources/testData/graphql/graphql-config";
    }

    public void testCompletionSchemas() {
        doTestCompletion("schema-one/query-one.graphql", Lists.newArrayList("fieldOne"));
        doTestCompletion("schema-two/query-two.graphql", Lists.newArrayList("fieldTwo"));
    }

    private void doTestCompletion(String sourceFile, List<String> expectedCompletions) {
        for (PsiFile file : this.files) {
            if (file.getVirtualFile().getPath().endsWith(sourceFile)) {
                myFixture.configureFromExistingVirtualFile(file.getVirtualFile());
                break;
            }
        }
        final Lock readLock = GraphQLConfigManager.getService(getProject()).getReadLock();
        try {
            readLock.lock();
            myFixture.complete(CompletionType.BASIC, 1);
            final List<String> completions = myFixture.getLookupElementStrings();
            assertEquals("Wrong completions", expectedCompletions, completions);
        } finally {
            readLock.unlock();
        }
        ApplicationManager.getApplication().runWriteAction(() -> {
            myFixture.getEditor().getDocument().setText(""); // blank out the file so it doesn't affect other tests
            PsiDocumentManager.getInstance(myFixture.getProject()).commitAllDocuments();
        });
    }

}
