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

import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.introspection.Introspection;
import com.intellij.lang.jsgraphql.types.util.*;

import java.util.*;
import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.types.Assert.*;
import static com.intellij.lang.jsgraphql.types.schema.GraphQLSchemaElementAdapter.SCHEMA_ELEMENT_ADAPTER;
import static com.intellij.lang.jsgraphql.types.schema.SchemaElementChildrenContainer.newSchemaElementChildrenContainer;
import static com.intellij.lang.jsgraphql.types.util.NodeZipper.ModificationType.REPLACE;
import static com.intellij.lang.jsgraphql.types.util.TraversalControl.CONTINUE;
import static java.lang.String.format;

/**
 * Transforms a {@link GraphQLSchema} object.
 */
@PublicApi
public class SchemaTransformer {

  // artificial schema element which serves as root element for the transformation
  private static class DummyRoot implements GraphQLSchemaElement {

    static final String QUERY = "query";
    static final String MUTATION = "mutation";
    static final String SUBSCRIPTION = "subscription";
    static final String ADD_TYPES = "addTypes";
    static final String DIRECTIVES = "directives";
    static final String SCHEMA_DIRECTIVES = "schemaDirectives";
    static final String INTROSPECTION = "introspection";
    static final String SCHEMA_ELEMENT = "schemaElement";

    GraphQLSchema schema;
    GraphQLObjectType query;
    GraphQLObjectType mutation;
    GraphQLObjectType subscription;
    Set<GraphQLType> additionalTypes;
    Set<GraphQLDirective> directives;
    Set<GraphQLDirective> schemaDirectives;
    GraphQLSchemaElement schemaElement;

    DummyRoot(GraphQLSchema schema) {
      this.schema = schema;
      query = schema == null ? null : schema.getQueryType();
      mutation = schema == null ? null : schema.isSupportingMutations() ? schema.getMutationType() : null;
      subscription = schema == null ? null : schema.isSupportingSubscriptions() ? schema.getSubscriptionType() : null;
      additionalTypes = schema == null ? null : schema.getAdditionalTypes();
      schemaDirectives = new LinkedHashSet<>(schema.getSchemaDirectives());
      directives = new LinkedHashSet<>(schema.getDirectives());
    }

    DummyRoot(GraphQLSchemaElement schemaElement) {
      this.schemaElement = schemaElement;
    }


    @Override
    public List<GraphQLSchemaElement> getChildren() {
      return assertShouldNeverHappen();
    }

    @Override
    public SchemaElementChildrenContainer getChildrenWithTypeReferences() {
      SchemaElementChildrenContainer.Builder builder = newSchemaElementChildrenContainer();
      if (schemaElement != null) {
        builder.child(SCHEMA_ELEMENT, schemaElement);
      }
      else {
        builder.child(QUERY, query);
        if (schema.isSupportingMutations()) {
          builder.child(MUTATION, mutation);
        }
        if (schema.isSupportingSubscriptions()) {
          builder.child(SUBSCRIPTION, subscription);
        }
        builder.children(ADD_TYPES, additionalTypes);
        builder.children(DIRECTIVES, directives);
        builder.children(SCHEMA_DIRECTIVES, schemaDirectives);
        builder.child(INTROSPECTION, Introspection.__Schema);
      }
      return builder.build();
    }

    @Override
    public GraphQLSchemaElement withNewChildren(SchemaElementChildrenContainer newChildren) {
      if (this.schemaElement != null) {
        this.schemaElement = newChildren.getChildOrNull(SCHEMA_ELEMENT);
        return this;
      }
      // special hack: we don't create a new dummy root, but we simply update it
      query = newChildren.getChildOrNull(QUERY);
      mutation = newChildren.getChildOrNull(MUTATION);
      subscription = newChildren.getChildOrNull(SUBSCRIPTION);
      additionalTypes = new LinkedHashSet<>(newChildren.getChildren(ADD_TYPES));
      directives = new LinkedHashSet<>(newChildren.getChildren(DIRECTIVES));
      schemaDirectives = new LinkedHashSet<>(newChildren.getChildren(SCHEMA_DIRECTIVES));
      return this;
    }

