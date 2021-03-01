package com.intellij.lang.jsgraphql.schema;

import com.intellij.lang.jsgraphql.types.GraphQLException;
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

interface GraphQLRegistryBuilder {
    void merge(@NotNull TypeDefinitionRegistry source);

    @NotNull
    TypeDefinitionRegistry build();

    @NotNull
    List<GraphQLException> getErrors();
}
