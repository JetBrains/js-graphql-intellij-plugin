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

import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.InstrumentationState;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * A base class that keeps track of whether aggressive batching can be used
 */
@PublicApi
public class DataLoaderDispatcherInstrumentationState implements InstrumentationState {

  @Internal
  public static final DataLoaderRegistry EMPTY_DATALOADER_REGISTRY = new DataLoaderRegistry() {

    private static final String ERROR_MESSAGE = "You MUST set in your own DataLoaderRegistry to use data loader";

    @Override
    public DataLoaderRegistry register(String key, DataLoader<?, ?> dataLoader) {
      return Assert.assertShouldNeverHappen(ERROR_MESSAGE);
    }

    @Override
    public <K, V> DataLoader<K, V> computeIfAbsent(final String key,
                                                   final Function<String, DataLoader<?, ?>> mappingFunction) {
      return Assert.assertShouldNeverHappen(ERROR_MESSAGE);
    }

    @Override
    public DataLoaderRegistry unregister(String key) {
      return Assert.assertShouldNeverHappen(ERROR_MESSAGE);
    }
  };

  private final FieldLevelTrackingApproach approach;
  private final AtomicReference<DataLoaderRegistry> dataLoaderRegistry;
  private final InstrumentationState state;
  private volatile boolean aggressivelyBatching = true;
  private volatile boolean hasNoDataLoaders;

  public DataLoaderDispatcherInstrumentationState(DataLoaderRegistry dataLoaderRegistry) {
    this.dataLoaderRegistry = new AtomicReference<>(dataLoaderRegistry);
    this.approach = new FieldLevelTrackingApproach(this::getDataLoaderRegistry);
    this.state = approach.createState();
    hasNoDataLoaders = checkForNoDataLoader(dataLoaderRegistry);
  }

  private boolean checkForNoDataLoader(DataLoaderRegistry dataLoaderRegistry) {
    //
    // if they have never set a dataloader into the execution input then we can optimize
    // away the tracking code
    //
    return dataLoaderRegistry == EMPTY_DATALOADER_REGISTRY;
  }

  boolean isAggressivelyBatching() {
    return aggressivelyBatching;
  }

  void setAggressivelyBatching(boolean aggressivelyBatching) {
    this.aggressivelyBatching = aggressivelyBatching;
  }

  FieldLevelTrackingApproach getApproach() {
    return approach;
  }

  DataLoaderRegistry getDataLoaderRegistry() {
    return dataLoaderRegistry.get();
  }

  void setDataLoaderRegistry(DataLoaderRegistry newRegistry) {
    dataLoaderRegistry.set(newRegistry);
    hasNoDataLoaders = checkForNoDataLoader(newRegistry);
  }

  boolean hasNoDataLoaders() {
    return hasNoDataLoaders;
  }

  InstrumentationState getState() {
    return state;
  }
}
