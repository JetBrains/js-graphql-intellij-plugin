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

import com.intellij.lang.jsgraphql.types.ExecutionResult;
import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

@PublicApi
public class FieldValueInfo {

  public enum CompleteValueType {
    OBJECT,
    LIST,
    NULL,
    SCALAR,
    ENUM
  }

  private final CompleteValueType completeValueType;
  private final CompletableFuture<ExecutionResult> fieldValue;
  private final List<FieldValueInfo> fieldValueInfos;

  private FieldValueInfo(CompleteValueType completeValueType,
                         CompletableFuture<ExecutionResult> fieldValue,
                         List<FieldValueInfo> fieldValueInfos) {
    assertNotNull(fieldValueInfos, () -> "fieldValueInfos can't be null");
    this.completeValueType = completeValueType;
    this.fieldValue = fieldValue;
    this.fieldValueInfos = fieldValueInfos;
  }

  public CompleteValueType getCompleteValueType() {
    return completeValueType;
  }

  public CompletableFuture<ExecutionResult> getFieldValue() {
    return fieldValue;
  }

  public List<FieldValueInfo> getFieldValueInfos() {
    return fieldValueInfos;
  }

  public static Builder newFieldValueInfo(CompleteValueType completeValueType) {
    return new Builder(completeValueType);
  }

  @Override
  public String toString() {
    return "FieldValueInfo{" +
           "completeValueType=" + completeValueType +
           ", fieldValue=" + fieldValue +
           ", fieldValueInfos=" + fieldValueInfos +
           '}';
  }

  @SuppressWarnings("unused")
  public static class Builder {
    private CompleteValueType completeValueType;
    private CompletableFuture<ExecutionResult> executionResultFuture;
    private List<FieldValueInfo> listInfos = new ArrayList<>();

    public Builder(CompleteValueType completeValueType) {
      this.completeValueType = completeValueType;
    }

    public Builder completeValueType(CompleteValueType completeValueType) {
      this.completeValueType = completeValueType;
      return this;
    }

    public Builder fieldValue(CompletableFuture<ExecutionResult> executionResultFuture) {
      this.executionResultFuture = executionResultFuture;
      return this;
    }

    public Builder fieldValueInfos(List<FieldValueInfo> listInfos) {
      assertNotNull(listInfos, () -> "fieldValueInfos can't be null");
      this.listInfos = listInfos;
      return this;
    }

    public FieldValueInfo build() {
      return new FieldValueInfo(completeValueType, executionResultFuture, listInfos);
    }
  }
}
