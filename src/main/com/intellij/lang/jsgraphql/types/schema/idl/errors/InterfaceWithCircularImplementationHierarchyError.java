package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.ImplementingTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.InterfaceTypeDefinition;

@Internal
public class InterfaceWithCircularImplementationHierarchyError extends BaseError {
    public InterfaceWithCircularImplementationHierarchyError(String typeOfType, ImplementingTypeDefinition typeDefinition, InterfaceTypeDefinition implementedInterface) {
        super(typeDefinition, format("The %s type '%s' %s cannot implement '%s' %s as this would result in a circular reference",
                typeOfType, typeDefinition.getName(), lineCol(typeDefinition),
                implementedInterface.getName(), lineCol(implementedInterface)
        ));
    }
}
