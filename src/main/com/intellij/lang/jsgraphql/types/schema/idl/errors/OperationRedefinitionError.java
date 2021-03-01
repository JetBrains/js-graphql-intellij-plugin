package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.OperationTypeDefinition;

@Internal
public class OperationRedefinitionError extends BaseError {

    public OperationRedefinitionError(OperationTypeDefinition oldEntry, OperationTypeDefinition newEntry) {
        super(oldEntry, format("There is already an operation '%s' defined %s.  The offending new one is here %s",
                oldEntry.getName(), lineCol(oldEntry), lineCol(newEntry)));
    }
}