    @Override
    public TraversalControl accept(TraverserContext<GraphQLSchemaElement> context, GraphQLTypeVisitor visitor) {
      return assertShouldNeverHappen();
    }

    public GraphQLSchema rebuildSchema(GraphQLCodeRegistry.Builder codeRegistry) {
      return GraphQLSchema.newSchema()
        .query(this.query)
        .mutation(this.mutation)
        .subscription(this.subscription)
        .additionalTypes(this.additionalTypes)
        .additionalDirectives(this.directives)
        .withSchemaDirectives(this.schemaDirectives)
        .codeRegistry(codeRegistry.build())
        .description(schema.getDescription())
        .buildImpl(true);
    }
  }


  /**
   * Transforms a GraphQLSchema and returns a new GraphQLSchema object.
   *
   * @param schema  the schema to transform
   * @param visitor the visitor call back
   * @return a new GraphQLSchema instance.
   */
  public static GraphQLSchema transformSchema(GraphQLSchema schema, GraphQLTypeVisitor visitor) {
    SchemaTransformer schemaTransformer = new SchemaTransformer();
    return schemaTransformer.transform(schema, visitor);
  }

  /**
   * Transforms a GraphQLSchema and returns a new GraphQLSchema object.
   *
   * @param schema             the schema to transform
   * @param visitor            the visitor call back
   * @param postTransformation a callback that can be as a final step to the schema
   * @return a new GraphQLSchema instance.
   */
  public static GraphQLSchema transformSchema(GraphQLSchema schema,
                                              GraphQLTypeVisitor visitor,
                                              Consumer<GraphQLSchema.Builder> postTransformation) {
    SchemaTransformer schemaTransformer = new SchemaTransformer();
    return schemaTransformer.transform(schema, visitor, postTransformation);
  }

  /**
   * Transforms a {@link GraphQLSchemaElement} and returns a new element.
   *
   * @param schemaElement the schema element to transform
   * @param visitor       the visitor call back
   * @param <T>           for two
   * @return a new GraphQLSchemaElement instance.
   */
  public static <T extends GraphQLSchemaElement> T transformSchema(final T schemaElement, GraphQLTypeVisitor visitor) {
    SchemaTransformer schemaTransformer = new SchemaTransformer();
    return schemaTransformer.transform(schemaElement, visitor);
  }

  public GraphQLSchema transform(final GraphQLSchema schema, GraphQLTypeVisitor visitor) {
    return (GraphQLSchema)transformImpl(schema, null, visitor, null);
  }

  public GraphQLSchema transform(final GraphQLSchema schema,
                                 GraphQLTypeVisitor visitor,
                                 Consumer<GraphQLSchema.Builder> postTransformation) {
    return (GraphQLSchema)transformImpl(schema, null, visitor, postTransformation);
  }

  public <T extends GraphQLSchemaElement> T transform(final T schemaElement, GraphQLTypeVisitor visitor) {
    //noinspection unchecked
    return (T)transformImpl(null, schemaElement, visitor, null);
  }

  private Object transformImpl(final GraphQLSchema schema,
                               GraphQLSchemaElement schemaElement,
                               GraphQLTypeVisitor visitor,
                               Consumer<GraphQLSchema.Builder> postTransformation) {
    DummyRoot dummyRoot;
    GraphQLCodeRegistry.Builder codeRegistry = null;
    if (schema != null) {
      dummyRoot = new DummyRoot(schema);
      codeRegistry = GraphQLCodeRegistry.newCodeRegistry(schema.getCodeRegistry());
    }
    else {
      dummyRoot = new DummyRoot(schemaElement);
    }

    final Map<String, GraphQLNamedType> changedTypes = new LinkedHashMap<>();
    final Map<String, GraphQLTypeReference> typeReferences = new LinkedHashMap<>();

    // first pass - general transformation
    traverseAndTransform(dummyRoot, changedTypes, typeReferences, visitor, codeRegistry);

    // if we have changed any named elements AND we have type references referring to them then
    // we need to make a second pass to replace these type references to the new names
    if (!changedTypes.isEmpty()) {
      boolean hasTypeRefsForChangedTypes = changedTypes.keySet().stream().anyMatch(typeReferences::containsKey);
      if (hasTypeRefsForChangedTypes) {
        replaceTypeReferences(dummyRoot, codeRegistry, changedTypes);
      }
    }

    if (schema != null) {
      GraphQLSchema graphQLSchema = dummyRoot.rebuildSchema(codeRegistry);
      if (postTransformation != null) {
        graphQLSchema = graphQLSchema.transform(postTransformation);
      }
      return graphQLSchema;
    }
    else {
      return dummyRoot.schemaElement;
    }
  }

