package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Directive;
import com.intellij.lang.jsgraphql.types.language.TypeDefinition;

@Internal
public class TypeExtensionDirectiveRedefinitionError extends BaseError {

    public TypeExtensionDirectiveRedefinitionError(TypeDefinition typeExtensionDefinition, Directive directive) {
        super(typeExtensionDefinition,
                format("The extension '%s' type %s has redefined the directive called '%s'",
                        typeExtensionDefinition.getName(), BaseError.lineCol(typeExtensionDefinition), directive.getName()
                ));
    }
}
