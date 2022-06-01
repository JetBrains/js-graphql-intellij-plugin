package com.intellij.lang.jsgraphql;

import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryDescriptor;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryManager;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryTypes;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.testFramework.PlatformTestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.function.Consumer;

public final class GraphQLTestUtils {
    public static @NotNull String getTestBasePath() {
        return "test-resources/testData/graphql";
    }

    public static @NotNull String getTestDataPath(@NotNull String path) {
        return String.join(path.startsWith(File.separator) ? "" : File.separator, getTestBasePath(), path);
    }

    public static void withSettings(@NotNull Project project,
                                    @NotNull Consumer<GraphQLSettings> consumer,
                                    @NotNull Disposable disposable) {
        withSettings(project, consumer, null, disposable);
    }

    public static void withSettings(@NotNull Project project,
                                    @NotNull Consumer<GraphQLSettings> consumer,
                                    @Nullable Runnable onDispose,
                                    @NotNull Disposable disposable) {
        GraphQLSettings settings = GraphQLSettings.getSettings(project);
        GraphQLSettings.GraphQLSettingsState previousState = settings.getState();
        assert previousState != null;
        Disposer.register(disposable, () -> {
            settings.loadState(previousState);
            if (onDispose != null) {
                onDispose.run();
            }
        });

        GraphQLSettings.GraphQLSettingsState tempState = new GraphQLSettings.GraphQLSettingsState();
        settings.loadState(tempState);
        consumer.accept(settings);
    }

    public static void withLibrary(@NotNull Project project,
                                   @NotNull GraphQLLibraryDescriptor libraryDescriptor,
                                   @NotNull Runnable testCase,
                                   @NotNull Disposable disposable) {
        withSettings(project, settings -> {
            if (libraryDescriptor == GraphQLLibraryTypes.RELAY) {
                settings.setRelaySupportEnabled(true);
            } else if (libraryDescriptor == GraphQLLibraryTypes.FEDERATION) {
                settings.setFederationSupportEnabled(true);
            } else if (libraryDescriptor == GraphQLLibraryTypes.APOLLO_KOTLIN) {
                settings.setApolloKotlinSupportEnabled(true);
            } else {
                throw new IllegalArgumentException("Unexpected library: " + libraryDescriptor);
            }
            updateLibraries(project);
            testCase.run();
        }, () -> updateLibraries(project), disposable);
    }

    private static void updateLibraries(@NotNull Project project) {
        GraphQLLibraryManager.getInstance(project).notifyLibrariesChanged();
        PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue();
    }
}
