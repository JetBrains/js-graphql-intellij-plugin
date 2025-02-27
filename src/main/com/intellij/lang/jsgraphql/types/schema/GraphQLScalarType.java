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
import com.intellij.lang.jsgraphql.types.DirectivesUtil;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.ScalarTypeDefinition;
import com.intellij.lang.jsgraphql.types.language.ScalarTypeExtensionDefinition;
import com.intellij.lang.jsgraphql.types.util.TraversalControl;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.Assert.assertValidName;
import static com.intellij.lang.jsgraphql.types.schema.SchemaElementChildrenContainer.newSchemaElementChildrenContainer;
import static java.util.Collections.emptyList;

/**
 * A scalar type is a leaf node in the graphql tree of types.  This class allows you to define new scalar types.
 *
 * <blockquote>
 * GraphQL provides a number of built‐in scalars, but type systems can add additional scalars with semantic meaning,
 * for example, a GraphQL system could define a scalar called Time which, while serialized as a string, promises to
 * conform to ISO‐8601. When querying a field of type Time, you can then rely on the ability to parse the result with an ISO‐8601 parser and use a client‐specific primitive for time.
 * <p>
 * From the spec : http://facebook.github.io/graphql/#sec-Scalars
 * </blockquote>
 * <p>
 * graphql-java ships with a set of predefined scalar types via {@link com.intellij.lang.jsgraphql.types.Scalars}
 *
 * @see com.intellij.lang.jsgraphql.types.Scalars
 */
