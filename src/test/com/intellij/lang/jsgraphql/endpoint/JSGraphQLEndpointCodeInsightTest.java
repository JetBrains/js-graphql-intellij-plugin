/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint;

import com.google.common.collect.Lists;
import com.intellij.application.options.CodeStyle;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.List;


public class JSGraphQLEndpointCodeInsightTest extends BasePlatformTestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		myFixture.addFileToProject("main.graphqle", "type MainType {}");
		myFixture.addFileToProject("importable.graphqle", "type ImportableType {}");
		myFixture.addFileToProject(GraphQLConfigManager.GRAPHQLCONFIG, "{\n" +
				"    \"extensions\": {\n" +
				"        \"endpoint-language\": {\n" +
				"            \"entry\" : \"main.graphqle\",\n" +
				"            \"annotations\": [\n" +
				"                {\n" +
				"                    \"name\": \"DataFetcher\",\n" +
				"                    \"arguments\": [\n" +
				"                        {\n" +
				"                            \"name\": \"value\",\n" +
				"                            \"type\": \"String\",\n" +
				"                            \"isDefault\": true,\n" +
				"                            \"isRequired\": true\n" +
				"                        },\n" +
				"                        {\n" +
				"                            \"name\": \"batched\",\n" +
				"                            \"type\": \"Boolean\",\n" +
				"                            \"isRequired\": false\n" +
				"                        }\n" +
				"                    ]\n" +
				"                }\n" +
				"            ]\n" +
				"        }\n" +
				"\n" +
				"    }\n" +
				"}");
		// use the synchronous method of building the configuration for the unit test
		GraphQLConfigManager.getService(getProject()).doBuildConfigurationModel(null);
	}

	@Override
	protected String getTestDataPath() {
		return "test-resources/testData/endpoint";
	}


	// ---- formatter ----

	public void testFormatter() {
		myFixture.configureByFiles("FormatterTestData.graphqle");
		CodeStyle.getSettings(getProject()).KEEP_BLANK_LINES_IN_CODE = 2;
		WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            CodeStyleManager.getInstance(getProject()).reformat(myFixture.getFile());
        });
		myFixture.checkResultByFile("FormatterExpectedResult.graphqle");
	}


	// ---- completion ----

	public void testCompletionImplementsFirstInterface() {
		doTestCompletion("CompletionImplementsFirstInterface.graphqle", Lists.newArrayList("KnownInterface1", "KnownInterface2"));
	}

	public void testCompletionImplementsSecondInterface() {
		doTestCompletion("CompletionImplementsSecondInterface.graphqle", Lists.newArrayList("KnownInterface2"));
	}

	public void testCompletionOperation() {
		doTestCompletion("CompletionOperation.graphqle", Lists.newArrayList("@DataFetcher", "annotation", "enum", "import", "input", "interface", "scalar", "schema", "type", "union"));
	}

	public void testCompletionImplementsKeyword1() {
		doTestCompletion("CompletionImplementsKeyword1.graphqle", Lists.newArrayList("implements"));
	}

	public void testCompletionImplementsKeyword2() {
		doTestCompletion("CompletionImplementsKeyword2.graphqle", Lists.newArrayList("implements"));
	}

	public void testCompletionFieldType() {
		doTestCompletion("CompletionFieldType.graphqle", Lists.newArrayList("Boolean", "Float", "Foo", "ID", "Int", "KnownInterface", "KnownType", "MyEnum", "MyUnion", "String"));
	}

	public void testCompletionInputFieldType() {
		doTestCompletion("CompletionInputFieldType.graphqle", Lists.newArrayList("Boolean", "Float", "ID", "Int", "MyEnum", "MyInput1", "String"));
	}

	public void testCompletionArgumentType() {
		doTestCompletion("CompletionArgumentType.graphqle", Lists.newArrayList("Boolean", "Float", "ID", "Int", "MyCompletionInputABC", "MyEnum", "String"));
	}

	public void testCompletionSecondArgumentType() {
		doTestCompletion("CompletionSecondArgumentType.graphqle", Lists.newArrayList("Boolean", "Float", "ID", "Int", "MyCompletionInputABC", "MyEnum", "String"));
	}

	public void testCompletionAnnotation1() {
		doTestCompletion("CompletionAnnotation1.graphqle", Lists.newArrayList("@DataFetcher"));
	}

	public void testCompletionAnnotation2() {
		doTestCompletion("CompletionAnnotation2.graphqle", Lists.newArrayList("@DataFetcher"));
	}

	public void testCompletionAnnotation3() {
		doTestCompletion("CompletionAnnotation3.graphqle", Lists.newArrayList("batched = ", "value = "));
	}

	public void testCompletionAnnotation4() {
		doTestCompletion("CompletionAnnotation4.graphqle", Lists.newArrayList("false", "true"));
	}

	public void testCompletionFieldOverride() {
		doTestCompletion("CompletionFieldOverride.graphqle", Lists.newArrayList("fieldToImpl2: Boolean"));
	}

	public void testCompletionImportFiles() {
		myFixture.addFileToProject("folder/import1.graphqle", "");
		myFixture.addFileToProject("import2.graphqle", "");
		doTestCompletion("CompletionImportFiles.graphqle", Lists.newArrayList("folder/import1", "import2", "importable"));
	}

	private void doTestCompletion(String sourceFile, List<String> expectedCompletions) {
		myFixture.configureByFile(sourceFile);
		myFixture.complete(CompletionType.BASIC);
		final List<String> completions = myFixture.getLookupElementStrings();
		assertEquals("Wrong completions", expectedCompletions, completions);
	}


	// ---- highlighting -----

	public void testErrorAnnotator() {
		myFixture.configureByFiles("ErrorAnnotator.graphqle");
		myFixture.checkHighlighting(false, false, false);
	}

}
