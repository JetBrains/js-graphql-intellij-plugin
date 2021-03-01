package com.intellij.lang.jsgraphql.types.schema.idl;


import com.intellij.lang.jsgraphql.types.PublicApi;

@PublicApi
abstract class WiringEnvironment {

    private final TypeDefinitionRegistry registry;

    WiringEnvironment(TypeDefinitionRegistry registry) {
        this.registry = registry;
    }

    public TypeDefinitionRegistry getRegistry() {
        return registry;
    }
}
