package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.ImplementingTypeDefinition;

import static java.lang.String.format;

@Internal
public class InterfaceImplementingItselfError extends BaseError {
    public InterfaceImplementingItselfError(String typeOfType, ImplementingTypeDefinition typeDefinition) {
        super(typeDefinition, format("The %s type '%s' %s cannot implement itself",
                typeOfType, typeDefinition.getName(), lineCol(typeDefinition)));
    }
}
