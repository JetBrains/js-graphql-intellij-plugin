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


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.intellij.lang.jsgraphql.types.DirectivesUtil;
import com.intellij.lang.jsgraphql.types.GraphQLException;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.SchemaDefinition;
import com.intellij.lang.jsgraphql.types.language.SchemaExtensionDefinition;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.types.Assert.*;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.*;
import static com.intellij.lang.jsgraphql.types.schema.GraphqlTypeComparators.byNameAsc;
import static com.intellij.lang.jsgraphql.types.schema.GraphqlTypeComparators.sortTypes;

/**
 * The schema represents the combined type system of the graphql engine.  This is how the engine knows
 * what graphql queries represent what data.
 * <p>
 * See http://graphql.org/learn/schema/#type-language for more details
 */
@PublicApi
public class GraphQLSchema {


  private final GraphQLObjectType queryType;
  private final GraphQLObjectType mutationType;
  private final GraphQLObjectType subscriptionType;
  private final ImmutableSet<GraphQLType> additionalTypes;
  private final DirectivesUtil.DirectivesHolder directives;
  private final DirectivesUtil.DirectivesHolder schemaDirectives;
  private final SchemaDefinition definition;
  private final ImmutableList<SchemaExtensionDefinition> extensionDefinitions;

  private final ImmutableMap<String, GraphQLNamedType> typeMap;
  private final ImmutableMap<String, ImmutableList<GraphQLObjectType>> interfaceNameToObjectTypes;
  private final ImmutableMap<String, ImmutableList<String>> interfaceNameToObjectTypeNames;

  private final String description;

  private final List<GraphQLException> errors = new ArrayList<>();

  /**
   * @param queryType the query type
   * @deprecated use the {@link #newSchema()} builder pattern instead, as this constructor will be made private in a future version.
   */
  @Internal
  @Deprecated(forRemoval = true)
  public GraphQLSchema(GraphQLObjectType queryType) {
    this(queryType, null, Collections.emptySet());
  }

  /**
   * @param queryType       the query type
   * @param mutationType    the mutation type
   * @param additionalTypes additional types
   * @deprecated use the {@link #newSchema()} builder pattern instead, as this constructor will be made private in a future version.
   */
  @Internal
  @Deprecated(forRemoval = true)
  public GraphQLSchema(GraphQLObjectType queryType, GraphQLObjectType mutationType, Set<GraphQLType> additionalTypes) {
    this(queryType, mutationType, null, additionalTypes);
  }

  /**
   * @param queryType        the query type
   * @param mutationType     the mutation type
   * @param subscriptionType the subscription type
   * @param additionalTypes  additional types
   * @deprecated use the {@link #newSchema()} builder pattern instead, as this constructor will be made private in a future version.
   */
  @Internal
  @Deprecated(forRemoval = true)
  public GraphQLSchema(GraphQLObjectType queryType,
                       GraphQLObjectType mutationType,
                       GraphQLObjectType subscriptionType,
                       Set<GraphQLType> additionalTypes) {
    this(newSchema().query(queryType).mutation(mutationType).subscription(subscriptionType).additionalTypes(additionalTypes), false);
  }

  @Internal
  private GraphQLSchema(Builder builder, boolean afterTransform) {
    assertNotNull(builder.additionalTypes, () -> "additionalTypes can't be null");
    assertNotNull(builder.additionalDirectives, () -> "directives can't be null");


    this.queryType = builder.queryType;
    this.mutationType = builder.mutationType;
    this.subscriptionType = builder.subscriptionType;
    this.additionalTypes = ImmutableSet.copyOf(builder.additionalTypes);
    this.directives = new DirectivesUtil.DirectivesHolder(builder.additionalDirectives);
    this.schemaDirectives = new DirectivesUtil.DirectivesHolder(builder.schemaDirectives);
    this.definition = builder.definition;
    this.extensionDefinitions = nonNullCopyOf(builder.extensionDefinitions);
    // sorted by type name
    SchemaUtil schemaUtil = new SchemaUtil();
    this.typeMap = ImmutableMap.copyOf(schemaUtil.allTypes(this, additionalTypes, afterTransform));
    this.interfaceNameToObjectTypes = buildInterfacesToObjectTypes(schemaUtil.groupImplementations(this));
    this.interfaceNameToObjectTypeNames = buildInterfacesToObjectName(interfaceNameToObjectTypes);
    this.description = builder.description;
  }

