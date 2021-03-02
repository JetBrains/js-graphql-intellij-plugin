package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.DirectiveDefinition;

import static java.lang.String.format;

@Internal
public class DirectiveRedefinitionError extends BaseError {

    public DirectiveRedefinitionError(DirectiveDefinition newEntry, DirectiveDefinition oldEntry) {
        super(oldEntry,
                format("'%s' type %s tried to redefine existing directive '%s' type %s",
                        newEntry.getName(), BaseError.lineCol(newEntry), oldEntry.getName(), BaseError.lineCol(oldEntry)
                ));
    }
}
