package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.NamedNode;

@Internal
public class IllegalNameError extends BaseError {
    public IllegalNameError(NamedNode directiveDefinition) {
        super(directiveDefinition,
                String.format("'%s''%s' must not begin with '__', which is reserved by GraphQL introspection.",
                        directiveDefinition.getName(), lineCol(directiveDefinition)
                ));
    }
}
