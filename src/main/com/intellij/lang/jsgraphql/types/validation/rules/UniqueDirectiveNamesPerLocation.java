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

import com.intellij.lang.jsgraphql.types.DirectivesUtil;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.GraphQLDirective;
import com.intellij.lang.jsgraphql.types.validation.AbstractRule;
import com.intellij.lang.jsgraphql.types.validation.ValidationContext;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorCollector;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;

/**
 * https://facebook.github.io/graphql/June2018/#sec-Directives-Are-Unique-Per-Location
 */
@Internal
public class UniqueDirectiveNamesPerLocation extends AbstractRule {

  public UniqueDirectiveNamesPerLocation(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
    super(validationContext, validationErrorCollector);
  }

  @Override
  public void checkInlineFragment(InlineFragment inlineFragment) {
    checkDirectivesUniqueness(inlineFragment, inlineFragment.getDirectives());
  }

  @Override
  public void checkFragmentDefinition(FragmentDefinition fragmentDefinition) {
    checkDirectivesUniqueness(fragmentDefinition, fragmentDefinition.getDirectives());
  }

  @Override
  public void checkFragmentSpread(FragmentSpread fragmentSpread) {
    checkDirectivesUniqueness(fragmentSpread, fragmentSpread.getDirectives());
  }

  @Override
  public void checkField(Field field) {
    checkDirectivesUniqueness(field, field.getDirectives());
  }

  @Override
  public void checkOperationDefinition(OperationDefinition operationDefinition) {
    checkDirectivesUniqueness(operationDefinition, operationDefinition.getDirectives());
  }

  private void checkDirectivesUniqueness(Node<?> directivesContainer, List<Directive> directives) {
    Set<String> directiveNames = new LinkedHashSet<>();
    for (Directive directive : directives) {
      String name = directive.getName();
      Map<String, List<GraphQLDirective>> directivesByName = getValidationContext().getSchema().getAllDirectivesByName();
      boolean nonRepeatable = DirectivesUtil.isAllNonRepeatable(directivesByName.getOrDefault(name, emptyList()));
      if (directiveNames.contains(name) && nonRepeatable) {
        addError(ValidationErrorType.DuplicateDirectiveName,
                 directive.getSourceLocation(),
                 duplicateDirectiveNameMessage(name, directivesContainer.getClass().getSimpleName()));
      }
      else {
        directiveNames.add(name);
      }
    }
  }

  private String duplicateDirectiveNameMessage(String directiveName, String location) {
    return String.format(
      "Non repeatable directives must be uniquely named within a location. The directive '%s' used on a '%s' is not unique.", directiveName,
      location);
  }
}
