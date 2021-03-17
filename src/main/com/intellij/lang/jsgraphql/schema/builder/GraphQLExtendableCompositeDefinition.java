package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.lang.jsgraphql.types.language.SDLDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class GraphQLExtendableCompositeDefinition<T extends SDLDefinition<T>, E extends T> extends GraphQLCompositeDefinition<T> {
    protected final List<E> myExtensions = new SmartList<>();

    @Nullable
    protected List<E> myMergedExtensions;

    public void addExtension(@Nullable E extension) {
        myMergedExtensions = null;

        if (extension != null) {
            myExtensions.add(extension);
        }
    }

    @NotNull
    public List<E> getSourceExtensions() {
        return ContainerUtil.unmodifiableOrEmptyList(myExtensions);
    }

    @NotNull
    public final List<E> getProcessedExtensions() {
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

        return myMergedExtensions = ContainerUtil.unmodifiableOrEmptyList(processExtensions());
    }

    /**
     * Called only when at least one extension definition is added.
     * @return
     */
    @NotNull
    protected abstract List<E> processExtensions();
}
