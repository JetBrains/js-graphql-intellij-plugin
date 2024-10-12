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
package com.intellij.lang.jsgraphql.types;

import com.intellij.lang.jsgraphql.types.collect.ImmutableMapWithNullValues;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;

import java.util.Map;

@SuppressWarnings("TypeParameterUnusedInFormals")
@PublicApi
public class TypeResolutionEnvironment {

  private final Object object;
  private final ImmutableMapWithNullValues<String, Object> arguments;
  private final GraphQLType fieldType;
  private final GraphQLSchema schema;
  private final Object context;

  public TypeResolutionEnvironment(Object object,
                                   Map<String, Object> arguments,
                                   GraphQLType fieldType,
                                   GraphQLSchema schema,
                                   final Object context) {
    this.object = object;
    this.arguments = ImmutableMapWithNullValues.copyOf(arguments);
    this.fieldType = fieldType;
    this.schema = schema;
    this.context = context;
  }

  /**
   * You will be passed the specific source object that needs to be resolve into a concrete graphql object type
   *
   * @param <T> you decide what type it is
   * @return the object that needs to be resolved into a specific graphql object type
   */
  @SuppressWarnings("unchecked")
  public <T> T getObject() {
    return (T)object;
  }

  /**
   * @return the runtime arguments to this the graphql field
   */
  public Map<String, Object> getArguments() {
    return arguments;
  }

  public GraphQLType getFieldType() {
    return fieldType;
  }

  /**
   * @return the graphql schema in question
   */
  public GraphQLSchema getSchema() {
    return schema;
  }

  public <T> T getContext() {
    return (T)context;
  }
}
