/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.schema.ide.type;

import com.intellij.psi.PsiElement;

/**
 * Provides a scoped registry to look up known type definitions based on their name
 */
public interface JSGraphQLNamedTypeRegistry {

    /**
     * Gets the name type, if any, that has the specified typeName
     * @param typeName the name of the type to lookup
     * @param scopedElement the PSI element from which the request is made, serving as scoping for schema discovery
     */
    JSGraphQLNamedType getNamedType(String typeName, PsiElement scopedElement);

}
