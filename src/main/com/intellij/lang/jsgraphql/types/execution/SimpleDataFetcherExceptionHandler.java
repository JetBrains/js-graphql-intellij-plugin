package com.intellij.lang.jsgraphql.types.execution;

import com.intellij.lang.jsgraphql.types.ExceptionWhileDataFetching;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;

/**
 * The standard handling of data fetcher error involves placing a {@link ExceptionWhileDataFetching} error
 * into the error collection
 */
@PublicApi
public class SimpleDataFetcherExceptionHandler implements DataFetcherExceptionHandler {


    @Override
    public DataFetcherExceptionHandlerResult onException(DataFetcherExceptionHandlerParameters handlerParameters) {
        Throwable exception = handlerParameters.getException();
        SourceLocation sourceLocation = handlerParameters.getSourceLocation();
        ResultPath path = handlerParameters.getPath();

        ExceptionWhileDataFetching error = new ExceptionWhileDataFetching(path, exception, sourceLocation);

        return DataFetcherExceptionHandlerResult.newResult().error(error).build();
    }
}
