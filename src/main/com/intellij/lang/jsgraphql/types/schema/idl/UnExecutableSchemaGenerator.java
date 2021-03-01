package com.intellij.lang.jsgraphql.types.schema.idl;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.ScalarTypeDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;

import java.util.Map;

import static com.intellij.lang.jsgraphql.types.schema.idl.EchoingWiringFactory.fakeScalar;

@Internal
public class UnExecutableSchemaGenerator {

    /*
     * Creates just enough runtime wiring to allow a schema to be built but which CANT
     * be sensibly executed
     */
    public static GraphQLSchema makeUnExecutableSchema(TypeDefinitionRegistry registry) {
        RuntimeWiring runtimeWiring = EchoingWiringFactory.newEchoingWiring(wiring -> {
            Map<String, ScalarTypeDefinition> scalars = registry.scalars();
            scalars.forEach((name, v) -> {
                if (!ScalarInfo.isGraphqlSpecifiedScalar(name)) {
                    wiring.scalar(fakeScalar(name));
                }
            });
        });

        return new SchemaGenerator().makeExecutableSchema(registry, runtimeWiring);
    }
}
