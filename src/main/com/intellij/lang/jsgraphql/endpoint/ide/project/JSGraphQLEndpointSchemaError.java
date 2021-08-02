/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.project;

import com.intellij.psi.PsiElement;
import com.intellij.lang.jsgraphql.types.GraphQLException;

/**
 * Represents a schema error in the Endpoint language that prevents a complete and valid schema to be created.
 */
public class JSGraphQLEndpointSchemaError extends GraphQLException {


    private final PsiElement sourceElement;

    JSGraphQLEndpointSchemaError(String message, PsiElement sourceElement) {
        super(message);
        this.sourceElement = sourceElement;
    }

    public PsiElement getSourceElement() {
        return sourceElement;
    }
}
