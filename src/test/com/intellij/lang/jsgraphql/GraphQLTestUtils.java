package com.intellij.lang.jsgraphql;

import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;

public final class GraphQLTestUtils {
    public static @NotNull String getTestBasePath() {
        return "test-resources/testData/graphql";
    }

    public static @NotNull String getTestDataPath(@NotNull String path) {
        return FileUtil.join(getTestBasePath(), path);
    }
}
