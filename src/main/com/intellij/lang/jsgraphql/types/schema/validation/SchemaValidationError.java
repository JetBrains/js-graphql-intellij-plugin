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
package com.intellij.lang.jsgraphql.types.schema.validation;

import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLEmptyTypeInspection;
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLIllegalNameInspection;
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInspection;
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInterfaceImplementationInspection;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Node;
import com.intellij.lang.jsgraphql.types.schema.idl.errors.BaseError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Internal
public class SchemaValidationError extends BaseError {

  private final SchemaValidationErrorType errorType;
  private final String description;

  public SchemaValidationError(@NotNull SchemaValidationErrorType errorType,
                               @NotNull String description,
                               @Nullable Node node) {
    super(node, description);
    this.errorType = errorType;
    this.description = description;
  }

  public SchemaValidationErrorType getValidationErrorType() {
    return errorType;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + errorType.hashCode();
    result = 31 * result + description.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof SchemaValidationError that)) {
      return false;
    }
    return this.errorType.equals(that.errorType) && this.description.equals(that.description);
  }

  @Override
  public @Nullable Class<? extends GraphQLInspection> getInspectionClass() {
    return switch (errorType) {
      case InvalidCustomizedNameError -> GraphQLIllegalNameInspection.class;
      case EnumLackOfValueError, InputObjectTypeLackOfFieldError, ImplementingTypeLackOfFieldError -> GraphQLEmptyTypeInspection.class;
      case ObjectDoesNotImplementItsInterfaces -> GraphQLInterfaceImplementationInspection.class;
      default -> super.getInspectionClass();
    };
  }
}
