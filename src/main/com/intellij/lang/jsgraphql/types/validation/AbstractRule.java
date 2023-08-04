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
import com.intellij.lang.jsgraphql.types.language.*;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.lang.jsgraphql.types.validation.ValidationError.newValidationError;

@Internal
public class AbstractRule {

  private final ValidationContext validationContext;
  private final ValidationErrorCollector validationErrorCollector;


  private boolean visitFragmentSpreads;

  private ValidationUtil validationUtil = new ValidationUtil();

  public AbstractRule(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
    this.validationContext = validationContext;
    this.validationErrorCollector = validationErrorCollector;
  }

  public boolean isVisitFragmentSpreads() {
    return visitFragmentSpreads;
  }

  public void setVisitFragmentSpreads(boolean visitFragmentSpreads) {
    this.visitFragmentSpreads = visitFragmentSpreads;
  }


  public ValidationUtil getValidationUtil() {
    return validationUtil;
  }

  public void addError(ValidationErrorType validationErrorType, List<? extends Node<?>> locations, String description) {
    List<SourceLocation> locationList = new ArrayList<>();
    for (Node<?> node : locations) {
      locationList.add(node.getSourceLocation());
    }
    addError(newValidationError()
               .validationErrorType(validationErrorType)
               .sourceLocations(locationList)
               .description(description));
  }

  public void addError(ValidationErrorType validationErrorType, SourceLocation location, String description) {
    addError(newValidationError()
               .validationErrorType(validationErrorType)
               .sourceLocation(location)
               .description(description));
  }

  public void addError(ValidationError.Builder validationError) {
    validationErrorCollector.addError(validationError.queryPath(getQueryPath()).build());
  }

  public List<ValidationError> getErrors() {
    return validationErrorCollector.getErrors();
  }


  public ValidationContext getValidationContext() {
    return validationContext;
  }

  public ValidationErrorCollector getValidationErrorCollector() {
    return validationErrorCollector;
  }

  protected List<String> getQueryPath() {
    return validationContext.getQueryPath();
  }

  public void checkDocument(Document document) {

  }

  public void checkArgument(Argument argument) {

  }

  public void checkTypeName(TypeName typeName) {

  }

  public void checkVariableDefinition(VariableDefinition variableDefinition) {

  }

  public void checkField(Field field) {

  }

  public void checkInlineFragment(InlineFragment inlineFragment) {

  }

  public void checkDirective(Directive directive, List<Node> ancestors) {

  }

  public void checkFragmentSpread(FragmentSpread fragmentSpread) {

  }

  public void checkFragmentDefinition(FragmentDefinition fragmentDefinition) {

  }

  public void checkOperationDefinition(OperationDefinition operationDefinition) {

  }

  public void leaveOperationDefinition(OperationDefinition operationDefinition) {

  }

  public void checkSelectionSet(SelectionSet selectionSet) {

  }

  public void leaveSelectionSet(SelectionSet selectionSet) {

  }

  public void checkVariable(VariableReference variableReference) {

  }

  public void documentFinished(Document document) {

  }

  @Override
  public String toString() {
    return "Rule{" + validationContext + "}";
  }
}
