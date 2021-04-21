package com.intellij.lang.jsgraphql;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class GraphQLTestUtils {
    public static @NotNull String getTestBasePath() {
        return "test-resources/testData/graphql";
    }

    public static @NotNull String getTestDataPath(@NotNull String path) {
        return String.join(path.startsWith(File.separator) ? "" : File.separator, getTestBasePath(), path);
    }
}
