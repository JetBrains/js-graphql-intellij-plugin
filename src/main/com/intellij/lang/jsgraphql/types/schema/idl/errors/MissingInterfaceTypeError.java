package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.TypeDefinition;
import com.intellij.lang.jsgraphql.types.language.TypeName;

@Internal
public class MissingInterfaceTypeError extends BaseError {

    public MissingInterfaceTypeError(String typeOfType, TypeDefinition typeDefinition, TypeName typeName) {
        super(typeDefinition, format("The %s type '%s' is not present when resolving type '%s' %s",
                typeOfType, typeName.getName(), typeDefinition.getName(), lineCol(typeDefinition)));
    }
}
