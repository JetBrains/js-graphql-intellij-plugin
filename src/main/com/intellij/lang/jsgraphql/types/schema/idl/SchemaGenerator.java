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

import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.OperationTypeDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLDirective;
import com.intellij.lang.jsgraphql.types.schema.GraphQLObjectType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import com.intellij.lang.jsgraphql.types.schema.idl.errors.SchemaProblem;
import com.intellij.openapi.diagnostic.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;


/**
 * This can generate a working runtime schema from a type registry and runtime wiring
 */
@PublicApi
public class SchemaGenerator {

  private static final Logger LOG = Logger.getInstance(SchemaGenerator.class);

  /**
   * These options control how the schema generation works
   */
  public static class Options {

    Options() {
    }

    public static Options defaultOptions() {
      return new Options();
    }
  }

  private final SchemaTypeChecker typeChecker = new SchemaTypeChecker();
  private final SchemaGeneratorHelper schemaGeneratorHelper = new SchemaGeneratorHelper();

  public SchemaGenerator() {
  }

  /**
   * This will take a {@link TypeDefinitionRegistry} and a {@link RuntimeWiring} and put them together to create a executable schema
   *
   * @param typeRegistry type registry
   * @param wiring       this can be built using {@link RuntimeWiring#newRuntimeWiring()}
   * @return an executable schema
   * @throws SchemaProblem if there are problems in assembling a schema such as missing type resolvers or no operations defined
   */
  public GraphQLSchema makeExecutableSchema(TypeDefinitionRegistry typeRegistry, RuntimeWiring wiring)
    throws SchemaProblem {
    return makeExecutableSchema(Options.defaultOptions(), typeRegistry, wiring);
  }

  /**
   * This will take a {@link TypeDefinitionRegistry} and a {@link RuntimeWiring} and put them together to create a executable schema
   * controlled by the provided options.
   *
   * @param options      the controlling options
   * @param typeRegistry type registry
   * @param wiring       this can be built using {@link RuntimeWiring#newRuntimeWiring()}
   * @return an executable schema
   * @throws SchemaProblem if there are problems in assembling a schema such as missing type resolvers or no operations defined
   */
  public GraphQLSchema makeExecutableSchema(Options options,
                                            TypeDefinitionRegistry typeRegistry,
                                            RuntimeWiring wiring) throws SchemaProblem {

    TypeDefinitionRegistry typeRegistryCopy = new TypeDefinitionRegistry();
    typeRegistryCopy.merge(typeRegistry);

    schemaGeneratorHelper.addDirectivesIncludedByDefault(typeRegistryCopy);

    List<GraphQLError> errors = typeChecker.checkTypeRegistry(typeRegistryCopy, wiring);

    Map<String, OperationTypeDefinition> operationTypeDefinitions = SchemaExtensionsChecker.gatherOperationDefs(typeRegistry);

    GraphQLSchema schema;
    try {
      schema = makeExecutableSchemaImpl(typeRegistryCopy, wiring, operationTypeDefinitions);
    }
    catch (CancellationException e) {
      throw e;
    }
    catch (Exception e) {
      LOG.error("Schema build error: ", e); // we should prevent any thrown exceptions during schema build
      schema = GraphQLSchema.newSchema()
        .query(GraphQLObjectType.newObject().name("Query").build()).build();
    }

    if (!errors.isEmpty()) {
      schema.addError(new SchemaProblem(errors));
    }
    return schema;
  }

  private GraphQLSchema makeExecutableSchemaImpl(TypeDefinitionRegistry typeRegistry,
                                                 RuntimeWiring wiring,
                                                 Map<String, OperationTypeDefinition> operationTypeDefinitions) {
    SchemaGeneratorHelper.BuildContext buildCtx = new SchemaGeneratorHelper.BuildContext(typeRegistry, wiring,
                                                                                         operationTypeDefinitions);

    GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema();

    Set<GraphQLDirective> additionalDirectives = schemaGeneratorHelper.buildAdditionalDirectives(buildCtx);
    schemaBuilder.additionalDirectives(additionalDirectives);

    schemaGeneratorHelper.buildSchemaDirectivesAndExtensions(buildCtx, schemaBuilder);

    schemaGeneratorHelper.buildOperations(buildCtx, schemaBuilder);

    Set<GraphQLType> additionalTypes = schemaGeneratorHelper.buildAdditionalTypes(buildCtx);
    schemaBuilder.additionalTypes(additionalTypes);

    buildCtx.getTypeRegistry().schemaDefinition().ifPresent(schemaDefinition -> {
      String description = schemaGeneratorHelper.buildDescription(schemaDefinition,
                                                                  schemaDefinition.getDescription());
      schemaBuilder.description(description);
    });
    GraphQLSchema graphQLSchema = schemaBuilder.build();

    List<GraphQLError> buildErrors = buildCtx.getErrors();
    if (!buildErrors.isEmpty()) {
      graphQLSchema.addError(new SchemaProblem(buildErrors));
    }
    return graphQLSchema;
  }
}
