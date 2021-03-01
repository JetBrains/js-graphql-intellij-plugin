package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.EnumValueDefinition;
import com.intellij.lang.jsgraphql.types.language.FieldDefinition;
import com.intellij.lang.jsgraphql.types.language.InputValueDefinition;
import com.intellij.lang.jsgraphql.types.language.TypeDefinition;

@Internal
public class NonUniqueDirectiveError extends BaseError {

    public NonUniqueDirectiveError(TypeDefinition typeDefinition, FieldDefinition fieldDefinition, String directiveName) {
        super(typeDefinition, format("The type '%s' with field '%s' %s has declared a directive with a non unique name '%s'",
                typeDefinition.getName(), fieldDefinition.getName(), lineCol(typeDefinition), directiveName));
    }

    public NonUniqueDirectiveError(TypeDefinition typeDefinition, InputValueDefinition inputValueDefinition, String directiveName) {
        super(typeDefinition, format("The type '%s' with input value '%s' %s has declared a directive with a non unique name '%s'",
                typeDefinition.getName(), inputValueDefinition.getName(), lineCol(typeDefinition), directiveName));
    }

    public NonUniqueDirectiveError(TypeDefinition typeDefinition, EnumValueDefinition enumValueDefinition, String directiveName) {
        super(typeDefinition, format("The '%s' type with enum value '%s' %s has declared a directive with a non unique name '%s'",
                typeDefinition.getName(), enumValueDefinition.getName(), lineCol(typeDefinition), directiveName));
    }

}
