package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.AbstractNode;
import com.intellij.lang.jsgraphql.types.language.FieldDefinition;
import com.intellij.lang.jsgraphql.types.language.InputValueDefinition;
import com.intellij.lang.jsgraphql.types.language.TypeDefinition;

@Internal
public class TypeExtensionFieldRedefinitionError extends BaseError {

    public TypeExtensionFieldRedefinitionError(TypeDefinition typeDefinition, FieldDefinition fieldDefinition) {
        super(typeDefinition,
                formatMessage(typeDefinition, fieldDefinition.getName(), fieldDefinition));
    }

    public TypeExtensionFieldRedefinitionError(TypeDefinition typeDefinition, InputValueDefinition fieldDefinition) {
        super(typeDefinition,
                formatMessage(typeDefinition, fieldDefinition.getName(), fieldDefinition));
    }

    private static String formatMessage(TypeDefinition typeDefinition, String fieldName, AbstractNode<?> fieldDefinition) {
        return format("'%s' extension type %s tried to redefine field '%s' %s",
                typeDefinition.getName(), BaseError.lineCol(typeDefinition), fieldName, BaseError.lineCol(fieldDefinition)
        );
    }
}
