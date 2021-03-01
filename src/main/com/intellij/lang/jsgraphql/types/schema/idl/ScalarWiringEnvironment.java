package com.intellij.lang.jsgraphql.types.schema.idl;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.ScalarTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.ScalarTypeExtensionDefinition;

import java.util.List;

@PublicApi
public class ScalarWiringEnvironment extends WiringEnvironment {

    private final ScalarTypeDefinition scalarTypeDefinition;
    private final List<ScalarTypeExtensionDefinition> extensions;

    ScalarWiringEnvironment(TypeDefinitionRegistry registry, ScalarTypeDefinition interfaceTypeDefinition, List<ScalarTypeExtensionDefinition> extensions) {
        super(registry);
        this.scalarTypeDefinition = interfaceTypeDefinition;
        this.extensions = extensions;
    }

    public ScalarTypeDefinition getScalarTypeDefinition() {
        return scalarTypeDefinition;
    }

    public List<ScalarTypeExtensionDefinition> getExtensions() {
        return extensions;
    }
}
