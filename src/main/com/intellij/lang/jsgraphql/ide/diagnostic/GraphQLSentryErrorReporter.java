package com.intellij.lang.jsgraphql.ide.diagnostic;

import com.intellij.diagnostic.IdeaReportingEvent;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.util.NlsActions;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.Consumer;
import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.protocol.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class GraphQLSentryErrorReporter extends ErrorReportSubmitter {

    @Override
    public @NlsActions.ActionText @NotNull String getReportActionText() {
        return GraphQLBundle.message("graphql.report.to.sentry");
    }

    @Override
    public @Nullable String getPrivacyNoticeText() {
        return "By clicking on the '" + getReportActionText() + "' button you agree that " +
            "the following will be sent along with the error message: " +
            "IDE version, " +
            "IDE name, " +
            "OS version, " +
            "plugin version, " +
            "date when the event occurred, " +
            "error log and other data that the IDE passes to the error handler.<br>" +
            "This data will only be used for debugging and bug fixing.<br>" +
            "<br>" +
            "<b>Attention!</b> Carefully study the transmitted data. Do not " +
            "click the report button if the error log or other data contains " +
            "personal information or other information that you do not want " +
            "to share.";
    }

    @Override
    public boolean submit(IdeaLoggingEvent @NotNull [] events,
                          @Nullable String additionalInfo,
                          @NotNull Component parentComponent,
                          @NotNull Consumer<? super SubmittedReportInfo> consumer) {
        GraphQLSentryClient client = GraphQLSentryClient.getInstance();

        for (IdeaLoggingEvent event : events) {
            if (!(event instanceof IdeaReportingEvent)) {
                continue;
            }
            IdeaReportingEvent reportingEvent = (IdeaReportingEvent) event;
            SentryEvent sentryEvent = new SentryEvent(reportingEvent.getData().getThrowable());

            Message message = new Message();
            String exceptionMessage = event.getThrowableText().lines().findFirst().orElse(event.getMessage());
            message.setMessage(exceptionMessage);
            sentryEvent.setMessage(message);
            sentryEvent.setLevel(SentryLevel.ERROR);

            IdeaPluginDescriptor plugin = reportingEvent.getPlugin();
            if (plugin != null) {
                sentryEvent.setRelease(plugin.getVersion());
                sentryEvent.setEnvironment(plugin.getName());
            }

            String ideaVersion = ApplicationInfo.getInstance().getBuild().asString();
            sentryEvent.setTag("IDE", ideaVersion);
            sentryEvent.setTag("OS", SystemInfo.getOsNameAndVersion());

            if (additionalInfo != null) {
                sentryEvent.setExtra("Additional info", additionalInfo);
            }

            client.captureEvent(sentryEvent);
        }

        consumer.consume(new SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE));
        return true;
    }
}
