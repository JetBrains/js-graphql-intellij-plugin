package com.intellij.lang.jsgraphql.types.util;

import com.intellij.lang.jsgraphql.types.Internal;

@Internal
public class TraverserResult {

    private final Object accumulatedResult;

    public TraverserResult(Object accumulatedResult) {
        this.accumulatedResult = accumulatedResult;
    }

    public Object getAccumulatedResult() {
        return accumulatedResult;
    }

}
