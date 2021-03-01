package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.FieldDefinition;
import com.intellij.lang.jsgraphql.types.language.ImplementingTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.InterfaceTypeDefinition;

@Internal
public class InterfaceFieldRedefinitionError extends BaseError {
    public InterfaceFieldRedefinitionError(String typeOfType, ImplementingTypeDefinition typeDefinition, InterfaceTypeDefinition interfaceTypeDef, FieldDefinition objectFieldDef, String objectFieldType, String interfaceFieldType) {
        super(typeDefinition, format("The %s type '%s' %s has tried to redefine field '%s' defined via interface '%s' %s from '%s' to '%s'",
                typeOfType, typeDefinition.getName(), lineCol(typeDefinition), objectFieldDef.getName(), interfaceTypeDef.getName(), lineCol(interfaceTypeDef), interfaceFieldType, objectFieldType));
    }
}
