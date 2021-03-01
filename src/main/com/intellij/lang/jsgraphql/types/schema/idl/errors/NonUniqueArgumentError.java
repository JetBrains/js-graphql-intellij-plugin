package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.EnumValueDefinition;
import com.intellij.lang.jsgraphql.types.language.FieldDefinition;
import com.intellij.lang.jsgraphql.types.language.InputValueDefinition;
import com.intellij.lang.jsgraphql.types.language.TypeDefinition;

@Internal
public class NonUniqueArgumentError extends BaseError {

    public NonUniqueArgumentError(TypeDefinition typeDefinition, FieldDefinition fieldDefinition, String argumentName) {
        super(typeDefinition, format("The type '%s' with field '%s' %s has declared an argument with a non unique name '%s'",
                typeDefinition.getName(), fieldDefinition.getName(), lineCol(typeDefinition), argumentName));
    }

    public NonUniqueArgumentError(TypeDefinition typeDefinition, InputValueDefinition inputValueDefinition, String argumentName) {
        super(typeDefinition, format("The type '%s' with input value '%s' %s has declared an argument with a non unique name '%s'",
                typeDefinition.getName(), inputValueDefinition.getName(), lineCol(typeDefinition), argumentName));
    }

    public NonUniqueArgumentError(TypeDefinition typeDefinition, EnumValueDefinition enumValueDefinition, String argumentName) {
        super(typeDefinition, format("The '%s' type with enum value '%s' %s has declared an argument with a non unique name '%s'",
                typeDefinition.getName(), enumValueDefinition.getName(), lineCol(typeDefinition), argumentName));
    }

}
