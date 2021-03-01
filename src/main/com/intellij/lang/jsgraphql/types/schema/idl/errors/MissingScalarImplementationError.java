package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;

import static java.lang.String.format;

@Internal
public class MissingScalarImplementationError extends BaseError {

    public MissingScalarImplementationError(String scalarName) {
        super(null, format("There is no scalar implementation for the named  '%s' scalar type", scalarName));
    }

}
