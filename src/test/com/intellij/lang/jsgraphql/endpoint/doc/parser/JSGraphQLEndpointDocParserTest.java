/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.doc.parser;

import org.junit.Test;

import com.intellij.lang.jsgraphql.endpoint.doc.JSGraphQLEndpointDocParserDefinition;
import com.intellij.testFramework.ParsingTestCase;
import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.runner.RunWith;

@RunWith(JUnit38ClassRunner.class) // TODO: drop the annotation when issue with Gradle test scanning go away
public class JSGraphQLEndpointDocParserTest extends ParsingTestCase {

	public JSGraphQLEndpointDocParserTest() {
		super("", "graphqld", new JSGraphQLEndpointDocParserDefinition());
	}

	@Test
	public void testParsingTestData() {
		doTest(true);
	}

	@Override
	protected String getTestDataPath() {
		return "test-resources/testData/endpoint/doc";
	}

	@Override
	protected boolean skipSpaces() {
		return false;
	}

	@Override
	protected boolean includeRanges() {
		return true;
	}
}
