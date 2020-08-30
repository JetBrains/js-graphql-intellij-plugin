package com.intellij.lang.jsgraphql.schema.builder;

import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import graphql.language.SDLDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public abstract class GraphQLDefinitionBuilder<T extends SDLDefinition<T>> {
    protected final Collection<T> myDefinitions = new SmartList<>();

    @Nullable
    protected T myBuiltDefinition;

    public void addDefinition(@NotNull SDLDefinition<?> definition) {
        if (isExpectedDefinitionType(definition)) {
            //noinspection unchecked
            myDefinitions.add((T) definition);
        }
    }

    @NotNull
    public Collection<T> getSourceDefinitions() {
        return ContainerUtil.unmodifiableOrEmptyCollection(myDefinitions);
    }

    @Nullable
    public final T buildDefinition() {
        if (myBuiltDefinition != null) {
            return myBuiltDefinition;
        }

        if (myDefinitions.isEmpty()) {
            return null;
        }

        return myBuiltDefinition = buildDefinitionImpl();
    }

    /**
     * Note that a definition extension is a subtype of the definition itself.
     */
    public boolean isExpectedDefinitionType(@NotNull SDLDefinition<?> definition) {
        return getDefinitionClass().isAssignableFrom(definition.getClass());
    }

    /**
     * Called only when at least one type definition is added.
     */
    @NotNull
    protected abstract T buildDefinitionImpl();

    @NotNull
    protected abstract Class<T> getDefinitionClass();
}
