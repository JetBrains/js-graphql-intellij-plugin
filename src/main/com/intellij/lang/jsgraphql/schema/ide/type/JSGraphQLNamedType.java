/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema.ide.type;

import com.google.common.collect.Maps;
import com.intellij.lang.jsgraphql.psi.JSGraphQLNamedTypePsiElement;
import com.intellij.lang.jsgraphql.psi.JSGraphQLPsiElement;

import java.util.Map;

/**
 * Represents a named type in a GraphQL schema, e.g. Query, Mutation, StarShip etc.
 */
public class JSGraphQLNamedType {

    public final JSGraphQLPsiElement definitionElement;
    public final JSGraphQLNamedTypePsiElement nameElement;
    public final Map<String, JSGraphQLPropertyType> properties = Maps.newHashMap();

    public JSGraphQLNamedType(JSGraphQLPsiElement definitionElement, JSGraphQLNamedTypePsiElement nameElement) {
        this.definitionElement = definitionElement;
        this.nameElement = nameElement;
    }

    @Override
    public String toString() {
        return "Type: " + nameElement.getName();
    }
}
