package com.intellij.lang.jsgraphql.ide.diagnostic;

import io.sentry.Sentry;
import io.sentry.SentryEvent;
import org.jetbrains.annotations.NotNull;

public final class GraphQLSentryClient {
    private static class Lazy {
        private static final GraphQLSentryClient ourClient = new GraphQLSentryClient();
    }

    public static GraphQLSentryClient getInstance() {
        return Lazy.ourClient;
    }

    private GraphQLSentryClient() {
        Sentry.init(options -> options.setDsn("https://56294687de4a4eda977a0e2be35a785c@o1086110.ingest.sentry.io/6097822"));
    }

    public void captureEvent(@NotNull SentryEvent event) {
        Sentry.captureEvent(event);
    }
}
