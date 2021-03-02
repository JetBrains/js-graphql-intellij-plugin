package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.TypeDefinition;

import static java.lang.String.format;

@Internal
public class TypeExtensionMissingBaseTypeError extends BaseError {

    public TypeExtensionMissingBaseTypeError(TypeDefinition typeExtensionDefinition) {
        super(typeExtensionDefinition,
                format("The extension '%s' type %s is missing its base underlying type",
                        typeExtensionDefinition.getName(), BaseError.lineCol(typeExtensionDefinition)
                ));
    }
}
