package com.intellij.lang.jsgraphql.types.language;


import com.intellij.lang.jsgraphql.types.PublicApi;

/**
 * All Schema Definition Language (SDL) Definitions.
 *
 * @param <T> the actual Node type
 */
@PublicApi
public interface SDLDefinition<T extends SDLDefinition> extends Definition<T> {

}
