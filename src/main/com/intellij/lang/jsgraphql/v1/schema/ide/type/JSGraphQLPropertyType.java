/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.schema.ide.type;

import com.intellij.psi.PsiNamedElement;

/**
 * Represents the property aspect of a field in a GraphQL schema, e.g. 'username' on type 'User'.
 */
public class JSGraphQLPropertyType {

    public final PsiNamedElement propertyElement;
    public final JSGraphQLNamedType declaringTypeElement;
    public final String propertyValueTypeName;

    public JSGraphQLPropertyType(PsiNamedElement propertyElement, JSGraphQLNamedType declaringTypeElement, String propertyValueTypeName) {
        this.propertyElement = propertyElement;
        this.declaringTypeElement = declaringTypeElement;
        this.propertyValueTypeName = propertyValueTypeName;
    }

    public String getPropertyName() {
        return propertyElement != null ? propertyElement.getName() : null;
    }

    @Override
    public String toString() {
        return declaringTypeElement.getName() + "." + getPropertyName() + ": " + propertyValueTypeName;
    }
}
