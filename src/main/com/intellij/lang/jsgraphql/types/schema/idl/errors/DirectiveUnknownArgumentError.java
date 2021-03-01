package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Node;

import static java.lang.String.format;

@Internal
public class DirectiveUnknownArgumentError extends BaseError {

    public DirectiveUnknownArgumentError(Node element, String elementName, String directiveName, String argumentName) {
        super(element,
                format("'%s' %s use an unknown argument '%s' on directive '%s'",
                        elementName, BaseError.lineCol(element), argumentName, directiveName
                ));
    }
}