  private void replaceTypeReferences(DummyRoot dummyRoot,
                                     GraphQLCodeRegistry.Builder codeRegistry,
                                     Map<String, GraphQLNamedType> changedTypes) {
    GraphQLTypeVisitor typeRefVisitor = new GraphQLTypeVisitorStub() {
      @Override
      public TraversalControl visitGraphQLTypeReference(GraphQLTypeReference typeRef, TraverserContext<GraphQLSchemaElement> context) {
        GraphQLNamedType graphQLNamedType = changedTypes.get(typeRef.getName());
        if (graphQLNamedType != null) {
          typeRef = GraphQLTypeReference.typeRef(graphQLNamedType.getName());
          return changedNode(typeRef, context);
        }
        return CONTINUE;
      }
    };
    traverseAndTransform(dummyRoot, new HashMap<>(), new HashMap<>(), typeRefVisitor, codeRegistry);
  }

  private void traverseAndTransform(DummyRoot dummyRoot,
                                    Map<String, GraphQLNamedType> changedTypes,
                                    Map<String, GraphQLTypeReference> typeReferences,
                                    GraphQLTypeVisitor visitor,
                                    GraphQLCodeRegistry.Builder codeRegistry) {
    List<NodeZipper<GraphQLSchemaElement>> zippers = new LinkedList<>();
    Map<GraphQLSchemaElement, NodeZipper<GraphQLSchemaElement>> zipperByNodeAfterTraversing = new LinkedHashMap<>();
    Map<GraphQLSchemaElement, NodeZipper<GraphQLSchemaElement>> zipperByOriginalNode = new LinkedHashMap<>();

    Map<NodeZipper<GraphQLSchemaElement>, List<List<Breadcrumb<GraphQLSchemaElement>>>> breadcrumbsByZipper = new LinkedHashMap<>();

    Map<GraphQLSchemaElement, List<GraphQLSchemaElement>> reverseDependencies = new LinkedHashMap<>();

    TraverserVisitor<GraphQLSchemaElement> nodeTraverserVisitor = new TraverserVisitor<GraphQLSchemaElement>() {
      @Override
      public TraversalControl enter(TraverserContext<GraphQLSchemaElement> context) {
        GraphQLSchemaElement currentSchemaElement = context.thisNode();
        if (currentSchemaElement == dummyRoot) {
          return TraversalControl.CONTINUE;
        }
        if (currentSchemaElement instanceof GraphQLTypeReference typeRef) {
          typeReferences.put(typeRef.getName(), typeRef);
        }
        NodeZipper<GraphQLSchemaElement> nodeZipper =
          new NodeZipper<>(currentSchemaElement, context.getBreadcrumbs(), SCHEMA_ELEMENT_ADAPTER);
        context.setVar(NodeZipper.class, nodeZipper);
        context.setVar(NodeAdapter.class, SCHEMA_ELEMENT_ADAPTER);

        int zippersBefore = zippers.size();
        TraversalControl result = currentSchemaElement.accept(context, visitor);

        // detection if the node was changed
        if (zippersBefore + 1 == zippers.size()) {
          nodeZipper = zippers.get(zippers.size() - 1);
          if (context.originalThisNode() instanceof GraphQLNamedType originalNamedType && context.isChanged()) {
            GraphQLNamedType changedNamedType = (GraphQLNamedType)context.thisNode();
            if (!originalNamedType.getName().equals(changedNamedType.getName())) {
              changedTypes.put(originalNamedType.getName(), changedNamedType);
            }
          }
        }
        zipperByOriginalNode.put(context.originalThisNode(), nodeZipper);

        if (context.isDeleted()) {
          zipperByNodeAfterTraversing.put(context.originalThisNode(), nodeZipper);
        }
        else {
          zipperByNodeAfterTraversing.put(context.thisNode(), nodeZipper);
        }

        breadcrumbsByZipper.put(nodeZipper, new ArrayList<>());
        breadcrumbsByZipper.get(nodeZipper).add(context.getBreadcrumbs());
        if (nodeZipper.getModificationType() != NodeZipper.ModificationType.DELETE) {
          reverseDependencies.computeIfAbsent(context.thisNode(), ign -> new ArrayList<>()).add(context.getParentNode());
        }
        return result;
      }

      @Override
      public TraversalControl leave(TraverserContext<GraphQLSchemaElement> context) {
        return TraversalControl.CONTINUE;
      }

      @Override
      public TraversalControl backRef(TraverserContext<GraphQLSchemaElement> context) {
        NodeZipper<GraphQLSchemaElement> zipper = zipperByOriginalNode.get(context.thisNode());
        breadcrumbsByZipper.get(zipper).add(context.getBreadcrumbs());
        visitor.visitBackRef(context);
        List<GraphQLSchemaElement> reverseDependenciesForCurNode = reverseDependencies.get(zipper.getCurNode());
        assertNotNull(reverseDependenciesForCurNode);
        reverseDependenciesForCurNode.add(context.getParentNode());
        return TraversalControl.CONTINUE;
      }
    };


    Traverser<GraphQLSchemaElement> traverser =
      Traverser.depthFirstWithNamedChildren(SCHEMA_ELEMENT_ADAPTER::getNamedChildren, zippers, null);
    if (codeRegistry != null) {
      traverser.rootVar(GraphQLCodeRegistry.Builder.class, codeRegistry);
    }

    traverser.traverse(dummyRoot, nodeTraverserVisitor);

    List<GraphQLSchemaElement> topologicalSort = topologicalSort(zipperByNodeAfterTraversing.keySet(), reverseDependencies);

    zipUpToDummyRoot(zippers, topologicalSort, breadcrumbsByZipper, zipperByNodeAfterTraversing);
  }

