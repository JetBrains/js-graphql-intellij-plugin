/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.intentions;

import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class JSGraphQLEndpointCreateUnionTypeIntention extends JSGraphQLEndpointCreateDefinitionIntention {
    @NotNull
    @Override
    public String getText() {
        return "Create 'union'";
    }

    @Override
    protected @NotNull IElementType getSupportedDefinitionType() {
        return JSGraphQLEndpointTokenTypes.UNION;
    }
}
