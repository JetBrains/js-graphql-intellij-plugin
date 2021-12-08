package com.intellij.lang.jsgraphql.ide.diagnostic;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.NlsActions;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class GraphQLGithubErrorReporter extends ErrorReportSubmitter {

    private static final String URL = "https://github.com/jimkyndemeyer/js-graphql-intellij-plugin/issues/new?";
    private static final String LABELS = "exception";

    private static final int STACKTRACE_LENGTH = 5000;

    @Override
    public @NlsActions.ActionText @NotNull String getReportActionText() {
        return GraphQLBundle.message("graphql.report.to.issue.tracker");
    }

    @Override
    public boolean submit(IdeaLoggingEvent @NotNull [] events,
                          @Nullable String additionalInfo,
                          @NotNull Component parentComponent,
                          @NotNull Consumer<? super SubmittedReportInfo> consumer) {
        IdeaLoggingEvent event = ArrayUtil.getFirstElement(events);
        String title = "Exception: ";
        String stacktrace = "Please paste the full stacktrace from the IDEA error popup.\n";

        if (event != null) {
            String throwableText = event.getThrowableText();
            String exceptionTitle = throwableText.lines().findFirst().orElse(event.getMessage());
            title += !StringUtil.isEmptyOrSpaces(exceptionTitle) ? exceptionTitle : "<Fill in title>";

            if (!StringUtil.isEmptyOrSpaces(throwableText)) {
                String quotes = "\n```\n";
                stacktrace += quotes + StringUtil.first(throwableText, STACKTRACE_LENGTH, true) + quotes;
            }
        }

        IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.getId("com.intellij.lang.jsgraphql"));
        String pluginVersion = plugin != null ? plugin.getVersion() : "";
        String ideaVersion = ApplicationInfo.getInstance().getBuild().asString();

        StringBuilder template = new StringBuilder();
        template.append("### Description\n");
        if (additionalInfo != null) {
            template.append(additionalInfo).append("\n");
        }
        template.append("\n");

        template.append("### Stacktrace\n").append(stacktrace).append("\n");

        template.append("### Version and Environment Details\n")
            .append("Operation system: ").append(SystemInfo.getOsNameAndVersion()).append("\n")
            .append("IDE version: ").append(ideaVersion).append("\n")
            .append("Plugin version: ").append(pluginVersion);

        Charset charset = StandardCharsets.UTF_8;
        String url = String.format(
            "%stitle=%s&labels=%s&body=%s",
            URL,
            URLEncoder.encode(title, charset),
            URLEncoder.encode(LABELS, charset),
            URLEncoder.encode(template.toString(), charset));

        BrowserUtil.browse(url);
        consumer.consume(
            new SubmittedReportInfo(
                null,
                "GitHub issue",
                SubmittedReportInfo.SubmissionStatus.NEW_ISSUE
            )
        );
        return true;
    }
}


