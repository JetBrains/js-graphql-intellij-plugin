package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.lang.jsgraphql.types.language.SDLDefinition;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class GraphQLExtendableCompositeDefinition<T extends SDLDefinition<T>, E extends T> extends GraphQLCompositeDefinition<T> {
  private final List<E> myExtensions = new SmartList<>();

  public void addExtension(@Nullable E extension) {
    if (extension != null) {
      myExtensions.add(extension);
    }
  }

  public @NotNull List<E> getSourceExtensions() {
    return myExtensions;
  }
}
