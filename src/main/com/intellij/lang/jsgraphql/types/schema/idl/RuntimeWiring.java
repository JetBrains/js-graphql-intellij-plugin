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

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.schema.GraphQLScalarType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.schema.GraphqlTypeComparatorRegistry;
import com.intellij.lang.jsgraphql.types.schema.TypeResolver;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

/**
 * A runtime wiring is a specification of data fetchers, type resolvers and custom scalars that are needed
 * to wire together a functional {@link GraphQLSchema}
 */
@PublicApi
public class RuntimeWiring {

  private final Map<String, GraphQLScalarType> scalars;
  private final Map<String, TypeResolver> typeResolvers;
  private final Map<String, SchemaDirectiveWiring> registeredDirectiveWiring;
  private final List<SchemaDirectiveWiring> directiveWiring;
  private final WiringFactory wiringFactory;
  private final Map<String, EnumValuesProvider> enumValuesProviders;
  private final GraphqlTypeComparatorRegistry comparatorRegistry;

  private RuntimeWiring(Builder builder) {
    this.scalars = builder.scalars;
    this.typeResolvers = builder.typeResolvers;
    this.registeredDirectiveWiring = builder.registeredDirectiveWiring;
    this.directiveWiring = builder.directiveWiring;
    this.wiringFactory = builder.wiringFactory;
    this.enumValuesProviders = builder.enumValuesProviders;
    this.comparatorRegistry = builder.comparatorRegistry;
  }

  /**
   * @return a builder of Runtime Wiring
   */
  public static Builder newRuntimeWiring() {
    return new Builder();
  }

  public Map<String, GraphQLScalarType> getScalars() {
    return new LinkedHashMap<>(scalars);
  }

  public Map<String, TypeResolver> getTypeResolvers() {
    return typeResolvers;
  }

  public Map<String, EnumValuesProvider> getEnumValuesProviders() {
    return this.enumValuesProviders;
  }

  public WiringFactory getWiringFactory() {
    return wiringFactory;
  }

  public Map<String, SchemaDirectiveWiring> getRegisteredDirectiveWiring() {
    return registeredDirectiveWiring;
  }

  public List<SchemaDirectiveWiring> getDirectiveWiring() {
    return directiveWiring;
  }

  public GraphqlTypeComparatorRegistry getComparatorRegistry() {
    return comparatorRegistry;
  }

  @PublicApi
  public static class Builder {
    private final Map<String, GraphQLScalarType> scalars = new LinkedHashMap<>();
    private final Map<String, TypeResolver> typeResolvers = new LinkedHashMap<>();
    private final Map<String, EnumValuesProvider> enumValuesProviders = new LinkedHashMap<>();
    private final Map<String, SchemaDirectiveWiring> registeredDirectiveWiring = new LinkedHashMap<>();
    private final List<SchemaDirectiveWiring> directiveWiring = new ArrayList<>();
    private WiringFactory wiringFactory = new NoopWiringFactory();
    private GraphqlTypeComparatorRegistry comparatorRegistry = GraphqlTypeComparatorRegistry.AS_IS_REGISTRY;

    private Builder() {
    }

    /**
     * Adds a wiring factory into the runtime wiring
     *
     * @param wiringFactory the wiring factory to add
     * @return this outer builder
     */
    public Builder wiringFactory(WiringFactory wiringFactory) {
      assertNotNull(wiringFactory, () -> "You must provide a wiring factory");
      this.wiringFactory = wiringFactory;
      return this;
    }

    /**
     * This allows you to add in new custom Scalar implementations beyond the standard set.
     *
     * @param scalarType the new scalar implementation
     * @return the runtime wiring builder
     */
    public Builder scalar(GraphQLScalarType scalarType) {
      scalars.put(scalarType.getName(), scalarType);
      return this;
    }

    /**
     * This allows you to add a new type wiring via a builder
     *
     * @param builder the type wiring builder to use
     * @return this outer builder
     */
    public Builder type(TypeRuntimeWiring.Builder builder) {
      return type(builder.build());
    }

    /**
     * This form allows a lambda to be used as the builder of a type wiring
     *
     * @param typeName        the name of the type to wire
     * @param builderFunction a function that will be given the builder to use
     * @return the runtime wiring builder
     */
    public Builder type(String typeName, UnaryOperator<TypeRuntimeWiring.Builder> builderFunction) {
      TypeRuntimeWiring.Builder builder = builderFunction.apply(TypeRuntimeWiring.newTypeWiring(typeName));
      return type(builder.build());
    }

    /**
     * This adds a type wiring
     *
     * @param typeRuntimeWiring the new type wiring
     * @return the runtime wiring builder
     */
    public Builder type(TypeRuntimeWiring typeRuntimeWiring) {
      String typeName = typeRuntimeWiring.getTypeName();

      TypeResolver typeResolver = typeRuntimeWiring.getTypeResolver();
      if (typeResolver != null) {
        this.typeResolvers.put(typeName, typeResolver);
      }

      EnumValuesProvider enumValuesProvider = typeRuntimeWiring.getEnumValuesProvider();
      if (enumValuesProvider != null) {
        this.enumValuesProviders.put(typeName, enumValuesProvider);
      }
      return this;
    }

    /**
     * This provides the wiring code for a named directive.
     * <p>
     * Note: The provided directive wiring will ONLY be called back if an element has a directive
     * with the specified name.
     * <p>
     * To be called back for every directive the use {@link #directiveWiring(SchemaDirectiveWiring)} or
     * use {@link WiringFactory#providesSchemaDirectiveWiring(SchemaDirectiveWiringEnvironment)}
     * instead.
     *
     * @param directiveName         the name of the directive to wire
     * @param schemaDirectiveWiring the runtime behaviour of this wiring
     * @return the runtime wiring builder
     * @see #directiveWiring(SchemaDirectiveWiring)
     * @see SchemaDirectiveWiring
     * @see WiringFactory#providesSchemaDirectiveWiring(SchemaDirectiveWiringEnvironment)
     */
    public Builder directive(String directiveName, SchemaDirectiveWiring schemaDirectiveWiring) {
      registeredDirectiveWiring.put(directiveName, schemaDirectiveWiring);
      return this;
    }

    /**
     * This adds a directive wiring that will be called for all directives.
     * <p>
     * Note : Unlike {@link #directive(String, SchemaDirectiveWiring)} which is only called back if a  named
     * directives is present, this directive wiring will be called back for every element
     * in the schema even if it has zero directives.
     *
     * @param schemaDirectiveWiring the runtime behaviour of this wiring
     * @return the runtime wiring builder
     * @see #directive(String, SchemaDirectiveWiring)
     * @see SchemaDirectiveWiring
     * @see WiringFactory#providesSchemaDirectiveWiring(SchemaDirectiveWiringEnvironment)
     */
    public Builder directiveWiring(SchemaDirectiveWiring schemaDirectiveWiring) {
      directiveWiring.add(schemaDirectiveWiring);
      return this;
    }

    /**
     * You can specify your own sort order of graphql types via {@link GraphqlTypeComparatorRegistry}
     * which will tell you what type of objects you are to sort when
     * it asks for a comparator.
     *
     * @param comparatorRegistry your own comparator registry
     * @return the runtime wiring builder
     */
    public Builder comparatorRegistry(GraphqlTypeComparatorRegistry comparatorRegistry) {
      this.comparatorRegistry = comparatorRegistry;
      return this;
    }

    /**
     * @return the built runtime wiring
     */
    public RuntimeWiring build() {
      return new RuntimeWiring(this);
    }
  }
}

