/*
    The MIT License (MIT)

    Copyright (c) 2015 Andreas Marek and Contributors

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
    (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
    publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
    so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
    OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
    CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.intellij.lang.jsgraphql.types.execution.instrumentation.dataloader;

import com.intellij.lang.jsgraphql.types.PublicApi;

/**
 * The options that control the operation of {@link graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation}
 */
@PublicApi
public class DataLoaderDispatcherInstrumentationOptions {

    private final boolean includeStatistics;

    private DataLoaderDispatcherInstrumentationOptions(boolean includeStatistics) {
        this.includeStatistics = includeStatistics;
    }

    public static DataLoaderDispatcherInstrumentationOptions newOptions() {
        return new DataLoaderDispatcherInstrumentationOptions(false);
    }

    /**
     * This will toggle the ability to include java-dataloader statistics into the extensions
     * output of your query
     *
     * @param flag the switch to follow
     *
     * @return a new options object
     */
    public DataLoaderDispatcherInstrumentationOptions includeStatistics(boolean flag) {
        return new DataLoaderDispatcherInstrumentationOptions(flag);
    }


    public boolean isIncludeStatistics() {
        return includeStatistics;
    }

}
