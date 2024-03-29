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
package com.intellij.lang.jsgraphql.types.analysis;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.Field;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.language.SelectionSetContainer;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldsContainer;
import com.intellij.lang.jsgraphql.types.schema.GraphQLOutputType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;

import java.util.Map;

@PublicApi
public interface QueryVisitorFieldEnvironment {

  /**
   * @return the graphql schema in play
   */
  GraphQLSchema getSchema();

  /**
   * @return true if the current field is __typename
   */
  boolean isTypeNameIntrospectionField();

  /**
   * @return the current Field
   */
  Field getField();

  GraphQLFieldDefinition getFieldDefinition();

  /**
   * @return the parent output type of the current field.
   */
  GraphQLOutputType getParentType();

  /**
   * @return the unmodified fields container fot the current type. This is the unwrapped version of {@link #getParentType()}
   * It is either {@link com.intellij.lang.jsgraphql.types.schema.GraphQLObjectType} or {@link com.intellij.lang.jsgraphql.types.schema.GraphQLInterfaceType}. because these
   * are the only {@link GraphQLFieldsContainer}
   * @throws IllegalStateException if the current field is __typename see {@link #isTypeNameIntrospectionField()}
   */
  GraphQLFieldsContainer getFieldsContainer();

  QueryVisitorFieldEnvironment getParentEnvironment();

  Map<String, Object> getArguments();

  SelectionSetContainer getSelectionSetContainer();

  TraverserContext<Node> getTraverserContext();
}
