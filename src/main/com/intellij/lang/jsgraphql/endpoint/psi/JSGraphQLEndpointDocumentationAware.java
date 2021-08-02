/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.psi;

/**
 * Implemented by PSI elements that can provide their own documentation
 */
public interface JSGraphQLEndpointDocumentationAware {

	/**
	 * Gets the code that represents the declaration of the element, e..g "type Foo implements Bar".
	 */
	String getDeclaration();

	/**
	 * Gets any textual documentation associated with the element
	 */
	String getDocumentation(boolean fullDocumentation);
}
