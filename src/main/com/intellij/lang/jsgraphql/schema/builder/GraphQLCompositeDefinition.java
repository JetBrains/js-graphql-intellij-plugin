package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.lang.jsgraphql.schema.GraphQLPsiDocumentBuilder;
import com.intellij.lang.jsgraphql.types.language.SDLDefinition;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class GraphQLCompositeDefinition<T extends SDLDefinition<T>> {
  private final List<T> myDefinitions = new SmartList<>();
  private final List<T> myLibraryDefinitions = new SmartList<>();

  protected @Nullable T myMergedDefinition;

  public void addDefinition(@Nullable T definition) {
    if (definition == null) {
      return;
    }

    if (GraphQLPsiDocumentBuilder.isInLibrary(definition)) {
      myLibraryDefinitions.add(definition);
    }
    else {
      myDefinitions.add(definition);
    }
  }

  public @NotNull List<T> getSourceDefinitions() {
    return !myDefinitions.isEmpty() ? myDefinitions : myLibraryDefinitions;
  }

  public final @Nullable T buildDefinition() {
    if (myMergedDefinition != null) {
      return myMergedDefinition;
    }

    List<T> sourceDefinitions = getSourceDefinitions();
    if (sourceDefinitions.isEmpty()) {
      return null;
    }
    if (sourceDefinitions.size() == 1) {
      return sourceDefinitions.get(0);
    }

    return myMergedDefinition = mergeDefinitions(sourceDefinitions);
  }

  /**
   * Called only when at least one type definition is added.
   */
  protected abstract @NotNull T mergeDefinitions(@NotNull List<T> sourceDefinitions);
}
