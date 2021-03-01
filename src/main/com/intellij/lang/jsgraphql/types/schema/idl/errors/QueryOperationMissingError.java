package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;

@Internal
public class QueryOperationMissingError extends BaseError {

    public QueryOperationMissingError() {
        super(null, "A schema MUST have a 'query' operation defined");
    }
}
