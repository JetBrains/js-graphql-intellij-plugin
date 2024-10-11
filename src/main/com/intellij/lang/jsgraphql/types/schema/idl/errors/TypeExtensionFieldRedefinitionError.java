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

import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInspection;
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLMemberRedefinitionInspection;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.AbstractNode;
import com.intellij.lang.jsgraphql.types.language.FieldDefinition;
import com.intellij.lang.jsgraphql.types.language.InputValueDefinition;
import com.intellij.lang.jsgraphql.types.language.TypeDefinition;
import org.jetbrains.annotations.Nullable;

import static java.lang.String.format;

@Internal
public class TypeExtensionFieldRedefinitionError extends BaseError {

  public TypeExtensionFieldRedefinitionError(TypeDefinition typeDefinition,
                                             FieldDefinition fieldDefinition) {
    super(typeDefinition,
          formatMessage(typeDefinition, fieldDefinition.getName(), fieldDefinition));
  }

  public TypeExtensionFieldRedefinitionError(TypeDefinition typeDefinition,
                                             InputValueDefinition fieldDefinition) {
    super(typeDefinition,
          formatMessage(typeDefinition, fieldDefinition.getName(), fieldDefinition));
  }

  private static String formatMessage(TypeDefinition typeDefinition, String fieldName, AbstractNode<?> fieldDefinition) {
    return format("'%s' extension type tried to redefine field '%s'",
                  typeDefinition.getName(), fieldName
    );
  }

  @Override
  public @Nullable Class<? extends GraphQLInspection> getInspectionClass() {
    return GraphQLMemberRedefinitionInspection.class;
  }
}
