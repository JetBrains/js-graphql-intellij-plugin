package com.intellij.lang.jsgraphql.types.schema.diff.reporting;

import com.intellij.lang.jsgraphql.types.PublicSpi;
import com.intellij.lang.jsgraphql.types.schema.diff.DiffEvent;

/**
 * This is called with each different encountered (including info ones) by a {@link com.intellij.lang.jsgraphql.types.schema.diff.SchemaDiff} operation
 */
@PublicSpi
public interface DifferenceReporter {

    /**
     * Called to report a difference
     *
     * @param differenceEvent the event describing the difference
     */
    void report(DiffEvent differenceEvent);

    /**
     * Called when the difference operation if finished
     */
    void onEnd();
}