@PublicApi
public class GraphQLScalarType
  implements GraphQLNamedInputType, GraphQLNamedOutputType, GraphQLUnmodifiedType, GraphQLNullableType, GraphQLDirectiveContainer {

  private final String name;
  private final String description;
  private final Coercing coercing;
  private final ScalarTypeDefinition definition;
  private final ImmutableList<ScalarTypeExtensionDefinition> extensionDefinitions;
  private final DirectivesUtil.DirectivesHolder directives;
  private final String specifiedByUrl;

  public static final String CHILD_DIRECTIVES = "directives";


  /**
   * @param name        the name
   * @param description the description
   * @param coercing    the coercing function
   * @deprecated use the {@link #newScalar()} builder pattern instead, as this constructor will be made private in a future version.
   */
  @Internal
  @Deprecated
  public GraphQLScalarType(String name, String description, Coercing coercing) {
    this(name, description, coercing, emptyList(), null);
  }

  /**
   * @param name        the name
   * @param description the description
   * @param coercing    the coercing function
   * @param directives  the directives on this type element
   * @param definition  the AST definition
   * @deprecated use the {@link #newScalar()} builder pattern instead, as this constructor will be made private in a future version.
   */
  @Internal
  @Deprecated(forRemoval = true)
  public GraphQLScalarType(String name,
                           String description,
                           Coercing coercing,
                           List<GraphQLDirective> directives,
                           ScalarTypeDefinition definition) {
    this(name, description, coercing, directives, definition, emptyList(), null);
  }

  private GraphQLScalarType(String name,
                            String description,
                            Coercing coercing,
                            List<GraphQLDirective> directives,
                            ScalarTypeDefinition definition,
                            List<ScalarTypeExtensionDefinition> extensionDefinitions,
                            String specifiedByUrl) {
    assertValidName(name);
    assertNotNull(coercing, () -> "coercing can't be null");
    assertNotNull(directives, () -> "directives can't be null");

    this.name = name;
    this.description = description;
    this.coercing = coercing;
    this.definition = definition;
    this.directives = new DirectivesUtil.DirectivesHolder(directives);
    this.extensionDefinitions = ImmutableList.copyOf(extensionDefinitions);
    this.specifiedByUrl = specifiedByUrl;
  }

  @Override
  public String getName() {
    return name;
  }


  @Override
  public String getDescription() {
    return description;
  }

  public String getSpecifiedByUrl() {
    return specifiedByUrl;
  }

  public Coercing getCoercing() {
    return coercing;
  }

  @Override
  public ScalarTypeDefinition getDefinition() {
    return definition;
  }

  public List<ScalarTypeExtensionDefinition> getExtensionDefinitions() {
    return extensionDefinitions;
  }

  @Override
  public List<GraphQLDirective> getDirectives() {
    return directives.getDirectives();
  }

  @Override
  public Map<String, GraphQLDirective> getDirectivesByName() {
    return directives.getDirectivesByName();
  }

  @Override
  public Map<String, List<GraphQLDirective>> getAllDirectivesByName() {
    return directives.getAllDirectivesByName();
  }

  @Override
  public GraphQLDirective getDirective(String directiveName) {
    return directives.getDirective(directiveName);
  }

  @Override
  public String toString() {
    return "GraphQLScalarType{" +
           "name='" + name + '\'' +
           ", description='" + description + '\'' +
           ", coercing=" + coercing +
           '}';
  }

  /**
   * This helps you transform the current GraphQLObjectType into another one by starting a builder with all
   * the current values and allows you to transform it how you want.
   *
   * @param builderConsumer the consumer code that will be given a builder to transform
   * @return a new object based on calling build on that builder
   */
  public GraphQLScalarType transform(Consumer<Builder> builderConsumer) {
    Builder builder = newScalar(this);
    builderConsumer.accept(builder);
    return builder.build();
  }

  @Override
  public TraversalControl accept(TraverserContext<GraphQLSchemaElement> context, GraphQLTypeVisitor visitor) {
    return visitor.visitGraphQLScalarType(this, context);
  }

  @Override
  public List<GraphQLSchemaElement> getChildren() {
    return ImmutableList.copyOf(directives.getDirectives());
  }

  @Override
  public SchemaElementChildrenContainer getChildrenWithTypeReferences() {
    return newSchemaElementChildrenContainer()
      .children(CHILD_DIRECTIVES, directives.getDirectives())
      .build();
  }

  @Override
  public GraphQLScalarType withNewChildren(SchemaElementChildrenContainer newChildren) {
    return transform(builder ->
                       builder.replaceDirectives(newChildren.getChildren(CHILD_DIRECTIVES))
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean equals(Object o) {
    return super.equals(o);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int hashCode() {
    return super.hashCode();
  }


  public static Builder newScalar() {
    return new Builder();
  }

  public static Builder newScalar(GraphQLScalarType existing) {
    return new Builder(existing);
  }


  @PublicApi
  public static class Builder extends GraphqlTypeBuilder {
    private Coercing coercing;
    private ScalarTypeDefinition definition;
    private List<ScalarTypeExtensionDefinition> extensionDefinitions = emptyList();
    private final List<GraphQLDirective> directives = new ArrayList<>();
    private String specifiedByUrl;

    public Builder() {
    }

    public Builder(GraphQLScalarType existing) {
      name = existing.getName();
      description = existing.getDescription();
      coercing = existing.getCoercing();
      definition = existing.getDefinition();
      extensionDefinitions = existing.getExtensionDefinitions();
      specifiedByUrl = existing.getSpecifiedByUrl();
      DirectivesUtil.enforceAddAll(this.directives, existing.getDirectives());
    }

    @Override
    public Builder name(String name) {
      super.name(name);
      return this;
    }

    @Override
    public Builder description(String description) {
      super.description(description);
      return this;
    }

    public Builder specifiedByUrl(String specifiedByUrl) {
      this.specifiedByUrl = specifiedByUrl;
      return this;
    }

    @Override
    public Builder comparatorRegistry(GraphqlTypeComparatorRegistry comparatorRegistry) {
      super.comparatorRegistry(comparatorRegistry);
      return this;
    }

    public Builder definition(ScalarTypeDefinition definition) {
      this.definition = definition;
      return this;
    }

    public Builder extensionDefinitions(List<ScalarTypeExtensionDefinition> extensionDefinitions) {
      this.extensionDefinitions = extensionDefinitions;
      return this;
    }

    public Builder coercing(Coercing coercing) {
      this.coercing = coercing;
      return this;
    }

    public Builder withDirectives(GraphQLDirective... directives) {
      assertNotNull(directives, () -> "directives can't be null");
      this.directives.clear();
      for (GraphQLDirective directive : directives) {
        withDirective(directive);
      }
      return this;
    }

    public Builder withDirective(GraphQLDirective directive) {
      assertNotNull(directive, () -> "directive can't be null");
      DirectivesUtil.enforceAdd(this.directives, directive);
      return this;
    }

    public Builder replaceDirectives(List<GraphQLDirective> directives) {
      assertNotNull(directives, () -> "directive can't be null");
      this.directives.clear();
      DirectivesUtil.enforceAddAll(this.directives, directives);
      return this;
    }

    public Builder withDirective(GraphQLDirective.Builder builder) {
      return withDirective(builder.build());
    }

    /**
     * This is used to clear all the directives in the builder so far.
     *
     * @return this builder
     */
    public Builder clearDirectives() {
      directives.clear();
      return this;
    }

    public GraphQLScalarType build() {
      return new GraphQLScalarType(name,
                                   description,
                                   coercing,
                                   sort(directives, GraphQLScalarType.class, GraphQLDirective.class),
                                   definition,
                                   extensionDefinitions,
                                   specifiedByUrl);
    }
  }
}
