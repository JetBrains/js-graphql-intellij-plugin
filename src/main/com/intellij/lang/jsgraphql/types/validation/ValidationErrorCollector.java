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


import com.intellij.lang.jsgraphql.types.Internal;

import java.util.ArrayList;
import java.util.List;

@Internal
public class ValidationErrorCollector {

  private final List<ValidationError> errors = new ArrayList<>();

  public void addError(ValidationError validationError) {
    this.errors.add(validationError);
  }

  public List<ValidationError> getErrors() {
    return errors;
  }

  public boolean containsValidationError(ValidationErrorType validationErrorType) {
    return containsValidationError(validationErrorType, null);
  }

  public boolean containsValidationError(ValidationErrorType validationErrorType, String description) {
    for (ValidationError validationError : errors) {
      if (validationError.getValidationErrorType() == validationErrorType) {
        return description == null || validationError.getDescription().equals(description);
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "ValidationErrorCollector{" +
           "errors=" + errors +
           '}';
  }
}
