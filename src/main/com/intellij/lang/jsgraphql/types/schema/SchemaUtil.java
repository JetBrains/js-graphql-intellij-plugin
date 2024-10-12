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
package com.intellij.lang.jsgraphql.types.schema;


import com.google.common.collect.ImmutableMap;
import com.intellij.lang.jsgraphql.types.Internal;

import java.util.*;

@Internal
public class SchemaUtil {
  ImmutableMap<String, GraphQLNamedType> allTypes(final GraphQLSchema schema,
                                                  final Set<GraphQLType> additionalTypes,
                                                  boolean afterTransform) {
    List<GraphQLSchemaElement> roots = new ArrayList<>();
    if (schema.isQueryDefined()) {
      roots.add(schema.getQueryType());
    }

    if (schema.isSupportingMutations()) {
      roots.add(schema.getMutationType());
    }

    if (schema.isSupportingSubscriptions()) {
      roots.add(schema.getSubscriptionType());
    }

    if (additionalTypes != null) {
      roots.addAll(additionalTypes);
    }

    if (schema.getDirectives() != null) {
      roots.addAll(schema.getDirectives());
    }

    GraphQLTypeCollectingVisitor visitor = new GraphQLTypeCollectingVisitor();
    SchemaTraverser traverser;
    // when collecting all types we never want to follow type references
    // When a schema is build first the type references are not replaced, so
    // this is not a problem. But when a schema is transformed,
    // the type references are actually replaced so we need to make sure we
    // use the original type references
    if (afterTransform) {
      traverser = new SchemaTraverser(schemaElement -> schemaElement.getChildrenWithTypeReferences().getChildrenAsList());
    }
    else {
      traverser = new SchemaTraverser();
    }
    traverser.depthFirst(visitor, roots);
    Map<String, GraphQLNamedType> result = visitor.getResult();
    return ImmutableMap.copyOf(new TreeMap<>(result));
  }


  /*
   * Indexes GraphQLObject types registered with the provided schema by implemented GraphQLInterface name
   *
   * This helps in accelerates/simplifies collecting types that implement a certain interface
   *
   * Provided to replace {@link #findImplementations(com.intellij.lang.jsgraphql.types.schema.GraphQLSchema, com.intellij.lang.jsgraphql.types.schema.GraphQLInterfaceType)}
   *
   */
  Map<String, List<GraphQLObjectType>> groupImplementations(GraphQLSchema schema) {
    Map<String, List<GraphQLObjectType>> result = new LinkedHashMap<>();
    for (GraphQLType type : schema.getAllTypesAsList()) {
      if (type instanceof GraphQLObjectType) {
        List<GraphQLNamedOutputType> interfaces = ((GraphQLObjectType)type).getInterfaces();
        for (GraphQLNamedOutputType interfaceType : interfaces) {
          List<GraphQLObjectType> myGroup = result.computeIfAbsent(interfaceType.getName(), k -> new ArrayList<>());
          myGroup.add((GraphQLObjectType)type);
        }
      }
    }
    return ImmutableMap.copyOf(new TreeMap<>(result));
  }

  public Map<String, List<GraphQLImplementingType>> groupImplementationsForInterfacesAndObjects(GraphQLSchema schema) {
    Map<String, List<GraphQLImplementingType>> result = new LinkedHashMap<>();
    for (GraphQLType type : schema.getAllTypesAsList()) {
      if (type instanceof GraphQLImplementingType) {
        List<GraphQLNamedOutputType> interfaces = ((GraphQLImplementingType)type).getInterfaces();
        for (GraphQLNamedOutputType interfaceType : interfaces) {
          List<GraphQLImplementingType> myGroup = result.computeIfAbsent(interfaceType.getName(), k -> new ArrayList<>());
          myGroup.add((GraphQLImplementingType)type);
        }
      }
    }
    return ImmutableMap.copyOf(new TreeMap<>(result));
  }

  /**
   * This method is deprecated due to a performance concern.
   * <p>
   * The Algorithm complexity: O(n^2), where n is number of registered GraphQLTypes
   * <p>
   * That indexing operation is performed twice per input document:
   * 1. during validation
   * 2. during execution
   * <p>
   * We now indexed all types at the schema creation, which has brought complexity down to O(1)
   *
   * @param schema        GraphQL schema
   * @param interfaceType an interface type to find implementations for
   * @return List of object types implementing provided interface
   * @deprecated use {@link com.intellij.lang.jsgraphql.types.schema.GraphQLSchema#getImplementations(GraphQLInterfaceType)} instead
   */
  @Deprecated(forRemoval = true)
  public List<GraphQLObjectType> findImplementations(GraphQLSchema schema, GraphQLInterfaceType interfaceType) {
    List<GraphQLObjectType> result = new ArrayList<>();
    for (GraphQLType type : schema.getAllTypesAsList()) {
      if (!(type instanceof GraphQLObjectType objectType)) {
        continue;
      }
      if ((objectType).getInterfaces().contains(interfaceType)) {
        result.add(objectType);
      }
    }
    return result;
  }

  void replaceTypeReferences(GraphQLSchema schema) {
    final Map<String, GraphQLNamedType> typeMap = schema.getTypeMap();
    List<GraphQLSchemaElement> roots = new ArrayList<>(typeMap.values());
    roots.addAll(schema.getDirectives());
    SchemaTraverser schemaTraverser =
      new SchemaTraverser(schemaElement -> schemaElement.getChildrenWithTypeReferences().getChildrenAsList());
    schemaTraverser.depthFirst(new GraphQLTypeResolvingVisitor(typeMap), roots);
  }
}
