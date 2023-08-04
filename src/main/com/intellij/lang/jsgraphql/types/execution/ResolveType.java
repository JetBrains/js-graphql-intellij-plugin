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
import com.intellij.lang.jsgraphql.types.TypeResolutionEnvironment;
import com.intellij.lang.jsgraphql.types.schema.*;

import java.util.Map;

@Internal
public class ResolveType {


  public GraphQLObjectType resolveType(ExecutionContext executionContext,
                                       MergedField field,
                                       Object source,
                                       Map<String, Object> arguments,
                                       GraphQLType fieldType) {
    GraphQLObjectType resolvedType;
    if (fieldType instanceof GraphQLInterfaceType) {
      TypeResolutionParameters resolutionParams = TypeResolutionParameters.newParameters()
        .graphQLInterfaceType((GraphQLInterfaceType)fieldType)
        .field(field)
        .value(source)
        .argumentValues(arguments)
        .context(executionContext.getContext())
        .schema(executionContext.getGraphQLSchema()).build();
      resolvedType = resolveTypeForInterface(resolutionParams);
    }
    else if (fieldType instanceof GraphQLUnionType) {
      TypeResolutionParameters resolutionParams = TypeResolutionParameters.newParameters()
        .graphQLUnionType((GraphQLUnionType)fieldType)
        .field(field)
        .value(source)
        .argumentValues(arguments)
        .context(executionContext.getContext())
        .schema(executionContext.getGraphQLSchema()).build();
      resolvedType = resolveTypeForUnion(resolutionParams);
    }
    else {
      resolvedType = (GraphQLObjectType)fieldType;
    }

    return resolvedType;
  }

  public GraphQLObjectType resolveTypeForInterface(TypeResolutionParameters params) {
    TypeResolutionEnvironment env =
      new TypeResolutionEnvironment(params.getValue(), params.getArgumentValues(), params.getField(), params.getGraphQLInterfaceType(),
                                    params.getSchema(), params.getContext());
    GraphQLInterfaceType abstractType = params.getGraphQLInterfaceType();
    TypeResolver typeResolver = params.getSchema().getCodeRegistry().getTypeResolver(abstractType);
    GraphQLObjectType result = typeResolver.getType(env);
    if (result == null) {
      throw new UnresolvedTypeException(abstractType);
    }

    if (!params.getSchema().isPossibleType(abstractType, result)) {
      throw new UnresolvedTypeException(abstractType, result);
    }

    return result;
  }

  public GraphQLObjectType resolveTypeForUnion(TypeResolutionParameters params) {
    TypeResolutionEnvironment env =
      new TypeResolutionEnvironment(params.getValue(), params.getArgumentValues(), params.getField(), params.getGraphQLUnionType(),
                                    params.getSchema(), params.getContext());
    GraphQLUnionType abstractType = params.getGraphQLUnionType();
    TypeResolver typeResolver = params.getSchema().getCodeRegistry().getTypeResolver(abstractType);
    GraphQLObjectType result = typeResolver.getType(env);
    if (result == null) {
      throw new UnresolvedTypeException(abstractType);
    }

    if (!params.getSchema().isPossibleType(abstractType, result)) {
      throw new UnresolvedTypeException(abstractType, result);
    }

    return result;
  }
}
