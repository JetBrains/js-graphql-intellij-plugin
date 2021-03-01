package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Node;

import static java.lang.String.format;

@Internal
public class DirectiveMissingNonNullArgumentError extends BaseError {

    public DirectiveMissingNonNullArgumentError(Node element, String elementName, String directiveName, String argumentName) {
        super(element,
                format("'%s' %s failed to provide a value for the non null argument '%s' on directive '%s'",
                        elementName, BaseError.lineCol(element), argumentName, directiveName
                ));
    }
}
