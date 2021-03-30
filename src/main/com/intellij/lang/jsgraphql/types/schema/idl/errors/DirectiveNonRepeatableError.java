package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.language.Directive;
import org.jetbrains.annotations.NotNull;

import static java.lang.String.format;

public class DirectiveNonRepeatableError extends BaseError {
    public DirectiveNonRepeatableError(@NotNull Directive directive) {
        super(directive,
            format("The directive '%s' should be defined as repeatable if its repeated on an SDL element", directive.getName()));
    }
}
