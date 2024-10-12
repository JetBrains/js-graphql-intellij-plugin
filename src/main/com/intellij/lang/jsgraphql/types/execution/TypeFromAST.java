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


import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.ListType;
import com.intellij.lang.jsgraphql.types.language.NonNullType;
import com.intellij.lang.jsgraphql.types.language.Type;
import com.intellij.lang.jsgraphql.types.language.TypeName;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;

import static com.intellij.lang.jsgraphql.types.schema.GraphQLList.list;
import static com.intellij.lang.jsgraphql.types.schema.GraphQLNonNull.nonNull;

@Internal
public final class TypeFromAST {
  public static GraphQLType getTypeFromAST(GraphQLSchema schema, Type type) {
    GraphQLType innerType;
    if (type instanceof ListType) {
      innerType = getTypeFromAST(schema, ((ListType)type).getType());
      return innerType != null ? list(innerType) : null;
    }
    else if (type instanceof NonNullType) {
      innerType = getTypeFromAST(schema, ((NonNullType)type).getType());
      return innerType != null ? nonNull(innerType) : null;
    }

    return schema.getType(((TypeName)type).getName());
  }
}
