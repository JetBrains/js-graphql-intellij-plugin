package com.intellij.lang.jsgraphql;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

public class GraphQLBundle extends AbstractBundle {
    @NonNls
    public static final String PATH = "messages.GraphQLMessages";

    private static final GraphQLBundle INSTANCE = new GraphQLBundle();

    private GraphQLBundle() {
        super(PATH);
    }

    @NotNull
    public static @Nls String message(@NotNull @PropertyKey(resourceBundle = PATH) String key, Object @NotNull ... params) {
        return INSTANCE.getMessage(key, params);
    }

    public static @NotNull Supplier<@Nls String> messagePointer(
        @NotNull @PropertyKey(resourceBundle = PATH) String key,
        Object @NotNull ... params
    ) {
        return INSTANCE.getLazyMessage(key, params);
    }
}
