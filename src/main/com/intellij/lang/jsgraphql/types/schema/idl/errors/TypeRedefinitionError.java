package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.TypeDefinition;

@Internal
public class TypeRedefinitionError extends BaseError {

    public TypeRedefinitionError(TypeDefinition newEntry, TypeDefinition oldEntry) {
        super(oldEntry,
                format("'%s' type %s tried to redefine existing '%s' type %s",
                        newEntry.getName(), BaseError.lineCol(newEntry), oldEntry.getName(), BaseError.lineCol(oldEntry)
                ));
    }
}