  private ImmutableMap<String, ImmutableList<GraphQLObjectType>> buildInterfacesToObjectTypes(Map<String, List<GraphQLObjectType>> groupImplementations) {
    ImmutableMap.Builder<String, ImmutableList<GraphQLObjectType>> map = ImmutableMap.builder();
    for (Map.Entry<String, List<GraphQLObjectType>> e : groupImplementations.entrySet()) {
      ImmutableList<GraphQLObjectType> sortedObjectTypes = ImmutableList.copyOf(sortTypes(byNameAsc(), e.getValue()));
      map.put(e.getKey(), sortedObjectTypes);
    }
    return map.build();
  }

  private ImmutableMap<String, ImmutableList<String>> buildInterfacesToObjectName(ImmutableMap<String, ImmutableList<GraphQLObjectType>> byInterface) {
    ImmutableMap.Builder<String, ImmutableList<String>> map = ImmutableMap.builder();
    for (Map.Entry<String, ImmutableList<GraphQLObjectType>> e : byInterface.entrySet()) {
      ImmutableList<String> objectTypeNames = map(e.getValue(), GraphQLObjectType::getName);
      map.put(e.getKey(), objectTypeNames);
    }
    return map.build();
  }

  public Set<GraphQLType> getAdditionalTypes() {
    ProgressManager.checkCanceled();
    return additionalTypes;
  }

  public GraphQLType getType(String typeName) {
    ProgressManager.checkCanceled();
    return typeMap.get(typeName);
  }

  /**
   * Called to return a named {@link com.intellij.lang.jsgraphql.types.schema.GraphQLObjectType} from the schema
   *
   * @param typeName the name of the type
   * @return a graphql object type or null if there is one
   * @throws com.intellij.lang.jsgraphql.types.GraphQLException if the type is NOT a object type
   */
  public GraphQLObjectType getObjectType(String typeName) {
    ProgressManager.checkCanceled();
    GraphQLType graphQLType = typeMap.get(typeName);
    if (graphQLType != null) {
      assertTrue(graphQLType instanceof GraphQLObjectType,
                 () -> String.format("You have asked for named object type '%s' but its not an object type but rather a '%s'", typeName,
                                     graphQLType.getClass().getName()));
    }
    return (GraphQLObjectType)graphQLType;
  }

  public Map<String, GraphQLNamedType> getTypeMap() {
    ProgressManager.checkCanceled();
    return typeMap;
  }

  public List<GraphQLNamedType> getAllTypesAsList() {
    ProgressManager.checkCanceled();
    return sortTypes(byNameAsc(), typeMap.values());
  }

  /**
   * This will return the list of {@link com.intellij.lang.jsgraphql.types.schema.GraphQLObjectType} types that implement the given
   * interface type.
   *
   * @param type interface type to obtain implementations of.
   * @return list of types implementing provided interface
   */
  public List<GraphQLObjectType> getImplementations(GraphQLInterfaceType type) {
    ProgressManager.checkCanceled();
    return interfaceNameToObjectTypes.getOrDefault(type.getName(), emptyList());
  }

