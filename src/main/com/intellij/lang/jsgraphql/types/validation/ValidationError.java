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
package com.intellij.lang.jsgraphql.types.validation;


import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInspection;
import com.intellij.lang.jsgraphql.types.ErrorType;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.GraphqlErrorHelper;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@PublicApi
public class ValidationError implements GraphQLError {

  private final String message;
  private final List<SourceLocation> locations = new ArrayList<>();
  private final String description;
  private final ValidationErrorType validationErrorType;
  private final List<String> queryPath;
  private final Map<String, Object> extensions;
  private final Node node;
  private final Class<? extends GraphQLInspection> inspectionClass;

  private ValidationError(Builder builder) {
    this.validationErrorType = builder.validationErrorType;
    if (builder.sourceLocations != null) {
      this.locations.addAll(builder.sourceLocations);
    }
    this.description = builder.description;
    this.message = mkMessage(builder.validationErrorType, builder.description, builder.queryPath);
    this.queryPath = builder.queryPath;
    this.extensions = builder.extensions;
    this.node = builder.node;
    this.inspectionClass = builder.inspectionClass;
  }

  private String mkMessage(ValidationErrorType validationErrorType, String description, List<String> queryPath) {
    return String.format("Validation error of type %s: %s%s", validationErrorType, description, toPath(queryPath));
  }

  private String toPath(List<String> queryPath) {
    if (queryPath == null) {
      return "";
    }
    return String.format(" @ '%s'", String.join("/", queryPath));
  }

  public ValidationErrorType getValidationErrorType() {
    return validationErrorType;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public List<SourceLocation> getLocations() {
    return locations;
  }

  @Override
  public ErrorType getErrorType() {
    return ErrorType.ValidationError;
  }

  public List<String> getQueryPath() {
    return queryPath;
  }

  @Override
  public Map<String, Object> getExtensions() {
    return extensions;
  }

  @Override
  public @Nullable Node getNode() {
    return this.node;
  }

  @Override
  public @Nullable Class<? extends GraphQLInspection> getInspectionClass() {
    return ObjectUtils.coalesce(inspectionClass, GraphQLError.super.getInspectionClass());
  }

  @Override
  public String toString() {
    return "ValidationError{" +
           "validationErrorType=" + validationErrorType +
           ", queryPath=" + queryPath +
           ", message=" + message +
           ", locations=" + locations +
           ", description='" + description + '\'' +
           '}';
  }

  @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
  @Override
  public boolean equals(Object o) {
    return GraphqlErrorHelper.equals(this, o);
  }

  @Override
  public int hashCode() {
    return GraphqlErrorHelper.hashCode(this);
  }


  public static Builder newValidationError() {
    return new Builder();
  }

  public static class Builder {
    private List<SourceLocation> sourceLocations;
    private Map<String, Object> extensions;
    private String description;
    private ValidationErrorType validationErrorType;
    private List<String> queryPath;
    private Node node;
    private Class<? extends GraphQLInspection> inspectionClass;


    public Builder validationErrorType(ValidationErrorType validationErrorType) {
      this.validationErrorType = validationErrorType;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder queryPath(List<String> queryPath) {
      this.queryPath = queryPath;
      return this;
    }

    public Builder sourceLocation(SourceLocation sourceLocation) {
      this.sourceLocations = sourceLocation == null ? null : Collections.singletonList(sourceLocation);
      return this;
    }

    public Builder sourceLocations(List<SourceLocation> sourceLocations) {
      this.sourceLocations = sourceLocations;
      return this;
    }

    public Builder extensions(Map<String, Object> extensions) {
      this.extensions = extensions;
      return this;
    }

    public Builder node(@Nullable Node node) {
      this.node = node;
      return this;
    }

    public Builder inspectionClass(@NotNull Class<? extends GraphQLInspection> inspectionClass) {
      this.inspectionClass = inspectionClass;
      return this;
    }

    public ValidationError build() {
      return new ValidationError(this);
    }
  }
}
