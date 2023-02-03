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
import com.intellij.lang.jsgraphql.types.language.NamedNode;
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Verifies that two schemas can be separated using graphql-config
 */
public class GraphQLSchemaConfigTest extends GraphQLTestCaseBase {
    @Override
    protected @NotNull String getBasePath() {
        return "/schema/config";
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
        reloadConfiguration();

        doTestCompletion("completionSchemas/schema-one/query-one.graphql", Lists.newArrayList("fieldOne", "__typename"), files);
        doTestCompletion("completionSchemas/schema-two/query-two.graphql", Lists.newArrayList("fieldTwo", "__typename"), files);
    }

    public void testExcludeFilesAndDirectories() {
        test("Types3.graphql", "TheOnlyType");
    }

    private void test(@NotNull String initialFile, String @NotNull ... expectedTypes) {
        VirtualFile directory = myFixture.copyDirectoryToProject(getTestName(true), "/");
        reloadConfiguration();
        VirtualFile file = directory.findFileByRelativePath(initialFile);
        assertNotNull(file);
        PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(file);
        assertNotNull(psiFile);
        TypeDefinitionRegistry registry = GraphQLRegistryProvider.getInstance(getProject())
            .getRegistryInfo(psiFile).getTypeDefinitionRegistry();

        List<String> types = registry.types().values().stream()
            .map(NamedNode::getName)
            .filter(type -> !GraphQLKnownTypes.isIntrospectionType(type))
            .collect(Collectors.toList());
        assertSameElements(types, expectedTypes);
    }

    private void doTestCompletion(String sourceFile, List<String> expectedCompletions, PsiFile @NotNull [] files) {
        for (PsiFile file : files) {
            if (file.getVirtualFile().getPath().endsWith(sourceFile)) {
                myFixture.configureFromExistingVirtualFile(file.getVirtualFile());
                break;
            }
        }
        myFixture.complete(CompletionType.BASIC, 1);
        final List<String> completions = myFixture.getLookupElementStrings();
        assertEquals("Wrong completions", expectedCompletions, completions);
    }
}