  /**
   * Returns true if a specified concrete type is a possible type of a provided abstract type.
   * If the provided abstract type is:
   * - an interface, it checks whether the concrete type is one of its implementations.
   * - a union, it checks whether the concrete type is one of its possible types.
   *
   * @param abstractType abstract type either interface or union
   * @param concreteType concrete type
   * @return true if possible type, false otherwise.
   */
  public boolean isPossibleType(GraphQLNamedType abstractType, GraphQLObjectType concreteType) {
    ProgressManager.checkCanceled();
    if (abstractType instanceof GraphQLInterfaceType) {
      ImmutableList<String> objectNames = this.interfaceNameToObjectTypeNames.getOrDefault(abstractType.getName(), emptyList());
      return objectNames.contains(concreteType.getName());
    }
    else if (abstractType instanceof GraphQLUnionType) {
      return ((GraphQLUnionType)abstractType).isPossibleType(concreteType);
    }
    return assertShouldNeverHappen("Unsupported abstract type %s. Abstract types supported are Union and Interface.",
                                   abstractType.getName());
  }

  public @Nullable GraphQLObjectType getQueryType() {
    ProgressManager.checkCanceled();
    return queryType;
  }

  public @Nullable GraphQLObjectType getMutationType() {
    ProgressManager.checkCanceled();
    return mutationType;
  }

  public @Nullable GraphQLObjectType getSubscriptionType() {
    ProgressManager.checkCanceled();
    return subscriptionType;
  }

  /**
   * This returns the list of directives that are associated with this schema object including
   * built in ones.
   *
   * @return a list of directives
   */
  public List<GraphQLDirective> getDirectives() {
    ProgressManager.checkCanceled();
    return directives.getDirectives();
  }

  /**
   * @return a a map of non repeatable directives by directive name
   */
  public Map<String, GraphQLDirective> getDirectivesByName() {
    ProgressManager.checkCanceled();
    return directives.getDirectivesByName();
  }

  /**
   * Directives can be `repeatable` and hence this returns a list of directives by name, some with an arity of 1 and some with an arity of greater than
   * 1.
   *
   * @return a map of all directives by directive name
   */
  public Map<String, List<GraphQLDirective>> getAllDirectivesByName() {
    ProgressManager.checkCanceled();
    return directives.getAllDirectivesByName();
  }

  /**
   * Returns a named directive that (for legacy reasons) will be only in the set of non repeatable directives
   *
   * @param directiveName the name of the directive to retrieve
   * @return the directive or null if there is not one with that name
   */
  @Deprecated
  public GraphQLDirective getDirective(String directiveName) {
    ProgressManager.checkCanceled();
    return directives.getDirective(directiveName);
  }

  /**
   * Returns a list of named directive that can include non repeatable and repeatable directives.
   *
   * @param directiveName the name of the directives to retrieve
   * @return the directive or empty list if there is not one with that name
   */
  public List<GraphQLDirective> getDirectives(String directiveName) {
    ProgressManager.checkCanceled();
    return directives.getDirectives(directiveName);
  }

  /**
   * Returns a the first named directive that can include non repeatable and repeatable directives
   * or null if there is not one called that name
   *
   * @param directiveName the name of the directives to retrieve
   * @return the directive or null if there is not one with that name
   */
  public GraphQLDirective getFirstDirective(String directiveName) {
    ProgressManager.checkCanceled();
    return DirectivesUtil.getFirstDirective(directiveName, getAllDirectivesByName());
  }

  /**
   * This returns the list of directives that have been explicitly put on the
   * schema object.  Note that {@link #getDirectives()} will return
   * directives for all schema elements, whereas this is just for the schema
   * element itself
   *
   * @return a list of directives
   */
  public List<GraphQLDirective> getSchemaDirectives() {
    ProgressManager.checkCanceled();
    return schemaDirectives.getDirectives();
  }

  /**
   * This returns a map of non repeatable directives that have been explicitly put on the
   * schema object.  Note that {@link #getDirectives()} will return
   * directives for all schema elements, whereas this is just for the schema
   * element itself
   *
   * @return a map of directives
   */
  public Map<String, GraphQLDirective> getSchemaDirectiveByName() {
    ProgressManager.checkCanceled();
    return schemaDirectives.getDirectivesByName();
  }

