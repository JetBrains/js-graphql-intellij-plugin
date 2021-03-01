package com.intellij.lang.jsgraphql.types.execution.reactive;

import com.intellij.lang.jsgraphql.types.PublicApi;
import org.reactivestreams.Subscription;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

/**
 * A simple subscription that delegates to another
 */
@PublicApi
public class DelegatingSubscription implements Subscription {
    private final Subscription upstreamSubscription;

    public DelegatingSubscription(Subscription upstreamSubscription) {
        this.upstreamSubscription = assertNotNull(upstreamSubscription);
    }

    @Override
    public void request(long n) {
        upstreamSubscription.request(n);
    }

    @Override
    public void cancel() {
        upstreamSubscription.cancel();
    }
}
