package com.intellij.lang.jsgraphql.types.execution.batched;

import com.intellij.lang.jsgraphql.types.GraphQLException;
import com.intellij.lang.jsgraphql.types.PublicApi;


@Deprecated
@PublicApi
public class BatchAssertionFailed extends GraphQLException {
    public BatchAssertionFailed() {
        super();
    }

    public BatchAssertionFailed(String message) {
        super(message);
    }

    public BatchAssertionFailed(String message, Throwable cause) {
        super(message, cause);
    }

    public BatchAssertionFailed(Throwable cause) {
        super(cause);
    }
}
