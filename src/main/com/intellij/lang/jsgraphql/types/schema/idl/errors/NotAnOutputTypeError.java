package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Type;
import com.intellij.lang.jsgraphql.types.language.TypeDefinition;

import static java.lang.String.format;

@Internal
public class NotAnOutputTypeError extends BaseError {

    public NotAnOutputTypeError(Type rawType, TypeDefinition typeDefinition) {
        super(rawType, format("The type '%s' %s is not an output type, but was used to declare the output type of a field %s", typeDefinition.getName(), lineCol(typeDefinition), lineCol(rawType)));
    }
}
