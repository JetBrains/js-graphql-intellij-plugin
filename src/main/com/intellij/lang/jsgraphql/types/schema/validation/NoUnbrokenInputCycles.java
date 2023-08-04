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

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.schema.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil.*;

/**
 * Schema validation rule ensuring no input type forms an unbroken non-nullable recursion,
 * as such a type would be impossible to satisfy
 */
@Internal
public class NoUnbrokenInputCycles implements SchemaValidationRule {

  @Override
  public void check(GraphQLType type, SchemaValidationErrorCollector validationErrorCollector) {
  }

  @Override
  public void check(GraphQLSchema graphQLSchema, SchemaValidationErrorCollector validationErrorCollector) {
  }

  @Override
  public void check(GraphQLFieldDefinition fieldDef, SchemaValidationErrorCollector validationErrorCollector) {
    for (GraphQLArgument argument : fieldDef.getArguments()) {
      GraphQLInputType argumentType = argument.getType();
      if (argumentType instanceof GraphQLInputObjectType) {
        List<String> path = new ArrayList<>();
        //                path.add(argumentType.getName());
        check((GraphQLInputObjectType)argumentType, new LinkedHashSet<>(), path, validationErrorCollector);
      }
    }
  }

  private void check(GraphQLInputObjectType type,
                     Set<GraphQLType> seen,
                     List<String> path,
                     SchemaValidationErrorCollector validationErrorCollector) {
    if (seen.contains(type)) {
      validationErrorCollector.addError(
        new SchemaValidationError(SchemaValidationErrorType.UnbrokenInputCycle, getErrorMessage(path), type.getDefinition()));
      return;
    }
    seen.add(type);

    for (GraphQLInputObjectField field : type.getFieldDefinitions()) {
      if (isNonNull(field.getType())) {
        GraphQLType unwrapped = unwrapNonNull((GraphQLNonNull)field.getType());
        if (unwrapped instanceof GraphQLInputObjectType) {
          path = new ArrayList<>(path);
          path.add(field.getName() + "!");
          check((GraphQLInputObjectType)unwrapped, new LinkedHashSet<>(seen), path, validationErrorCollector);
        }
      }
    }
  }

  private GraphQLType unwrapNonNull(GraphQLNonNull type) {
    if (isList(type.getWrappedType())) {
      //we only care about [type!]! i.e. non-null lists of non-nulls
      GraphQLList listType = (GraphQLList)type.getWrappedType();
      if (isNonNull(listType.getWrappedType())) {
        return unwrapAll(listType.getWrappedType());
      }
      else {
        return type.getWrappedType();
      }
    }
    else {
      return unwrapAll(type.getWrappedType());
    }
  }

  private String getErrorMessage(List<String> path) {
    StringBuilder message = new StringBuilder();
    message.append("[");
    for (int i = 0; i < path.size(); i++) {
      if (i != 0) {
        message.append(".");
      }
      message.append(path.get(i));
    }
    message.append("] forms an unsatisfiable cycle");
    return message.toString();
  }
}
