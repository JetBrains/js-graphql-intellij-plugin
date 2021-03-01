package com.intellij.lang.jsgraphql.types.language;


import com.intellij.lang.jsgraphql.types.PublicApi;

/**
 * All type definitions in a SDL.
 *
 * @param <T> the actual Node type
 */
@PublicApi
public interface TypeDefinition<T extends TypeDefinition> extends SDLDefinition<T>, DirectivesContainer<T>, NamedNode<T> {

}
