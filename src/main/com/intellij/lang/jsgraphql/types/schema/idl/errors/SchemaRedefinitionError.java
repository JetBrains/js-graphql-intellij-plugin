package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.SchemaDefinition;

import static java.lang.String.format;

@Internal
public class SchemaRedefinitionError extends BaseError {

    public SchemaRedefinitionError(SchemaDefinition oldEntry, SchemaDefinition newEntry) {
        super(oldEntry, format("There is already a schema defined %s.  The offending new one is here %s",
                lineCol(oldEntry), lineCol(newEntry)));
    }
}
