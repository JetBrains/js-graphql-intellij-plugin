/*
    The MIT License (MIT)

    Copyright (c) 2015 Andreas Marek and Contributors

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
    (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
    publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
    so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
    OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
    CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.intellij.lang.jsgraphql.types.schema.idl;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.ScalarTypeDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLScalarType;
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
        else {
          // we can provide a PSI based scalar definition from Specification.graphql library,
          // so we need to ensure it has a valid coercing assigned for type checking
          GraphQLScalarType scalarType = GraphQLScalarType.newScalar()
            .name(name)
            .description(name)
            .coercing(ScalarInfo.GRAPHQL_SPECIFICATION_SCALARS_MAP.get(name).getCoercing())
            .build();
          wiring.scalar(scalarType);
        }
      });
    });

    return new SchemaGenerator().makeExecutableSchema(registry, runtimeWiring);
  }
}
