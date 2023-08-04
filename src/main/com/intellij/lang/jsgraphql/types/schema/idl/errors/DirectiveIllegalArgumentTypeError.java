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
package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLIllegalDirectiveArgumentInspection;
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInspection;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Node;
import org.jetbrains.annotations.Nullable;

import static java.lang.String.format;

@Internal
public class DirectiveIllegalArgumentTypeError extends BaseError {

  public static final String DUPLICATED_KEYS_MESSAGE = "Argument value object keys [%s] appear more than once.";
  public static final String UNKNOWN_FIELDS_MESSAGE = "Fields ['%s'] not present in type '%s'.";
  public static final String EXPECTED_ENUM_MESSAGE = "Argument value is of type '%s', expected an enum value.";
  public static final String MUST_BE_VALID_ENUM_VALUE_MESSAGE = "Argument value '%s' doesn't match any of the allowed enum values ['%s']";
  public static final String EXPECTED_SCALAR_MESSAGE = "Argument value is of type '%s', expected a scalar.";
  public static final String NOT_A_VALID_SCALAR_LITERAL_MESSAGE = "Argument value is not a valid value of scalar '%s'.";
  public static final String MISSING_REQUIRED_FIELD_MESSAGE = "Missing required field '%s'.";
  public static final String EXPECTED_NON_NULL_MESSAGE = "Argument value is 'null', expected a non-null value.";
  public static final String EXPECTED_LIST_MESSAGE = "Argument value is '%s', expected a list value.";
  public static final String EXPECTED_OBJECT_MESSAGE = "Argument value is of type '%s', expected an Object value.";

  public DirectiveIllegalArgumentTypeError(Node element,
                                           String elementName,
                                           String directiveName,
                                           String argumentName,
                                           String detailedMessaged) {
    super(element, mkDirectiveIllegalArgumentTypeErrorMessage(element, elementName, directiveName, argumentName, detailedMessaged));
  }

  static String mkDirectiveIllegalArgumentTypeErrorMessage(Node element,
                                                           String elementName,
                                                           String directiveName,
                                                           String argumentName,
                                                           String detailedMessage) {
    return format("'%s' uses an illegal value for the argument '%s' on directive '%s'. %s",
                  elementName, argumentName, directiveName, detailedMessage);
  }

  @Override
  public @Nullable Class<? extends GraphQLInspection> getInspectionClass() {
    return GraphQLIllegalDirectiveArgumentInspection.class;
  }
}
