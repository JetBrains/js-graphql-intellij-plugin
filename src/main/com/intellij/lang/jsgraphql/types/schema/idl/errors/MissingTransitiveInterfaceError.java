package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.ImplementingTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.InterfaceTypeDefinition;

@Internal
public class MissingTransitiveInterfaceError extends BaseError {
    public MissingTransitiveInterfaceError(String typeOfType, ImplementingTypeDefinition typeDefinition, InterfaceTypeDefinition implementedInterface, InterfaceTypeDefinition missingInterface) {
        super(typeDefinition, format("The %s type '%s' %s must implement '%s' %s because it is implemented by '%s' %s",
                typeOfType, typeDefinition.getName(), lineCol(typeDefinition), missingInterface.getName(), lineCol(missingInterface), implementedInterface.getName(), lineCol(implementedInterface)));
    }
}
