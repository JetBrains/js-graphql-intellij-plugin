/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.lang.jsgraphql.GraphQLTestCaseBase;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.types.language.NamedNode;
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.locks.Lock;


/**
 * Verifies that two schemas can be separated using graphql-config
 */
public class GraphQLSchemaConfigTest extends GraphQLTestCaseBase {
    @Override
    protected @NotNull String getBasePath() {
        return "/graphql-config";
    }

    public void testCompletionSchemas() {
        PsiFile[] files = myFixture.configureByFiles(
            "completionSchemas/schema-one/.graphqlconfig",
            "completionSchemas/schema-one/schema-one.graphql",
            "completionSchemas/schema-two/.graphqlconfig",
            "completionSchemas/schema-two/schema-two.graphql",
            "completionSchemas/schema-two/schema-excluded-two.graphql",
            "completionSchemas/schema-one/query-one.graphql",
            "completionSchemas/schema-two/query-two.graphql"
        );
        loadConfiguration();

        doTestCompletion("completionSchemas/schema-one/query-one.graphql", Lists.newArrayList("fieldOne"), files);
        doTestCompletion("completionSchemas/schema-two/query-two.graphql", Lists.newArrayList("fieldTwo"), files);
    }

    public void testExcludeFilesAndDirectories() {
        test("/Types3.graphql", "TheOnlyType");
    }

    private void test(@NotNull String initialFile, String @NotNull ... expectedTypes) {
        VirtualFile directory = myFixture.copyDirectoryToProject(getTestName(true), "/");
        loadConfiguration();
        VirtualFile file = directory.findFileByRelativePath(initialFile);
        assertNotNull(file);
        PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(file);
        assertNotNull(psiFile);
        TypeDefinitionRegistry registry = GraphQLRegistryProvider.getInstance(getProject()).getRegistryInfo(psiFile).getTypeDefinitionRegistry();
        assertSameElements(ContainerUtil.map(registry.types().values(), NamedNode::getName), expectedTypes);
    }

    private void doTestCompletion(String sourceFile, List<String> expectedCompletions, PsiFile @NotNull [] files) {
        for (PsiFile file : files) {
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
    }
}
