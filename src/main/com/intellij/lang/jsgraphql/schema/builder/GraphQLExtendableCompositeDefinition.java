package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.lang.jsgraphql.types.language.SDLDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public abstract class GraphQLExtendableCompositeDefinition<T extends SDLDefinition<T>, E extends T> extends GraphQLCompositeDefinition<T> {
    protected final Collection<E> myExtensions = new SmartList<>();

    @Nullable
    protected Collection<E> myMergedExtensions;

    public void addExtension(@Nullable E extension) {
        myMergedExtensions = null;

        if (extension != null) {
            myExtensions.add(extension);
        }
    }

    @NotNull
    public Collection<E> getSourceExtensions() {
        return ContainerUtil.unmodifiableOrEmptyCollection(myExtensions);
    }

    @NotNull
    public final Collection<E> getBuiltExtensions() {
        if (myMergedExtensions != null) {
            return myMergedExtensions;
        }

        if (myExtensions.isEmpty()) {
            return myMergedExtensions = ContainerUtil.emptyList();
        }

        getMergedDefinition();
        if (myMergedDefinition == null) {
            return myMergedExtensions = ContainerUtil.emptyList();
        }

        return myMergedExtensions = ContainerUtil.unmodifiableOrEmptyCollection(buildExtensions());
    }

    /**
     * Called only when at least one extension definition is added.
     */
    @NotNull
    protected abstract Collection<E> buildExtensions();
}
