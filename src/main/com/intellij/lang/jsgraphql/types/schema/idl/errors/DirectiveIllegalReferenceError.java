package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.DirectiveDefinition;
import com.intellij.lang.jsgraphql.types.language.NamedNode;

@Internal
public class DirectiveIllegalReferenceError extends BaseError {
    public DirectiveIllegalReferenceError(DirectiveDefinition directive, NamedNode location) {
        super(directive,
                String.format("'%s' must not reference itself on '%s''%s'",
                        directive.getName(), location.getName(), lineCol(location)
                ));
    }
}
