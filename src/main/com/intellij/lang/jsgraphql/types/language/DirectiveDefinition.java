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
package com.intellij.lang.jsgraphql.types.language;


import com.google.common.collect.ImmutableList;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.collect.ImmutableKit;
import com.intellij.lang.jsgraphql.types.util.TraversalControl;
import com.intellij.lang.jsgraphql.types.util.TraverserContext;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.emptyList;
import static com.intellij.lang.jsgraphql.types.language.NodeChildrenContainer.newNodeChildrenContainer;
import static java.util.Collections.emptyMap;

@PublicApi
public class DirectiveDefinition extends AbstractDescribedNode<DirectiveDefinition>
  implements SDLDefinition<DirectiveDefinition>, NamedNode<DirectiveDefinition> {
  private final String name;
  private final boolean repeatable;
  private final ImmutableList<InputValueDefinition> inputValueDefinitions;
  private final ImmutableList<DirectiveLocation> directiveLocations;

  public static final String CHILD_INPUT_VALUE_DEFINITIONS = "inputValueDefinitions";
  public static final String CHILD_DIRECTIVE_LOCATION = "directiveLocation";

  @Internal
  protected DirectiveDefinition(String name,
                                boolean repeatable,
                                Description description,
                                List<InputValueDefinition> inputValueDefinitions,
                                List<DirectiveLocation> directiveLocations,
                                SourceLocation sourceLocation,
                                List<Comment> comments,
                                IgnoredChars ignoredChars,
                                Map<String, String> additionalData,
                                @Nullable List<? extends Node> sourceNodes) {
    super(sourceLocation, comments, ignoredChars, additionalData, description, sourceNodes);
    this.name = name;
    this.repeatable = repeatable;
    this.inputValueDefinitions = ImmutableList.copyOf(inputValueDefinitions);
    this.directiveLocations = ImmutableList.copyOf(directiveLocations);
  }

  /**
   * alternative to using a Builder for convenience
   *
   * @param name of the directive definition
   */
  public DirectiveDefinition(String name) {
    this(name, false, null, emptyList(), emptyList(), null, emptyList(), IgnoredChars.EMPTY, emptyMap(), null);
  }

  @Override
  public String getName() {
    return name;
  }

  /**
   * An AST node can have multiple directives associated with it IF the directive definition allows
   * repeatable directives.
   *
   * @return true if this directive definition allows repeatable directives
   */
  public boolean isRepeatable() {
    return repeatable;
  }

  public List<InputValueDefinition> getInputValueDefinitions() {
    return inputValueDefinitions;
  }

  public List<DirectiveLocation> getDirectiveLocations() {
    return directiveLocations;
  }

  @Override
  public List<Node> getChildren() {
    List<Node> result = new ArrayList<>();
    result.addAll(inputValueDefinitions);
    result.addAll(directiveLocations);
    return result;
  }

  @Override
  public NodeChildrenContainer getNamedChildren() {
    return newNodeChildrenContainer()
      .children(CHILD_INPUT_VALUE_DEFINITIONS, inputValueDefinitions)
      .children(CHILD_DIRECTIVE_LOCATION, directiveLocations)
      .build();
  }

  @Override
  public DirectiveDefinition withNewChildren(NodeChildrenContainer newChildren) {
    return transform(builder -> builder
      .inputValueDefinitions(newChildren.getChildren(CHILD_INPUT_VALUE_DEFINITIONS))
      .directiveLocations(newChildren.getChildren(CHILD_DIRECTIVE_LOCATION))
    );
  }

  @Override
  public boolean isEqualTo(Node o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DirectiveDefinition that = (DirectiveDefinition)o;

    return Objects.equals(this.name, that.name);
  }

  @Override
  public DirectiveDefinition deepCopy() {
    return new DirectiveDefinition(name,
                                   repeatable,
                                   description,
                                   deepCopy(inputValueDefinitions),
                                   deepCopy(directiveLocations),
                                   getSourceLocation(),
                                   getComments(),
                                   getIgnoredChars(),
                                   getAdditionalData(),
                                   getSourceNodes());
  }

  @Override
  public String toString() {
    return "DirectiveDefinition{" +
           "name='" + name + "'" +
           ", inputValueDefinitions=" + inputValueDefinitions +
           ", directiveLocations=" + directiveLocations +
           "}";
  }

  @Override
  public TraversalControl accept(TraverserContext<Node> context, NodeVisitor visitor) {
    return visitor.visitDirectiveDefinition(this, context);
  }

  public static Builder newDirectiveDefinition() {
    return new Builder();
  }

  public DirectiveDefinition transform(Consumer<Builder> builderConsumer) {
    Builder builder = new Builder(this);
    builderConsumer.accept(builder);
    return builder.build();
  }

  public static final class Builder implements NodeBuilder {
    private SourceLocation sourceLocation;
    private ImmutableList<Comment> comments = emptyList();
    private String name;
    private boolean repeatable = false;
    private Description description;
    private ImmutableList<InputValueDefinition> inputValueDefinitions = emptyList();
    private ImmutableList<DirectiveLocation> directiveLocations = emptyList();
    private IgnoredChars ignoredChars = IgnoredChars.EMPTY;
    private Map<String, String> additionalData = new LinkedHashMap<>();
    private @Nullable List<? extends Node> sourceNodes;

    private Builder() {
    }

    private Builder(DirectiveDefinition existing) {
      this.sourceLocation = existing.getSourceLocation();
      this.comments = ImmutableList.copyOf(existing.getComments());
      this.name = existing.getName();
      this.repeatable = existing.isRepeatable();
      this.description = existing.getDescription();
      this.inputValueDefinitions = ImmutableList.copyOf(existing.getInputValueDefinitions());
      this.directiveLocations = ImmutableList.copyOf(existing.getDirectiveLocations());
      this.ignoredChars = existing.getIgnoredChars();
      this.additionalData = new LinkedHashMap<>(existing.getAdditionalData());
      this.sourceNodes = existing.getSourceNodes();
    }

    public Builder sourceLocation(SourceLocation sourceLocation) {
      this.sourceLocation = sourceLocation;
      return this;
    }

    public Builder comments(List<Comment> comments) {
      this.comments = ImmutableList.copyOf(comments);
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder repeatable(boolean repeatable) {
      this.repeatable = repeatable;
      return this;
    }

    public Builder description(Description description) {
      this.description = description;
      return this;
    }

    public Builder inputValueDefinitions(List<InputValueDefinition> inputValueDefinitions) {
      this.inputValueDefinitions = ImmutableList.copyOf(inputValueDefinitions);
      return this;
    }

    public Builder inputValueDefinition(InputValueDefinition inputValueDefinition) {
      this.inputValueDefinitions = ImmutableKit.addToList(inputValueDefinitions, inputValueDefinition);
      return this;
    }


    public Builder directiveLocations(List<DirectiveLocation> directiveLocations) {
      this.directiveLocations = ImmutableList.copyOf(directiveLocations);
      return this;
    }

    public Builder directiveLocation(DirectiveLocation directiveLocation) {
      this.directiveLocations = ImmutableKit.addToList(directiveLocations, directiveLocation);
      return this;
    }

    public Builder ignoredChars(IgnoredChars ignoredChars) {
      this.ignoredChars = ignoredChars;
      return this;
    }

    public Builder additionalData(Map<String, String> additionalData) {
      this.additionalData = assertNotNull(additionalData);
      return this;
    }

    public Builder additionalData(String key, String value) {
      this.additionalData.put(key, value);
      return this;
    }

    public Builder sourceNodes(@Nullable List<? extends Node> sourceNodes) {
      this.sourceNodes = sourceNodes;
      return this;
    }

    public DirectiveDefinition build() {
      return new DirectiveDefinition(name, repeatable, description, inputValueDefinitions, directiveLocations, sourceLocation, comments,
                                     ignoredChars, additionalData, sourceNodes);
    }
  }
}