  private List<GraphQLSchemaElement> topologicalSort(Set<GraphQLSchemaElement> allNodes,
                                                     Map<GraphQLSchemaElement, List<GraphQLSchemaElement>> reverseDependencies) {
    List<GraphQLSchemaElement> result = new ArrayList<>();
    Set<GraphQLSchemaElement> notPermMarked = new LinkedHashSet<>(allNodes);
    Set<GraphQLSchemaElement> tempMarked = new LinkedHashSet<>();
    Set<GraphQLSchemaElement> permMarked = new LinkedHashSet<>();
    while (true) {
      Iterator<GraphQLSchemaElement> iterator = notPermMarked.iterator();
      if (!iterator.hasNext()) {
        break;
      }
      GraphQLSchemaElement n = iterator.next();
      iterator.remove();
      visit(n, tempMarked, permMarked, notPermMarked, result, reverseDependencies);
    }
    return result;
  }

  private void visit(GraphQLSchemaElement n,
                     Set<GraphQLSchemaElement> tempMarked,
                     Set<GraphQLSchemaElement> permMarked,
                     Set<GraphQLSchemaElement> notPermMarked,
                     List<GraphQLSchemaElement> result,
                     Map<GraphQLSchemaElement, List<GraphQLSchemaElement>> reverseDependencies) {
    if (permMarked.contains(n)) {
      return;
    }
    if (tempMarked.contains(n)) {
      Assert.assertShouldNeverHappen("NOT A DAG: %s has temp mark", n);
      return;
    }
    tempMarked.add(n);
    if (reverseDependencies.containsKey(n)) {
      for (GraphQLSchemaElement m : reverseDependencies.get(n)) {
        visit(m, tempMarked, permMarked, notPermMarked, result, reverseDependencies);
      }
    }
    tempMarked.remove(n);
    permMarked.add(n);
    notPermMarked.remove(n);
    result.add(n);
  }

