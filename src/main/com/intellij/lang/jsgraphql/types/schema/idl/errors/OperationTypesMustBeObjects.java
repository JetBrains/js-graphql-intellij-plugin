package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.OperationTypeDefinition;

@Internal
public class OperationTypesMustBeObjects extends BaseError {

    public OperationTypesMustBeObjects(OperationTypeDefinition op) {
        super(op, format("The operation type '%s' MUST have a object type as its definition %s",
                op.getName(), lineCol(op)));
    }
}
