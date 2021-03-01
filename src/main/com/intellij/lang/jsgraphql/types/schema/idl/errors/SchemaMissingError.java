package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;

@Internal
public class SchemaMissingError extends BaseError {

    public SchemaMissingError() {
        super(null, "There is no top level schema object defined");
    }
}
