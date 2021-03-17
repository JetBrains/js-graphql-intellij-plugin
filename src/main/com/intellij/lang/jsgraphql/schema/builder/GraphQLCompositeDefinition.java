package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.lang.jsgraphql.types.language.SDLDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public abstract class GraphQLCompositeDefinition<T extends SDLDefinition<T>> {
    protected final List<T> myDefinitions = new SmartList<>();

    @Nullable
    protected T myMergedDefinition;

    public void addDefinition(@Nullable T definition) {
        myMergedDefinition = null;

        if (definition != null) {
            myDefinitions.add(definition);
        }
    }

    @NotNull
    public List<T> getSourceDefinitions() {
        return ContainerUtil.unmodifiableOrEmptyList(myDefinitions);
    }

    @Nullable
    public final T getMergedDefinition() {
        if (myMergedDefinition != null) {
            return myMergedDefinition;
        }

        if (myDefinitions.isEmpty()) {
            return null;
        }

        return myMergedDefinition = mergeDefinitions();
    }

    /**
     * Called only when at least one type definition is added.
     */
    @NotNull
    protected abstract T mergeDefinitions();
}
