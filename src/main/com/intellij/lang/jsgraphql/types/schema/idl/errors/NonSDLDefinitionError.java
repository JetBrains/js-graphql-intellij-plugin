package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Definition;

import static java.lang.String.format;

@Internal
public class NonSDLDefinitionError extends BaseError {

    public NonSDLDefinitionError(Definition definition) {
        super(definition, format("The schema definition text contains a non schema definition language (SDL) element '%s'",
                definition.getClass().getSimpleName(), lineCol(definition), lineCol(definition)));
    }
}