  private void zipUpToDummyRoot(List<NodeZipper<GraphQLSchemaElement>> zippers,
                                List<GraphQLSchemaElement> topSort,
                                Map<NodeZipper<GraphQLSchemaElement>, List<List<Breadcrumb<GraphQLSchemaElement>>>> breadcrumbsByZipper,
                                Map<GraphQLSchemaElement, NodeZipper<GraphQLSchemaElement>> nodeToZipper) {
    if (zippers.size() == 0) {
      return;
    }
    Set<NodeZipper<GraphQLSchemaElement>> curZippers = new LinkedHashSet<>(zippers);

    for (int i = topSort.size() - 1; i >= 0; i--) {
      GraphQLSchemaElement element = topSort.get(i);
      // that the map goes from  zipper -> one List (= one path) is because we know that in a schema one element
      // has never two different edges to another element
      Map<NodeZipper<GraphQLSchemaElement>, List<Breadcrumb<GraphQLSchemaElement>>> zipperWithSameParent =
        zipperWithSameParent(element, curZippers, breadcrumbsByZipper);
      // this means we have a node which doesn't need to be changed
      if (zipperWithSameParent.size() == 0) {
        continue;
      }
      NodeZipper<GraphQLSchemaElement> newZipper = moveUp(element, zipperWithSameParent);
      if (element instanceof DummyRoot) {
        // this means we have updated the dummy root and we are done (dummy root is a special as it gets updated in place, see Implementation of DummyRoot)
        break;
      }

      // update curZippers
      NodeZipper<GraphQLSchemaElement> curZipperForElement = nodeToZipper.get(element);
      assertNotNull(curZipperForElement, () -> format("curZipperForElement is null for parentNode %s", element));
      curZippers.remove(curZipperForElement);
      curZippers.add(newZipper);

      // update breadcrumbsByZipper to use the newZipper
      List<List<Breadcrumb<GraphQLSchemaElement>>> breadcrumbsForOriginalParent = breadcrumbsByZipper.get(curZipperForElement);
      assertNotNull(breadcrumbsForOriginalParent, () -> format("No breadcrumbs found for zipper %s", curZipperForElement));
      breadcrumbsByZipper.remove(curZipperForElement);
      breadcrumbsByZipper.put(newZipper, breadcrumbsForOriginalParent);
    }
  }

  private Map<NodeZipper<GraphQLSchemaElement>, List<Breadcrumb<GraphQLSchemaElement>>> zipperWithSameParent(GraphQLSchemaElement parent,
                                                                                                             Set<NodeZipper<GraphQLSchemaElement>> zippers,
                                                                                                             Map<NodeZipper<GraphQLSchemaElement>, List<List<Breadcrumb<GraphQLSchemaElement>>>> curBreadcrumbsByZipper) {
    Map<NodeZipper<GraphQLSchemaElement>, List<Breadcrumb<GraphQLSchemaElement>>> result = new LinkedHashMap<>();
    outer:
    for (NodeZipper<GraphQLSchemaElement> zipper : zippers) {
      for (List<Breadcrumb<GraphQLSchemaElement>> path : curBreadcrumbsByZipper.get(zipper)) {
        if (path.get(0).getNode() == parent) {
          result.put(zipper, path);
          continue outer;
        }
      }
    }
    return result;
  }


  private static class ZipperWithOneParent {
    public ZipperWithOneParent(NodeZipper<GraphQLSchemaElement> zipper, Breadcrumb<GraphQLSchemaElement> parent) {
      this.zipper = zipper;
      this.parent = parent;
    }

    public NodeZipper<GraphQLSchemaElement> zipper;
    public Breadcrumb<GraphQLSchemaElement> parent;
  }

