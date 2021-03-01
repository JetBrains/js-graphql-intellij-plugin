package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.ImplementingTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.InterfaceTypeDefinition;

@Internal
public class InterfaceImplementedMoreThanOnceError extends BaseError {
    public InterfaceImplementedMoreThanOnceError(String typeOfType, ImplementingTypeDefinition typeDefinition, InterfaceTypeDefinition implementedInterface) {
        super(typeDefinition, format("The %s type '%s' %s can only implement '%s' %s once.",
                typeOfType, typeDefinition.getName(), lineCol(typeDefinition), implementedInterface.getName(), lineCol(implementedInterface)));
    }
}
