/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.schema.ide.type;

import com.intellij.lang.jsgraphql.v1.psi.JSGraphQLNamedTypePsiElement;
import com.intellij.psi.PsiElement;
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

    public JSGraphQLPropertyType(PsiNamedElement propertyElement, JSGraphQLNamedType declaringTypeElement, Class<? extends PsiNamedElement> propertyClass) {
        this.propertyElement = propertyElement;
        this.declaringTypeElement = declaringTypeElement;
        PsiElement nextSibling = propertyElement.getNextSibling();
        JSGraphQLNamedTypePsiElement valueTypeElement = null;
        while(nextSibling != null) {
            if(nextSibling instanceof JSGraphQLNamedTypePsiElement) {
                // in the schema language, the last named type in a field definition is the value type of the property, e.g. type Foo { myProp(param: String): PropType }
                valueTypeElement = (JSGraphQLNamedTypePsiElement) nextSibling;
            }
            nextSibling = nextSibling.getNextSibling();
            if(nextSibling != null && propertyClass.isAssignableFrom(nextSibling.getClass())) {
                // stop before the next property
                break;
            }
        }
        propertyValueTypeName = valueTypeElement != null ? valueTypeElement.getName() : null;
    }

    public String getPropertyName() {
        return propertyElement != null ? propertyElement.getName() : null;
    }

    @Override
    public String toString() {
        return declaringTypeElement.getName() + "." + getPropertyName() + ": " + propertyValueTypeName;
    }
}
