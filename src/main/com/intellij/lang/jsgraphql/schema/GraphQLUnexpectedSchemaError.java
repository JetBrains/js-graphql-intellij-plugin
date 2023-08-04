/*
 * Copyright (c) 2019-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.intellij.lang.jsgraphql.types.ErrorType;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;

import java.util.Collections;
import java.util.List;

/**
 * Exception thrown due to an error that is likely to be internal in the schema building logic
 */
public class GraphQLUnexpectedSchemaError implements GraphQLError {

  private Exception exception;

  public GraphQLUnexpectedSchemaError(Exception exception) {
    this.exception = exception;
  }

  public Exception getException() {
    return exception;
  }

  @Override
  public String getMessage() {
    // strip out graphql-java internals part of the exception message given that the schema can actually be broken/invalid as the editor changes
    return exception.getMessage().replace("Internal error: should never happen: ", "");
  }

  @Override
  public List<SourceLocation> getLocations() {
    return Collections.emptyList();
  }

  @Override
  public ErrorType getErrorType() {
    return ErrorType.ValidationError;
  }
}
