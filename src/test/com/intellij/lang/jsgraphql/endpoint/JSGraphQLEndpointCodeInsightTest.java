/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.lang.jsgraphql.ide.configuration.JSGraphQLConfigurationProvider;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;


public class JSGraphQLEndpointCodeInsightTest extends LightCodeInsightFixtureTestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		myFixture.addFileToProject("main.graphqle", "");
		myFixture.addFileToProject(JSGraphQLConfigurationProvider.GRAPHQL_CONFIG_JSON, "{\n" +
				"    \"schema\": {\n" +
				"        \"endpoint\": {\n" +
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
	}

	@Override
	protected String getTestDataPath() {
		return "test-resources/testData/endpoint";
	}


	// ---- formatter ----

	@Test
	public void testFormatter() {
		myFixture.configureByFiles("FormatterTestData.graphqle");
		CodeStyleSettingsManager.getSettings(getProject()).KEEP_BLANK_LINES_IN_CODE = 2;
		new WriteCommandAction.Simple(getProject()) {
			@Override
			protected void run() throws Throwable {
				CodeStyleManager.getInstance(getProject()).reformat(myFixture.getFile());
			}
		}.execute();
		myFixture.checkResultByFile("FormatterExpectedResult.graphqle");
	}


	// ---- completion ----

	@Test
	public void testCompletionImplementsFirstInterface() {
		doTestCompletion("CompletionImplementsFirstInterface.graphqle", Lists.newArrayList("KnownInterface1", "KnownInterface2"));
	}

	@Test
	public void testCompletionImplementsSecondInterface() {
		doTestCompletion("CompletionImplementsSecondInterface.graphqle", Lists.newArrayList("KnownInterface2"));
	}

	@Test
	public void testCompletionOperation() {
		doTestCompletion("CompletionOperation.graphqle", Lists.newArrayList("enum", "import", "input", "interface", "schema", "type", "union"));
	}

	@Test
	public void testCompletionImplementsKeyword1() {
		doTestCompletion("CompletionImplementsKeyword1.graphqle", Lists.newArrayList("implements"));
	}

	@Test
	public void testCompletionImplementsKeyword2() {
		doTestCompletion("CompletionImplementsKeyword2.graphqle", Lists.newArrayList("implements"));
	}

	@Test
	public void testCompletionFieldType() {
		doTestCompletion("CompletionFieldType.graphqle", Lists.newArrayList("Boolean", "Float", "Foo", "ID", "Int", "KnownInterface", "KnownType", "MyEnum", "MyUnion", "String"));
	}

	@Test
	public void testCompletionInputFieldType() {
		doTestCompletion("CompletionInputFieldType.graphqle", Lists.newArrayList("Boolean", "Float", "ID", "Int", "MyEnum", "MyInput1", "String"));
	}

	@Test
	public void testCompletionArgumentType() {
		doTestCompletion("CompletionArgumentType.graphqle", Lists.newArrayList("Boolean", "Float", "ID", "Int", "MyCompletionInputABC", "MyEnum", "String"));
	}

	@Test
	public void testCompletionSecondArgumentType() {
		doTestCompletion("CompletionSecondArgumentType.graphqle", Lists.newArrayList("Boolean", "Float", "ID", "Int", "MyCompletionInputABC", "MyEnum", "String"));
	}

	@Test
	public void testCompletionAnnotation1() {
		doTestCompletion("CompletionAnnotation1.graphqle", Lists.newArrayList("@DataFetcher"));
	}

	@Test
	public void testCompletionAnnotation2() {
		doTestCompletion("CompletionAnnotation2.graphqle", Lists.newArrayList("@DataFetcher"));
	}

	@Test
	public void testCompletionAnnotation3() {
		doTestCompletion("CompletionAnnotation3.graphqle", Lists.newArrayList("batched = ", "value = "));
	}

	@Test
	public void testCompletionAnnotation4() {
		doTestCompletion("CompletionAnnotation4.graphqle", Lists.newArrayList("false", "true"));
	}

	@Test
	public void testCompletionFieldOverride() {
		doTestCompletion("CompletionFieldOverride.graphqle", Lists.newArrayList("fieldToImpl2: Boolean"));
	}

	@Test
	public void testCompletionImportFiles() {
		myFixture.addFileToProject("folder/import1.graphqle", "");
		myFixture.addFileToProject("import2.graphqle", "");
		doTestCompletion("CompletionImportFiles.graphqle", Lists.newArrayList("folder/import1", "import2"));
	}

	private void doTestCompletion(String sourceFile, List<String> expectedCompletions) {
		myFixture.configureByFiles(sourceFile);
		myFixture.complete(CompletionType.BASIC, 1);
		final List<String> completions = myFixture.getLookupElementStrings();
		assertEquals("Wrong completions", expectedCompletions, completions);
	}


}
