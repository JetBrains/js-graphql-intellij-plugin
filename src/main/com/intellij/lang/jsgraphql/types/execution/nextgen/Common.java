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
package com.intellij.lang.jsgraphql.types.execution.nextgen;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.execution.MissingRootTypeException;
import com.intellij.lang.jsgraphql.types.language.OperationDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLObjectType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;

import static com.intellij.lang.jsgraphql.types.Assert.assertShouldNeverHappen;
import static com.intellij.lang.jsgraphql.types.language.OperationDefinition.Operation.*;

@Internal
public class Common {

  public static GraphQLObjectType getOperationRootType(GraphQLSchema graphQLSchema, OperationDefinition operationDefinition) {
    OperationDefinition.Operation operation = operationDefinition.getOperation();
    if (operation == MUTATION) {
      GraphQLObjectType mutationType = graphQLSchema.getMutationType();
      if (mutationType == null) {
        throw new MissingRootTypeException("Schema is not configured for mutations.", operationDefinition.getSourceLocation());
      }
      return mutationType;
    }
    else if (operation == QUERY) {
      GraphQLObjectType queryType = graphQLSchema.getQueryType();
      if (queryType == null) {
        throw new MissingRootTypeException("Schema does not define the required query root type.", operationDefinition.getSourceLocation());
      }
      return queryType;
    }
    else if (operation == SUBSCRIPTION) {
      GraphQLObjectType subscriptionType = graphQLSchema.getSubscriptionType();
      if (subscriptionType == null) {
        throw new MissingRootTypeException("Schema is not configured for subscriptions.", operationDefinition.getSourceLocation());
      }
      return subscriptionType;
    }
    else {
      return assertShouldNeverHappen("Unhandled case.  An extra operation enum has been added without code support");
    }
  }
}
