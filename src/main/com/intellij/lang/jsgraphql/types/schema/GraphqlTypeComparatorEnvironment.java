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
package com.intellij.lang.jsgraphql.types.schema;

import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Defines the scope to control where the registered {@code Comparator} can be applied.
 * <p>
 * {@code elementType}s can be ordered within its {@code parentType} to restrict the {@code Comparator}s scope of operation.
 * Otherwise supplying only the {@code elementType} results in the {@code Comparator} being reused across all matching {@code GraphQLType}s regardless of parent.
 */
@PublicApi
public class GraphqlTypeComparatorEnvironment {

  private Class<? extends GraphQLSchemaElement> parentType;

  private Class<? extends GraphQLSchemaElement> elementType;

  private GraphqlTypeComparatorEnvironment(Class<? extends GraphQLSchemaElement> parentType,
                                           Class<? extends GraphQLSchemaElement> elementType) {
    Assert.assertNotNull(elementType, () -> "elementType can't be null");
    this.parentType = parentType;
    this.elementType = elementType;
  }

  /**
   * @return The parent type or {@code null} if not supplied.
   */
  public Class<? extends GraphQLSchemaElement> getParentType() {
    return parentType;
  }

  /**
   * @return The valid element type.
   */
  public Class<? extends GraphQLSchemaElement> getElementType() {
    return elementType;
  }

  /**
   * This helps you transform the current {@code GraphqlTypeComparatorEnvironment} into another one by starting a builder with all
   * the current values and allows you to transform it how you want.
   *
   * @param builderConsumer the consumer code that will be given a builder to transform.
   * @return a new object based on calling build on that builder.
   */
  public GraphqlTypeComparatorEnvironment transform(Consumer<Builder> builderConsumer) {
    Builder builder = newEnvironment(this);
    builderConsumer.accept(builder);
    return builder.build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GraphqlTypeComparatorEnvironment that = (GraphqlTypeComparatorEnvironment)o;
    return Objects.equals(parentType, that.parentType) && elementType.equals(that.elementType);
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + Objects.hashCode(parentType);
    result = 31 * result + Objects.hashCode(elementType);
    return result;
  }

  public static Builder newEnvironment() {
    return new Builder();
  }

  public static Builder newEnvironment(GraphqlTypeComparatorEnvironment existing) {
    return new Builder(existing);
  }

  public static class Builder {

    private Class<? extends GraphQLSchemaElement> parentType;

    private Class<? extends GraphQLSchemaElement> elementType;

    public Builder() {
    }

    public Builder(GraphqlTypeComparatorEnvironment existing) {
      this.parentType = existing.parentType;
      this.elementType = existing.elementType;
    }

    public <T extends GraphQLSchemaElement> Builder parentType(Class<T> parentType) {
      this.parentType = parentType;
      return this;
    }

    public <T extends GraphQLSchemaElement> Builder elementType(Class<T> elementType) {
      this.elementType = elementType;
      return this;
    }

    public GraphqlTypeComparatorEnvironment build() {
      return new GraphqlTypeComparatorEnvironment(parentType, elementType);
    }
  }
}