  private NodeZipper<GraphQLSchemaElement> moveUp(
    GraphQLSchemaElement parent,
    Map<NodeZipper<GraphQLSchemaElement>, List<Breadcrumb<GraphQLSchemaElement>>> sameParentsZipper) {
    Set<NodeZipper<GraphQLSchemaElement>> sameParent = sameParentsZipper.keySet();
    assertNotEmpty(sameParent, () -> "expected at least one zipper");

    Map<String, List<GraphQLSchemaElement>> childrenMap = new HashMap<>(SCHEMA_ELEMENT_ADAPTER.getNamedChildren(parent));
    Map<String, Integer> indexCorrection = new HashMap<>();

    List<ZipperWithOneParent> zipperWithOneParents = new ArrayList<>();
    for (NodeZipper<GraphQLSchemaElement> zipper : sameParent) {
      List<Breadcrumb<GraphQLSchemaElement>> breadcrumbs = sameParentsZipper.get(zipper);
      zipperWithOneParents.add(new ZipperWithOneParent(zipper, breadcrumbs.get(0)));
    }

    zipperWithOneParents.sort((zipperWithOneParent1, zipperWithOneParent2) -> {
      NodeZipper<GraphQLSchemaElement> zipper1 = zipperWithOneParent1.zipper;
      NodeZipper<GraphQLSchemaElement> zipper2 = zipperWithOneParent2.zipper;
      Breadcrumb<GraphQLSchemaElement> breadcrumb1 = zipperWithOneParent1.parent;
      Breadcrumb<GraphQLSchemaElement> breadcrumb2 = zipperWithOneParent2.parent;
      int index1 = breadcrumb1.getLocation().getIndex();
      int index2 = breadcrumb2.getLocation().getIndex();
      if (index1 != index2) {
        return Integer.compare(index1, index2);
      }
      NodeZipper.ModificationType modificationType1 = zipper1.getModificationType();
      NodeZipper.ModificationType modificationType2 = zipper2.getModificationType();

      // same index can never be deleted and changed at the same time

      if (modificationType1 == modificationType2) {
        return 0;
      }

      // always first replacing the node
      if (modificationType1 == REPLACE) {
        return -1;
      }
      // and then INSERT_BEFORE before INSERT_AFTER
      return modificationType1 == NodeZipper.ModificationType.INSERT_BEFORE ? -1 : 1;
    });

    for (ZipperWithOneParent zipperWithOneParent : zipperWithOneParents) {
      NodeZipper<GraphQLSchemaElement> zipper = zipperWithOneParent.zipper;
      Breadcrumb<GraphQLSchemaElement> breadcrumb = zipperWithOneParent.parent;
      NodeLocation location = breadcrumb.getLocation();
      Integer ixDiff = indexCorrection.getOrDefault(location.getName(), 0);
      int ix = location.getIndex() + ixDiff;
      String name = location.getName();
      List<GraphQLSchemaElement> childList = new ArrayList<>(childrenMap.get(name));
      switch (zipper.getModificationType()) {
        case REPLACE:
          childList.set(ix, zipper.getCurNode());
          break;
        case DELETE:
          childList.remove(ix);
          indexCorrection.put(name, ixDiff - 1);
          break;
        case INSERT_BEFORE:
          childList.add(ix, zipper.getCurNode());
          indexCorrection.put(name, ixDiff + 1);
          break;
        case INSERT_AFTER:
          childList.add(ix + 1, zipper.getCurNode());
          indexCorrection.put(name, ixDiff + 1);
          break;
      }
      childrenMap.put(name, childList);
    }

    GraphQLSchemaElement newNode = SCHEMA_ELEMENT_ADAPTER.withNewChildren(parent, childrenMap);
    final List<Breadcrumb<GraphQLSchemaElement>> oldBreadcrumbs = sameParent.iterator().next().getBreadcrumbs();
    List<Breadcrumb<GraphQLSchemaElement>> newBreadcrumbs;
    if (oldBreadcrumbs.size() > 1) {
      newBreadcrumbs = oldBreadcrumbs.subList(1, oldBreadcrumbs.size());
    }
    else {
      newBreadcrumbs = Collections.emptyList();
    }
    return new NodeZipper<>(newNode, newBreadcrumbs, SCHEMA_ELEMENT_ADAPTER);
  }
}
