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
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.schema.TypeResolver;

import java.util.function.UnaryOperator;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

/**
 * A type runtime wiring is a specification of the data fetchers and possible type resolver for a given type name.
 * <p>
 * This is used by {@link RuntimeWiring} to wire together a functional {@link GraphQLSchema}
 */
@PublicApi
public class TypeRuntimeWiring {
  private final String typeName;
  private final TypeResolver typeResolver;
  private final EnumValuesProvider enumValuesProvider;

  private TypeRuntimeWiring(String typeName,
                            TypeResolver typeResolver,
                            EnumValuesProvider enumValuesProvider) {
    this.typeName = typeName;
    this.typeResolver = typeResolver;
    this.enumValuesProvider = enumValuesProvider;
  }

  /**
   * Creates a new type wiring builder
   *
   * @param typeName the name of the type to wire
   * @return the builder
   */
  public static Builder newTypeWiring(String typeName) {
    assertNotNull(typeName, () -> "You must provide a type name");
    return new Builder().typeName(typeName);
  }

  /**
   * This form allows a lambda to be used as the builder
   *
   * @param typeName        the name of the type to wire
   * @param builderFunction a function that will be given the builder to use
   * @return the same builder back please
   */
  public static TypeRuntimeWiring newTypeWiring(String typeName, UnaryOperator<Builder> builderFunction) {
    return builderFunction.apply(newTypeWiring(typeName)).build();
  }

  public String getTypeName() {
    return typeName;
  }

  public TypeResolver getTypeResolver() {
    return typeResolver;
  }

  public EnumValuesProvider getEnumValuesProvider() {
    return enumValuesProvider;
  }

  public static class Builder {
    private String typeName;
    private TypeResolver typeResolver;
    private EnumValuesProvider enumValuesProvider;

    /**
     * Sets the type name for this type wiring.  You MUST set this.
     *
     * @param typeName the name of the type
     * @return the current type wiring
     */
    public Builder typeName(String typeName) {
      this.typeName = typeName;
      return this;
    }

    /**
     * Adds a {@link TypeResolver} to the current type.  This MUST be specified for Interface
     * and Union types.
     *
     * @param typeResolver the type resolver in play
     * @return the current type wiring
     */
    public Builder typeResolver(TypeResolver typeResolver) {
      assertNotNull(typeResolver, () -> "you must provide a type resolver");
      this.typeResolver = typeResolver;
      return this;
    }

    public Builder enumValues(EnumValuesProvider enumValuesProvider) {
      assertNotNull(enumValuesProvider, () -> "you must provide a type resolver");
      this.enumValuesProvider = enumValuesProvider;
      return this;
    }

    /**
     * @return the built type wiring
     */
    public TypeRuntimeWiring build() {
      assertNotNull(typeName, () -> "you must provide a type name");
      return new TypeRuntimeWiring(typeName, typeResolver, enumValuesProvider);
    }
  }
}
