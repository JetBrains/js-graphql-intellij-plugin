package com.intellij.lang.jsgraphql.types;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * This represents code that the graphql-java project considers internal code that MAY not be stable within
 * major releases.
 *
 * In general unnecessary changes will be avoided but you should not depend on internal classes being stable
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {CONSTRUCTOR, METHOD, TYPE, FIELD})
public @interface Internal {
}
