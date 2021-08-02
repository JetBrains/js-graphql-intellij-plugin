/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.schema.ide.type;

import com.google.common.collect.Maps;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;

import java.util.Map;

/**
 * Represents a named type in a GraphQL schema, e.g. Query, Mutation, StarShip etc.
 */
public class JSGraphQLNamedType {

    /** PSI element for the type definition, e.g. 'type Foo {}' */
    public final PsiElement definitionElement;

    /** PSI element representing the name of a type definition, e.g. 'Foo' in 'type Foo {}' */
    public final PsiNamedElement nameElement;

    public final Map<String, JSGraphQLPropertyType> properties = Maps.newHashMap();

    public JSGraphQLNamedType(PsiElement definitionElement, PsiNamedElement nameElement) {
        this.definitionElement = definitionElement;
        this.nameElement = nameElement;
    }

    public String getName() {
        return nameElement.getName();
    }

    @Override
    public String toString() {
        return "Type: " + getName();
    }
}
