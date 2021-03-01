package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Type;
import com.intellij.lang.jsgraphql.types.language.TypeDefinition;

@Internal
public class NotAnInputTypeError extends BaseError {

    public NotAnInputTypeError(Type rawType, TypeDefinition typeDefinition) {
        super(rawType, format("The type '%s' %s is not an input type, but was used as an input type %s", typeDefinition.getName(), lineCol(typeDefinition), lineCol(rawType)));
    }
}