  /**
   * Schema directives can be `repeatable` and hence this returns a list of directives by name, some with an arity of 1 and some with an arity of greater than
   * 1.
   *
   * @return a map of all schema directives by directive name
   */
  public Map<String, List<GraphQLDirective>> getAllSchemaDirectivesByName() {
    ProgressManager.checkCanceled();
    return schemaDirectives.getAllDirectivesByName();
  }

  /**
   * This returns the named directive that have been explicitly put on the
   * schema object.  Note that {@link com.intellij.lang.jsgraphql.types.schema.GraphQLDirectiveContainer#getDirective(String)} will return
   * directives for all schema elements, whereas this is just for the schema
   * element itself
   *
   * @param directiveName the name of the directive
   * @return a named directive
   */
  public GraphQLDirective getSchemaDirective(String directiveName) {
    ProgressManager.checkCanceled();
    return schemaDirectives.getDirective(directiveName);
  }

  public List<GraphQLDirective> getSchemaDirectives(String directiveName) {
    ProgressManager.checkCanceled();
    return schemaDirectives.getDirectives(directiveName);
  }

  public SchemaDefinition getDefinition() {
    ProgressManager.checkCanceled();
    return definition;
  }

  public List<SchemaExtensionDefinition> getExtensionDefinitions() {
    ProgressManager.checkCanceled();
    return extensionDefinitions;
  }

  public boolean isQueryDefined() {
    return queryType != null;
  }

  public boolean isSupportingMutations() {
    return mutationType != null;
  }

  public boolean isSupportingSubscriptions() {
    return subscriptionType != null;
  }

  public String getDescription() {
    return description;
  }

  public void addError(@NotNull GraphQLException error) {
    errors.add(error);
  }

  public @NotNull List<GraphQLException> getErrors() {
    return ContainerUtil.unmodifiableOrEmptyList(errors);
  }

  /**
   * This helps you transform the current GraphQLSchema object into another one by starting a builder with all
   * the current values and allows you to transform it how you want.
   *
   * @param builderConsumer the consumer code that will be given a builder to transform
   * @return a new GraphQLSchema object based on calling build on that builder
   */
  public GraphQLSchema transform(Consumer<Builder> builderConsumer) {
    Builder builder = newSchema(this);
    builderConsumer.accept(builder);
    return builder.build();
  }

  /**
   * @return a new schema builder
   */
  public static Builder newSchema() {
    return new Builder();
  }

  /**
   * This allows you to build a schema from an existing schema.  It copies everything from the existing
   * schema and then allows you to replace them.
   *
   * @param existingSchema the existing schema
   * @return a new schema builder
   */
  public static Builder newSchema(GraphQLSchema existingSchema) {
    return new Builder()
      .query(existingSchema.getQueryType())
      .mutation(existingSchema.getMutationType())
      .subscription(existingSchema.getSubscriptionType())
      .clearAdditionalTypes()
      .clearDirectives()
      .additionalDirectives(new LinkedHashSet<>(existingSchema.getDirectives()))
      .clearSchemaDirectives()
      .withSchemaDirectives(schemaDirectivesArray(existingSchema))
      .additionalTypes(existingSchema.additionalTypes)
      .description(existingSchema.getDescription());
  }

  private static GraphQLDirective[] schemaDirectivesArray(GraphQLSchema existingSchema) {
    return existingSchema.schemaDirectives.getDirectives().toArray(new GraphQLDirective[0]);
  }

  public static class Builder {
    private GraphQLObjectType queryType;
    private GraphQLObjectType mutationType;
    private GraphQLObjectType subscriptionType;
    private Set<GraphQLType> additionalTypes = new LinkedHashSet<>();
    private SchemaDefinition definition;
    private List<SchemaExtensionDefinition> extensionDefinitions;
    private String description;

    private Set<GraphQLDirective> additionalDirectives = new LinkedHashSet<>();
    private List<GraphQLDirective> schemaDirectives = new ArrayList<>();

    private SchemaUtil schemaUtil = new SchemaUtil();

    public Builder query(GraphQLObjectType.Builder builder) {
      return query(builder.build());
    }

