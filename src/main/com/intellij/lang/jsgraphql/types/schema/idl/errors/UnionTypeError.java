package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Node;

@Internal
public class UnionTypeError extends BaseError {
    public UnionTypeError(Node node, String msg) {
        super(node, msg);
    }
}
