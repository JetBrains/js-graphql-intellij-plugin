package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import graphql.language.SDLDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public abstract class GraphQLExtendableDefinitionBuilder<T extends SDLDefinition<T>, E extends T> extends GraphQLDefinitionBuilder<T> {
    protected final Collection<E> myExtensions = new SmartList<>();

    @Nullable
    protected Collection<E> myBuiltExtensions;

    public void addExtension(@NotNull SDLDefinition<?> extension) {
        if (isExpectedExtensionType(extension)) {
            //noinspection unchecked
            myExtensions.add((E) extension);
        }
    }

    @NotNull
    public Collection<E> getSourceExtensions() {
        return ContainerUtil.unmodifiableOrEmptyCollection(myExtensions);
    }

    @NotNull
    public final Collection<E> buildExtensions() {
        if (myBuiltExtensions != null) {
            return myBuiltExtensions;
        }

        if (myExtensions.isEmpty()) {
            return myBuiltExtensions = ContainerUtil.emptyList();
        }

        buildDefinition();
        if (myBuiltDefinition == null) {
            return myBuiltExtensions = ContainerUtil.emptyList();
        }

        return myBuiltExtensions = ContainerUtil.unmodifiableOrEmptyCollection(buildExtensionsImpl());
    }

    public boolean isExpectedExtensionType(@NotNull SDLDefinition<?> definition) {
        return getExtensionClass().isAssignableFrom(definition.getClass());
    }

    /**
     * Called only when at least one extension definition is added.
     */
    @NotNull
    protected abstract Collection<E> buildExtensionsImpl();

    @NotNull
    protected abstract Class<E> getExtensionClass();
}
