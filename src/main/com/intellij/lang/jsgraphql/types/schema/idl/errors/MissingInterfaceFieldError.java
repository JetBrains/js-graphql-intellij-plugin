package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.FieldDefinition;
import com.intellij.lang.jsgraphql.types.language.ImplementingTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.InterfaceTypeDefinition;

@Internal
public class MissingInterfaceFieldError extends BaseError {
    public MissingInterfaceFieldError(String typeOfType, ImplementingTypeDefinition objectType, InterfaceTypeDefinition interfaceTypeDef, FieldDefinition interfaceFieldDef) {
        super(objectType, format("The %s type '%s' %s does not have a field '%s' required via interface '%s' %s",
                typeOfType, objectType.getName(), lineCol(objectType), interfaceFieldDef.getName(), interfaceTypeDef.getName(), lineCol(interfaceTypeDef)));
    }
}