    public Builder query(GraphQLObjectType queryType) {
      this.queryType = queryType;
      return this;
    }

    public Builder mutation(GraphQLObjectType.Builder builder) {
      return mutation(builder.build());
    }

    public Builder mutation(GraphQLObjectType mutationType) {
      this.mutationType = mutationType;
      return this;
    }

    public Builder subscription(GraphQLObjectType.Builder builder) {
      return subscription(builder.build());
    }

    public Builder subscription(GraphQLObjectType subscriptionType) {
      this.subscriptionType = subscriptionType;
      return this;
    }

    public Builder additionalTypes(Set<GraphQLType> additionalTypes) {
      this.additionalTypes.addAll(additionalTypes);
      return this;
    }

    public Builder additionalType(GraphQLType additionalType) {
      this.additionalTypes.add(additionalType);
      return this;
    }

    public Builder clearAdditionalTypes() {
      this.additionalTypes.clear();
      return this;
    }

    public Builder additionalDirectives(Set<GraphQLDirective> additionalDirectives) {
      this.additionalDirectives.addAll(additionalDirectives);
      return this;
    }

    public Builder additionalDirective(GraphQLDirective additionalDirective) {
      this.additionalDirectives.add(additionalDirective);
      return this;
    }

    public Builder clearDirectives() {
      this.additionalDirectives.clear();
      return this;
    }


    public Builder withSchemaDirectives(GraphQLDirective... directives) {
      for (GraphQLDirective directive : directives) {
        withSchemaDirective(directive);
      }
      return this;
    }

    public Builder withSchemaDirectives(Collection<? extends GraphQLDirective> directives) {
      for (GraphQLDirective directive : directives) {
        withSchemaDirective(directive);
      }
      return this;
    }

    public Builder withSchemaDirective(GraphQLDirective directive) {
      assertNotNull(directive, () -> "directive can't be null");
      schemaDirectives.add(directive);
      return this;
    }

    public Builder withSchemaDirective(GraphQLDirective.Builder builder) {
      return withSchemaDirective(builder.build());
    }

    /**
     * This is used to clear all the directives in the builder so far.
     *
     * @return the builder
     */
    public Builder clearSchemaDirectives() {
      schemaDirectives.clear();
      return this;
    }

    public Builder definition(SchemaDefinition definition) {
      this.definition = definition;
      return this;
    }

    public Builder extensionDefinitions(List<SchemaExtensionDefinition> extensionDefinitions) {
      this.extensionDefinitions = extensionDefinitions;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Builds the schema
     *
     * @param additionalTypes - please dont use this any more
     * @return the built schema
     * @deprecated - Use the {@link #additionalType(GraphQLType)} methods
     */
    @Deprecated(forRemoval = true)
    public GraphQLSchema build(Set<GraphQLType> additionalTypes) {
      return additionalTypes(additionalTypes).build();
    }

    /**
     * Builds the schema
     *
     * @param additionalTypes      - please don't use this any more
     * @param additionalDirectives - please don't use this any more
     * @return the built schema
     * @deprecated - Use the {@link #additionalType(GraphQLType)} and {@link #additionalDirective(GraphQLDirective)} methods
     */
    @Deprecated(forRemoval = true)
    public GraphQLSchema build(Set<GraphQLType> additionalTypes, Set<GraphQLDirective> additionalDirectives) {
      return additionalTypes(additionalTypes).additionalDirectives(additionalDirectives).build();
    }

    /**
     * Builds the schema
     *
     * @return the built schema
     */
    public GraphQLSchema build() {
      return buildImpl(false);
    }

    GraphQLSchema buildImpl(boolean afterTransform) {
      assertNotNull(additionalTypes, () -> "additionalTypes can't be null");
      assertNotNull(additionalDirectives, () -> "additionalDirectives can't be null");

      final GraphQLSchema schema = new GraphQLSchema(this, afterTransform);
      schemaUtil.replaceTypeReferences(schema);
      return schema;
    }
  }
}
