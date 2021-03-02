package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.TypeDefinition;

import static java.lang.String.format;

@Internal
public class MissingTypeResolverError extends BaseError {

    public MissingTypeResolverError(TypeDefinition typeDefinition) {
        super(typeDefinition, format("There is no type resolver defined for interface / union '%s' type", typeDefinition.getName()));
    }

}
