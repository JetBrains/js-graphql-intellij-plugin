package com.intellij.lang.jsgraphql.types.schema.idl;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.InterfaceTypeDefinition;

@PublicApi
public class InterfaceWiringEnvironment extends WiringEnvironment {

    private final InterfaceTypeDefinition interfaceTypeDefinition;

    InterfaceWiringEnvironment(TypeDefinitionRegistry registry, InterfaceTypeDefinition interfaceTypeDefinition) {
        super(registry);
        this.interfaceTypeDefinition = interfaceTypeDefinition;
    }

    public InterfaceTypeDefinition getInterfaceTypeDefinition() {
        return interfaceTypeDefinition;
    }
}
