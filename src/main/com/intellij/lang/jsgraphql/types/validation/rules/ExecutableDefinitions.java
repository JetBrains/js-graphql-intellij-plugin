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
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.validation.AbstractRule;
import com.intellij.lang.jsgraphql.types.validation.ValidationContext;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorCollector;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;

@Internal
public class ExecutableDefinitions extends AbstractRule {

  public ExecutableDefinitions(ValidationContext validationContext, ValidationErrorCollector validationErrorCollector) {
    super(validationContext, validationErrorCollector);
  }

  /**
   * Executable definitions
   * <p>
   * A GraphQL document is only valid for execution if all definitions are either
   * operation or fragment definitions.
   */
  @Override
  public void checkDocument(Document document) {
    document.getDefinitions().forEach(definition -> {
      if (!(definition instanceof OperationDefinition)
          && !(definition instanceof FragmentDefinition)) {

        String message = nonExecutableDefinitionMessage(definition);
        addError(ValidationErrorType.NonExecutableDefinition, definition.getSourceLocation(), message);
      }
    });
  }

  private String nonExecutableDefinitionMessage(Definition definition) {

    String definitionName;
    if (definition instanceof TypeDefinition) {
      definitionName = ((TypeDefinition)definition).getName();
    }
    else if (definition instanceof SchemaDefinition) {
      definitionName = "schema";
    }
    else {
      definitionName = "provided";
    }

    return nonExecutableDefinitionMessage(definitionName);
  }

  static String nonExecutableDefinitionMessage(String definitionName) {
    return String.format("The %s definition is not executable.", definitionName);
  }
}
