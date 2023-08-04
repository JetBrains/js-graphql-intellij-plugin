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
package com.intellij.lang.jsgraphql.types.schema.visibility;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldsContainer;
import com.intellij.lang.jsgraphql.types.schema.GraphQLInputFieldsContainer;
import com.intellij.lang.jsgraphql.types.schema.GraphQLInputObjectField;

import java.util.List;

/**
 * This allows you to control the visibility of graphql fields.  By default
 * graphql-java makes every defined field visible but you can implement an instance of this
 * interface and reduce specific field visibility.
 */
@PublicApi
public interface GraphqlFieldVisibility {

  /**
   * Called to get the list of fields from an object type or interface
   *
   * @param fieldsContainer the type in play
   * @return a non null list of {@link com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition}s
   */
  List<GraphQLFieldDefinition> getFieldDefinitions(GraphQLFieldsContainer fieldsContainer);

  /**
   * Called to get a named field from an object type or interface
   *
   * @param fieldsContainer the type in play
   * @param fieldName       the name of the desired field
   * @return a {@link com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition} or null if its not visible
   */
  GraphQLFieldDefinition getFieldDefinition(GraphQLFieldsContainer fieldsContainer, String fieldName);


  /**
   * Called to get the list of fields from an input object type
   *
   * @param fieldsContainer the type in play
   * @return a non null list of {@link com.intellij.lang.jsgraphql.types.schema.GraphQLInputObjectField}s
   */
  default List<GraphQLInputObjectField> getFieldDefinitions(GraphQLInputFieldsContainer fieldsContainer) {
    return fieldsContainer.getFieldDefinitions();
  }

  /**
   * Called to get a named field from an input object type
   *
   * @param fieldsContainer the type in play
   * @param fieldName       the name of the desired field
   * @return a {@link com.intellij.lang.jsgraphql.types.schema.GraphQLInputObjectField} or null if its not visible
   */
  default GraphQLInputObjectField getFieldDefinition(GraphQLInputFieldsContainer fieldsContainer, String fieldName) {
    return fieldsContainer.getFieldDefinition(fieldName);
  }
}
