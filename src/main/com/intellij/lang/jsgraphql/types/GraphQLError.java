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


import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInspection;
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLSchemaValidationInspection;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The interface describing graphql errors
 * <p>
 * NOTE: This class implements {@link Serializable} and hence it can be serialised and placed into a distributed cache.  However we
 * are not aiming to provide long term compatibility and do not intend for you to place this serialised data into permanent storage,
 * with times frames that cross graphql-java versions.  While we don't change things unnecessarily,  we may inadvertently break
 * the serialised compatibility across versions.
 *
 * @see <a href="https://facebook.github.io/graphql/#sec-Errors">GraphQL Spec - 7.2.2 Errors</a>
 */
@SuppressWarnings("rawtypes")
@PublicApi
public interface GraphQLError extends Serializable {

  /**
   * @return a description of the error intended for the developer as a guide to understand and correct the error
   */
  String getMessage();

  /**
   * @return the location(s) within the GraphQL document at which the error occurred. Each {@link SourceLocation}
   * describes the beginning of an associated syntax element
   */
  List<SourceLocation> getLocations();

  /**
   * @return an object classifying this error
   */
  ErrorClassification getErrorType();

  /**
   * The graphql spec says that the (optional) path field of any error should be a list
   * of path entries - http://facebook.github.io/graphql/#sec-Errors
   *
   * @return the path in list format
   */
  default List<Object> getPath() {
    return null;
  }

  /**
   * The graphql specification says that result of a call should be a map that follows certain rules on what items
   * should be present.  Certain JSON serializers may or may interpret the error to spec, so this method
   * is provided to produce a map that strictly follows the specification.
   * <p>
   * See : <a href="http://facebook.github.io/graphql/#sec-Errors">http://facebook.github.io/graphql/#sec-Errors</a>
   *
   * @return a map of the error that strictly follows the specification
   */
  default Map<String, Object> toSpecification() {
    return GraphqlErrorHelper.toSpecification(this);
  }

  /**
   * @return a map of error extensions or null if there are none
   */
  default Map<String, Object> getExtensions() {
    return null;
  }

  default @Nullable Node getNode() {
    return null;
  }

  default @NotNull List<Node> getReferences() {
    return Collections.emptyList();
  }

  default @Nullable Class<? extends GraphQLInspection> getInspectionClass() {
    return GraphQLSchemaValidationInspection.class;
  }
}
