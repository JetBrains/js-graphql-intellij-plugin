package com.intellij.lang.jsgraphql;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

public final class GraphQLBundle {

  private static final String BUNDLE = "messages.GraphQLBundle";

  private static final DynamicBundle INSTANCE = new DynamicBundle(GraphQLBundle.class, BUNDLE);

  private GraphQLBundle() { }

  public static @NotNull @Nls String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object @NotNull ... params) {
    return INSTANCE.getMessage(key, params);
  }

  public static @NotNull Supplier<@Nls String> messagePointer(
    @NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
    Object @NotNull ... params
  ) {
    return INSTANCE.getLazyMessage(key, params);
  }
}
