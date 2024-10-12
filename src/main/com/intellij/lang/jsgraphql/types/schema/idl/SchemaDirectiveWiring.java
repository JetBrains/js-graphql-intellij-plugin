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
import com.intellij.lang.jsgraphql.types.schema.*;

/**
 * A SchemaDirectiveWiring is responsible for enhancing a runtime element based on directives placed on that
 * element in the Schema Definition Language (SDL).
 * <p>
 * The SchemaDirectiveWiring objects are called in a specific order based on registration:
 * <ol>
 * <li>{@link com.intellij.lang.jsgraphql.types.schema.idl.RuntimeWiring.Builder#directive(String, SchemaDirectiveWiring)} which work against a specific named directive are called first</li>
 * <li>{@link com.intellij.lang.jsgraphql.types.schema.idl.RuntimeWiring.Builder#directiveWiring(SchemaDirectiveWiring)} which work against all directives are called next</li>
 * <li>{@link com.intellij.lang.jsgraphql.types.schema.idl.WiringFactory#providesSchemaDirectiveWiring(SchemaDirectiveWiringEnvironment)} which work against all directives are called last</li>
 * </ol>
 */
@PublicApi
public interface SchemaDirectiveWiring {

  /**
   * This is called when an object is encountered, which gives the schema directive a chance to modify the shape and behaviour
   * of that DSL element
   * <p>
   * The {@link #onArgument(SchemaDirectiveWiringEnvironment)} and {@link #onField(SchemaDirectiveWiringEnvironment)} callbacks will have been
   * invoked for this element beforehand
   *
   * @param environment the wiring element
   * @return a non null element based on the original one
   */
  default GraphQLObjectType onObject(SchemaDirectiveWiringEnvironment<GraphQLObjectType> environment) {
    return environment.getElement();
  }

  /**
   * This is called when a field is encountered, which gives the schema directive a chance to modify the shape and behaviour
   * of that DSL element
   * <p>
   * The {@link #onArgument(SchemaDirectiveWiringEnvironment)} callbacks will have been
   * invoked for this element beforehand
   *
   * @param environment the wiring element
   * @return a non null element based on the original one
   */
  default GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    return environment.getElement();
  }

  /**
   * This is called when an argument is encountered, which gives the schema directive a chance to modify the shape and behaviour
   * of that DSL element
   *
   * @param environment the wiring element
   * @return a non null element based on the original one
   */
  default GraphQLArgument onArgument(SchemaDirectiveWiringEnvironment<GraphQLArgument> environment) {
    return environment.getElement();
  }

  /**
   * This is called when an interface is encountered, which gives the schema directive a chance to modify the shape and behaviour
   * of that DSL element
   * <p>
   * The {@link #onArgument(SchemaDirectiveWiringEnvironment)} and {@link #onField(SchemaDirectiveWiringEnvironment)} callbacks will have been
   * invoked for this element beforehand
   *
   * @param environment the wiring element
   * @return a non null element based on the original one
   */
  default GraphQLInterfaceType onInterface(SchemaDirectiveWiringEnvironment<GraphQLInterfaceType> environment) {
    return environment.getElement();
  }

  /**
   * This is called when a union is encountered, which gives the schema directive a chance to modify the shape and behaviour
   * of that DSL element
   *
   * @param environment the wiring element
   * @return a non null element based on the original one
   */
  default GraphQLUnionType onUnion(SchemaDirectiveWiringEnvironment<GraphQLUnionType> environment) {
    return environment.getElement();
  }

  /**
   * This is called when an enum is encountered, which gives the schema directive a chance to modify the shape and behaviour
   * of that DSL element
   * <p>
   * The {@link #onEnumValue(SchemaDirectiveWiringEnvironment)} callbacks will have been invoked for this element beforehand
   *
   * @param environment the wiring element
   * @return a non null element based on the original one
   */
  default GraphQLEnumType onEnum(SchemaDirectiveWiringEnvironment<GraphQLEnumType> environment) {
    return environment.getElement();
  }

  /**
   * This is called when an enum value is encountered, which gives the schema directive a chance to modify the shape and behaviour
   * of that DSL element
   *
   * @param environment the wiring element
   * @return a non null element based on the original one
   */
  default GraphQLEnumValueDefinition onEnumValue(SchemaDirectiveWiringEnvironment<GraphQLEnumValueDefinition> environment) {
    return environment.getElement();
  }

  /**
   * This is called when a custom scalar is encountered, which gives the schema directive a chance to modify the shape and behaviour
   * of that DSL  element
   *
   * @param environment the wiring element
   * @return a non null element based on the original one
   */
  default GraphQLScalarType onScalar(SchemaDirectiveWiringEnvironment<GraphQLScalarType> environment) {
    return environment.getElement();
  }

  /**
   * This is called when an input object is encountered, which gives the schema directive a chance to modify the shape and behaviour
   * of that DSL  element
   * <p>
   * The {@link #onInputObjectField(SchemaDirectiveWiringEnvironment)}callbacks will have been invoked for this element beforehand
   *
   * @param environment the wiring element
   * @return a non null element based on the original one
   */
  default GraphQLInputObjectType onInputObjectType(SchemaDirectiveWiringEnvironment<GraphQLInputObjectType> environment) {
    return environment.getElement();
  }

  /**
   * This is called when an input object field is encountered, which gives the schema directive a chance to modify the shape and behaviour
   * of that DSL  element
   *
   * @param environment the wiring element
   * @return a non null element based on the original one
   */
  default GraphQLInputObjectField onInputObjectField(SchemaDirectiveWiringEnvironment<GraphQLInputObjectField> environment) {
    return environment.getElement();
  }
}
