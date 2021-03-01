package com.intellij.lang.jsgraphql.types.schema.transform;


import com.intellij.lang.jsgraphql.types.PublicSpi;

/**
 * Predicate used during a {@link FieldVisibilitySchemaTransformation} to test whether a field should be visible.
 */
@PublicSpi
@FunctionalInterface
public interface VisibleFieldPredicate {

    /**
     * Test whether a field should be visible. Provided as a more descriptive "test" method that describes exactly
     * what a positive result of "test" should mean.
     *
     * @param environment the context of the field
     * @return true if visible
     */
    boolean isVisible(VisibleFieldPredicateEnvironment environment);
}
