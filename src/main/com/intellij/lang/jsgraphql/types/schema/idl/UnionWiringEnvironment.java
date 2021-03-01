package com.intellij.lang.jsgraphql.types.schema.idl;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.UnionTypeDefinition;

@PublicApi
public class UnionWiringEnvironment extends WiringEnvironment {

    private final UnionTypeDefinition unionTypeDefinition;

    UnionWiringEnvironment(TypeDefinitionRegistry registry, UnionTypeDefinition unionTypeDefinition) {
        super(registry);
        this.unionTypeDefinition = unionTypeDefinition;
    }

    public UnionTypeDefinition getUnionTypeDefinition() {
        return unionTypeDefinition;
    }
}
