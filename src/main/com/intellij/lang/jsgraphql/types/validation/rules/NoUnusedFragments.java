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
package com.intellij.lang.jsgraphql.types.validation.rules;


import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Document;
import com.intellij.lang.jsgraphql.types.language.FragmentDefinition;
import com.intellij.lang.jsgraphql.types.language.FragmentSpread;
import com.intellij.lang.jsgraphql.types.language.OperationDefinition;
import com.intellij.lang.jsgraphql.types.validation.AbstractRule;
import com.intellij.lang.jsgraphql.types.validation.ValidationContext;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorCollector;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Internal
public class NoUnusedFragments extends AbstractRule {


  private final List<FragmentDefinition> allDeclaredFragments = new ArrayList<>();

  private List<String> usedFragments = new ArrayList<>();
  private final Map<String, List<String>> spreadsInDefinition = new LinkedHashMap<>();
  private final List<List<String>> fragmentsUsedDirectlyInOperation = new ArrayList<>();

  public NoUnusedFragments(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
    super(validationContext, validationErrorCollector);
  }

  @Override
  public void checkOperationDefinition(OperationDefinition operationDefinition) {
    usedFragments = new ArrayList<>();
    fragmentsUsedDirectlyInOperation.add(usedFragments);
  }


  @Override
  public void checkFragmentSpread(FragmentSpread fragmentSpread) {
    usedFragments.add(fragmentSpread.getName());
  }

  @Override
  public void checkFragmentDefinition(FragmentDefinition fragmentDefinition) {
    allDeclaredFragments.add(fragmentDefinition);
    usedFragments = new ArrayList<>();
    spreadsInDefinition.put(fragmentDefinition.getName(), usedFragments);
  }

  @Override
  public void documentFinished(Document document) {

    List<String> allUsedFragments = new ArrayList<>();
    for (List<String> fragmentsInOneOperation : fragmentsUsedDirectlyInOperation) {
      for (String fragment : fragmentsInOneOperation) {
        collectUsedFragmentsInDefinition(allUsedFragments, fragment);
      }
    }

    for (FragmentDefinition fragmentDefinition : allDeclaredFragments) {
      if (!allUsedFragments.contains(fragmentDefinition.getName())) {
        String message = String.format("Unused fragment '%s'", fragmentDefinition.getName());
        addError(ValidationErrorType.UnusedFragment, fragmentDefinition.getSourceLocation(), message);
      }
    }
  }

  private void collectUsedFragmentsInDefinition(List<String> result, String fragmentName) {
    if (result.contains(fragmentName)) return;
    result.add(fragmentName);
    List<String> spreadList = spreadsInDefinition.get(fragmentName);
    if (spreadList == null) {
      return;
    }
    for (String fragment : spreadList) {
      collectUsedFragmentsInDefinition(result, fragment);
    }
  }
}
