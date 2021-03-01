package com.intellij.lang.jsgraphql.types.language;


import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.List;

/**
 * A {@link TypeDefinition} that might implement interfaces
 *
 * @param <T> for two
 */
@PublicApi
public interface ImplementingTypeDefinition<T extends TypeDefinition> extends TypeDefinition<T> {

    List<Type> getImplements();

    List<FieldDefinition> getFieldDefinitions();
}
