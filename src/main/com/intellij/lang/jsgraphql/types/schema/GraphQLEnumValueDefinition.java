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
import com.intellij.lang.jsgraphql.types.language.EnumValueDefinition;
import com.intellij.lang.jsgraphql.types.util.TraversalControl;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.Assert.assertValidName;
import static java.util.Collections.emptyList;

/**
 * A graphql enumeration type has a limited set of values and this defines one of those unique values
 * <p>
 * See http://graphql.org/learn/schema/#enumeration-types for more details
 *
 * @see com.intellij.lang.jsgraphql.types.schema.GraphQLEnumType
 */
@PublicApi
public class GraphQLEnumValueDefinition implements GraphQLNamedSchemaElement, GraphQLDirectiveContainer {

  private final String name;
  private final String description;
  private final Object value;
  private final String deprecationReason;
  private final DirectivesUtil.DirectivesHolder directives;
  private final EnumValueDefinition definition;

  public static final String CHILD_DIRECTIVES = "directives";

  /**
   * @param name        the name
   * @param description the description
   * @param value       the value
   * @deprecated use the {@link #newEnumValueDefinition()}   builder pattern instead, as this constructor will be made private in a future version.
   */
  @Internal
  @Deprecated(forRemoval = true)
  public GraphQLEnumValueDefinition(String name, String description, Object value) {
    this(name, description, value, null, emptyList());
  }

  /**
   * @param name              the name
   * @param description       the description
   * @param value             the value
   * @param deprecationReason the deprecation reasons
   * @deprecated use the {@link #newEnumValueDefinition()}   builder pattern instead, as this constructor will be made private in a future version.
   */
  @Internal
  @Deprecated(forRemoval = true)
  public GraphQLEnumValueDefinition(String name, String description, Object value, String deprecationReason) {
    this(name, description, value, deprecationReason, emptyList());
  }

  /**
   * @param name              the name
   * @param description       the description
   * @param value             the value
   * @param deprecationReason the deprecation reasons
   * @param directives        the directives on this type element
   * @deprecated use the {@link #newEnumValueDefinition()}   builder pattern instead, as this constructor will be made private in a future version.
   */
  @Internal
  @Deprecated(forRemoval = true)
  public GraphQLEnumValueDefinition(String name,
                                    String description,
                                    Object value,
                                    String deprecationReason,
                                    List<GraphQLDirective> directives) {
    this(name, description, value, deprecationReason, directives, null);
  }

  private GraphQLEnumValueDefinition(String name,
                                     String description,
                                     Object value,
                                     String deprecationReason,
                                     List<GraphQLDirective> directives,
                                     EnumValueDefinition definition) {
    assertValidName(name);
    assertNotNull(directives, () -> "directives cannot be null");

    this.name = name;
    this.description = description;
    this.value = value;
    this.deprecationReason = deprecationReason;
    this.directives = new DirectivesUtil.DirectivesHolder(directives);
    this.definition = definition;
  }

  @Override
  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Object getValue() {
    return value;
  }

  public boolean isDeprecated() {
    return deprecationReason != null;
  }

  public String getDeprecationReason() {
    return deprecationReason;
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

  public EnumValueDefinition getDefinition() {
    return definition;
  }

  /**
   * This helps you transform the current GraphQLEnumValueDefinition into another one by starting a builder with all
   * the current values and allows you to transform it how you want.
   *
   * @param builderConsumer the consumer code that will be given a builder to transform
   * @return a new field based on calling build on that builder
   */
  public GraphQLEnumValueDefinition transform(Consumer<Builder> builderConsumer) {
    Builder builder = newEnumValueDefinition(this);
    builderConsumer.accept(builder);
    return builder.build();
  }

  @Override
  public TraversalControl accept(TraverserContext<GraphQLSchemaElement> context, GraphQLTypeVisitor visitor) {
    return visitor.visitGraphQLEnumValueDefinition(this, context);
  }

  @Override
  public List<GraphQLSchemaElement> getChildren() {
    return ImmutableList.copyOf(directives.getDirectives());
  }

  @Override
  public SchemaElementChildrenContainer getChildrenWithTypeReferences() {
    return SchemaElementChildrenContainer.newSchemaElementChildrenContainer()
      .children(CHILD_DIRECTIVES, directives.getDirectives())
      .build();
  }

  @Override
  public GraphQLEnumValueDefinition withNewChildren(SchemaElementChildrenContainer newChildren) {
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

  @Override
  public String toString() {
    return "GraphQLEnumValueDefinition{" +
           "name='" + name + '\'' +
           ", description='" + description + '\'' +
           ", value=" + value +
           ", deprecationReason='" + deprecationReason + '\'' +
           ", directives=" + directives +
           ", definition=" + definition +
           '}';
  }

  public static Builder newEnumValueDefinition() {
    return new Builder();
  }

  public static Builder newEnumValueDefinition(GraphQLEnumValueDefinition existing) {
    return new Builder(existing);
  }

  @PublicApi
  public static class Builder extends GraphqlTypeBuilder {
    private Object value;
    private String deprecationReason;
    private EnumValueDefinition definition;
    private final List<GraphQLDirective> directives = new ArrayList<>();

    public Builder() {
    }

    public Builder(GraphQLEnumValueDefinition existing) {
      this.name = existing.getName();
      this.description = existing.getDescription();
      this.value = existing.getValue();
      this.deprecationReason = existing.getDeprecationReason();
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

    @Override
    public Builder comparatorRegistry(GraphqlTypeComparatorRegistry comparatorRegistry) {
      super.comparatorRegistry(comparatorRegistry);
      return this;
    }

    public Builder value(Object value) {
      this.value = value;
      return this;
    }

    public Builder deprecationReason(String deprecationReason) {
      this.deprecationReason = deprecationReason;
      return this;
    }

    public Builder definition(EnumValueDefinition definition) {
      this.definition = definition;
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
     * @return the builder
     */
    public Builder clearDirectives() {
      directives.clear();
      return this;
    }

    public GraphQLEnumValueDefinition build() {
      return new GraphQLEnumValueDefinition(name, description, value, deprecationReason, directives, definition);
    }
  }
}
