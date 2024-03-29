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
package com.intellij.lang.jsgraphql.types.execution.reactive;

import com.intellij.lang.jsgraphql.types.Internal;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.Assert.assertNotNullWithNPE;

/**
 * A Publisher of things that are buffered and handles a single subscriber at a time.
 * <p>
 * Rule #1 of reactive streams is don't write your own implementation.  However rule #1 of graphql-java is that we have
 * no unnecessary dependencies and force users into a code corner.  So we chose to have a very simple implementation (single subscriber)
 * implementation that allows a stream of results to be streamed out.  People can wrap this is a more complete implementation
 * if they so choose.
 * <p>
 * Inspired by Public Domain CC0 code at
 * https://github.com/jroper/reactive-streams-servlet/tree/master/reactive-streams-servlet/src/main/java/org/reactivestreams/servlet
 *
 * @param <T> the things to publish
 */
@Internal
public class SingleSubscriberPublisher<T> implements Publisher<T> {
  private final Deque<T> dataQ = new ConcurrentLinkedDeque<>();
  private final NonBlockingMutexExecutor mutex = new NonBlockingMutexExecutor();
  private final OnSubscriptionCallback subscriptionCallback;

  private Subscriber<? super T> subscriber;
  private Throwable pendingThrowable = null;
  private boolean running = true;
  private boolean noMoreData = false;
  private long demand = 0;

  /**
   * Constructs a publisher with no callback when subscribed
   */
  public SingleSubscriberPublisher() {
    this(() -> {
    });
  }

  /**
   * The producing code can provide a callback to know when the subscriber attaches
   *
   * @param subscriptionCallback the callback when some ones
   */
  public SingleSubscriberPublisher(OnSubscriptionCallback subscriptionCallback) {
    this.subscriptionCallback = assertNotNull(subscriptionCallback);
  }


  /**
   * Called from the producing code to offer data up ready for a subscriber to read it
   *
   * @param data the data to offer
   */
  public void offer(T data) {
    mutex.execute(() -> {
      dataQ.offer(data);
      maybeReadInMutex();
    });
  }

  /**
   * Called by the producing code to say there is no more data to offer and the stream
   * is complete
   */
  public void noMoreData() {
    mutex.execute(() -> {
      noMoreData = true;
      maybeReadInMutex();
    });
  }

  public void offerError(Throwable t) {
    mutex.execute(() -> {
      pendingThrowable = t;
      maybeReadInMutex();
    });
  }

  private void handleError(final Throwable t) {
    if (running) {
      running = false;
      subscriber.onError(t);
      subscriber = null;
    }
  }

  private void handleOnComplete() {
    if (running) {
      running = false;
      subscriber.onComplete();
      subscriber = null;
    }
  }

  @Override
  public void subscribe(Subscriber<? super T> subscriber) {
    assertNotNullWithNPE(subscriber, () -> "Subscriber passed to subscribe must not be null");
    mutex.execute(() -> {
      if (this.subscriber == null) {
        this.subscriber = subscriber;
        subscriptionCallback.onSubscription();
        subscriber.onSubscribe(new SimpleSubscription());
        if (pendingThrowable != null) {
          handleError(pendingThrowable);
        }
      }
      else if (this.subscriber.equals(subscriber)) {
        handleError(new IllegalStateException("Attempted to subscribe this Subscriber more than once for the same Publisher"));
      }
      else {
        // spec says handle this in tis manner
        subscriber.onSubscribe(new Subscription() {
          @Override
          public void request(long n) {
          }

          @Override
          public void cancel() {
          }
        });
        subscriber.onError(new IllegalStateException("This publisher only supports one subscriber"));
      }
    });
  }

  private void maybeReadInMutex() {
    while (running && demand > 0) {
      if (pendingThrowable != null) {
        handleError(pendingThrowable);
        return;
      }
      if (dataQ.isEmpty() && noMoreData) {
        handleOnComplete();
        return;
      }
      if (!dataQ.isEmpty()) {
        T data = dataQ.removeFirst();
        subscriber.onNext(data);
        demand--;
      }
      else {
        return;
      }
    }
  }

  private class SimpleSubscription implements Subscription {

    @Override
    public void request(long n) {
      mutex.execute(() -> {
        if (running) {
          if (n <= 0) {
            handleError(new IllegalArgumentException("Reactive streams 3.9 spec violation: non-positive subscription request"));
          }
          else {
            final long old = demand;
            if (old < Long.MAX_VALUE) {
              demand = ((old + n) < 0) ? Long.MAX_VALUE : (old + n); // Overflow protection
            }
            if (old == 0) {
              maybeReadInMutex();
            }
          }
        }
      });
    }

    @Override
    public void cancel() {
      mutex.execute(() -> {
        if (running) {
          subscriber = null;
          running = false;
        }
      });
    }
  }

  /**
   * This is called when a subscription is made to the publisher
   */
  public interface OnSubscriptionCallback {
    /**
     * The call back when some one has subscribed.  Its perhaps a good time to start
     * producing data
     */
    void onSubscription();
  }
}
