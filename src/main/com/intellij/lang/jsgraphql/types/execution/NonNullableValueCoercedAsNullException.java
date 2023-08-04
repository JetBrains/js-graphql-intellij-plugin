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
package com.intellij.lang.jsgraphql.types.execution;

import com.intellij.lang.jsgraphql.types.ErrorType;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.GraphQLException;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;
import com.intellij.lang.jsgraphql.types.language.VariableDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLInputObjectField;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil;

import java.util.Collections;
import java.util.List;

import static java.lang.String.format;

/**
 * This is thrown if a non nullable value is coerced to a null value
 */
@PublicApi
public class NonNullableValueCoercedAsNullException extends GraphQLException implements GraphQLError {
  private List<SourceLocation> sourceLocations;

  public NonNullableValueCoercedAsNullException(VariableDefinition variableDefinition, GraphQLType graphQLType) {
    super(format("Variable '%s' has coerced Null value for NonNull type '%s'",
                 variableDefinition.getName(), GraphQLTypeUtil.simplePrint(graphQLType)));
    this.sourceLocations = Collections.singletonList(variableDefinition.getSourceLocation());
  }

  public NonNullableValueCoercedAsNullException(VariableDefinition variableDefinition, String fieldName, GraphQLType graphQLType) {
    super(format("Field '%s' of variable '%s' has coerced Null value for NonNull type '%s'",
                 fieldName, variableDefinition.getName(), GraphQLTypeUtil.simplePrint(graphQLType)));
    this.sourceLocations = Collections.singletonList(variableDefinition.getSourceLocation());
  }

  public NonNullableValueCoercedAsNullException(GraphQLInputObjectField inputTypeField) {
    super(format("Input field '%s' has coerced Null value for NonNull type '%s'",
                 inputTypeField.getName(), GraphQLTypeUtil.simplePrint(inputTypeField.getType())));
  }

  @Override
  public List<SourceLocation> getLocations() {
    return sourceLocations;
  }

  @Override
  public ErrorType getErrorType() {
    return ErrorType.ValidationError;
  }
}
