package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.EnumValueDefinition;
import com.intellij.lang.jsgraphql.types.language.TypeDefinition;

import static java.lang.String.format;

@Internal
public class TypeExtensionEnumValueRedefinitionError extends BaseError {

    public TypeExtensionEnumValueRedefinitionError(TypeDefinition typeDefinition, EnumValueDefinition enumValueDefinition) {
        super(typeDefinition,
                format("'%s' extension type %s tried to redefine enum value '%s' %s",
                        typeDefinition.getName(), BaseError.lineCol(typeDefinition), enumValueDefinition.getName(), BaseError.lineCol(enumValueDefinition)
                ));
    }
}
