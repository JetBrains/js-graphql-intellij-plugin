package com.intellij.lang.jsgraphql.types;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * Marks fields, methods etc as more visible than actually needed for testing purposes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {CONSTRUCTOR, METHOD, FIELD})
@Internal
public @interface VisibleForTesting {
}
