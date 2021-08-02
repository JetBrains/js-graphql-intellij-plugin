/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1;

import java.util.Set;

import com.google.common.collect.Sets;

public interface JSGraphQLScalars {

	String STRING = "String";
	String BOOLEAN = "Boolean";
	String INT = "Int";
	String FLOAT = "Float";
	String ID = "ID";

	Set<String> SCALAR_TYPES = Sets.newHashSet(STRING, BOOLEAN, INT, FLOAT, ID);

}
